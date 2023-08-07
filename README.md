
针对个人项目而言，平时使用的Apache Shiro太过臃肿。

因此简单记录几种轻量的授权方式，分别是使用Filter、SpringMVC内置组件实现。

# 一、Filter

## 1.1 示例

核心示例代码

```java
@Configuration
public class AuthFilterConfig {


    /**
     * 身份验证过滤器
     *
     * @return {@link FilterRegistrationBean}
     */
    @Bean
    public FilterRegistrationBean authFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        //注入过滤器
        registrationBean.setFilter(new AuthFilter());
        //过滤器名称
        registrationBean.setName("AuthFilter");
        //拦截规则，就类似于jsp项目中@WebFilter配置urlPattern，该过程由web容器匹配，并不由spring管理
        registrationBean.addUrlPatterns("/test/test1/*");
        //过滤器顺序
        registrationBean.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);

        return registrationBean;
    }

    /**
     * 身份验证过滤器
     *
     * @author chenchuancheng
     * @date 2023/08/03 15:51
     */
    @Slf4j
    public static class AuthFilter implements Filter {

        /**
         * 预过滤
         *
         * @param request  请求
         * @param response 响应
         */
        public boolean preFilter(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            String token = request.getHeader("Token");
            if (ObjectUtils.isEmpty(token)) {
                Resp resp = Resp.getFailureResp("授权失败");
                String s = JSONUtils.toJSONString(resp);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getOutputStream().write(s.getBytes(StandardCharsets.UTF_8));
                return false;
            } else {
                UserUtils.add(Thread.currentThread().getName());
                return true;
            }
        }

        /**
         * 后过滤
         *
         * @param request  请求
         * @param response 响应
         */
        public void postFilter(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            UserUtils.remove();
        }

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            if (!preFilter(request, response)) {
                return;
            }
            filterChain.doFilter(request, response);
            postFilter(request, response);
        }
    }
}
```

## 1.2 优劣

请求会先经过所有的Filter，然后进入对应的Servlet进行处理，然后再回到所有的Filter进行响应处理。

```
Filter1 --> Filter2 --> ... --> FilterN --> Servlet --> FilterN --> ... --> Filter2 --> Filter1
```

优点

1. 不局限于Spring框架。因为Filter本身是Servlet规范所包含的。
2. 不影响前端页面的映射

缺点

1. 不支持Spring的全局异常捕获。Filter链执行完成之后，才会到Servlet。而Spring的全局异常捕获是在DispatcherServlet中实现的。
2. 只支持匹配规则，不支持排除。不过排除也是间接通过匹配实现的，需要自己实现才行。

## 1.3 Filter绕过问题

先简单介绍，如果我访问`/test/test1/test2`是会被拦截的，那么我访问`/a/b/../../test/test1/test2`呢？答案是也会被拦截。

之所以被拦截，是因为web容器进行了url标准化，url标准化可以防止一些路径遍历和恶意攻击。以tomcat为例，参考源码`org.apache.catalina.connector.CoyoteAdapter`

![](https://meethigher.top/blog/2023/auth-filter/image-20230808011705005.png)

![](https://meethigher.top/blog/2023/auth-filter/image-20230808010103414.png)

但要注意的是，如果是自己通过HttpServletRequest去getURL进行匹配，进而控制权限，这时候就会出现问题了，**getURL获取到的就是未标准化的路径，需要手动标准化**。shiro<1.6.0版本存在的权限绕过问题，就是由此引发的。

> 针对这种问题，只要保证拦截时，权限拦截正确即可，至于放行后的请求分发，是否进行url标准化就无所谓，比如我的[route-forward](https://github.com/meethigher/route-forward)，

![](https://meethigher.top/blog/2023/auth-filter/image-20230808005539400.png)

参考

1. [tomcat容器url解析特性研究 - 先知社区](https://xz.aliyun.com/t/10799)
2. [Java安全之Filter权限绕过 - nice_0e3 - 博客园](https://www.cnblogs.com/nice0e3/p/14801884.html#url%E7%BC%96%E7%A0%81%E7%BB%95%E8%BF%87)
3. [url解析特性造成绕过访问](https://moonsec.top/articles/61)

# 二、SpringMVC内置组件

## 2.1 WebMvcConfigurer

### 2.1.1 示例

核心示例代码

```java
@Configuration
public class AuthValidatorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthValidator())
                .addPathPatterns("/test/test1/**","/static/**")
                .excludePathPatterns("/static/index.html");
    }

    /**
     * 身份验证验证器
     *
     * @author chenchuancheng
     * @date 2023/08/03 19:14
     */
    public static class AuthValidator implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            String token = request.getHeader("Token");
            if (ObjectUtils.isEmpty(token)) {
                Resp resp = Resp.getFailureResp("授权失败");
                String s = JSONUtils.toJSONString(resp);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getOutputStream().write(s.getBytes(StandardCharsets.UTF_8));
                return false;
            } else {
                UserUtils.add(Thread.currentThread().getName());
                return true;
            }
        }

        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
            UserUtils.remove();
        }
    }
}
```

### 2.1.2 优劣

优点

1. 不影响前端页面的映射
2. 支持匹配与排除规则
3. 支持Spring的全局异常捕获

缺点，暂无

## 2.2 WebMvcConfigurationSupport

### 2.2.1 示例

核心示例代码

```java
@Configuration
public class AuthValidatorConfig extends WebMvcConfigurationSupport {


    @Resource
    private Environment environment;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthValidator())
                .addPathPatterns("/test/test1/**", "/static/**")
                .excludePathPatterns("/static/index.html");
    }


    /**
     * 自定义配置会导致swagger等内置页面失效，参考https://blog.csdn.net/rookiediary/article/details/106021572
     * 资源页面抄古来
     *
     * @param registry
     * @see org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
     * @see springfox.boot.starter.autoconfigure.SwaggerUiWebMvcConfigurer
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String staticPathPattern = environment.getProperty("spring.mvc.static-path-pattern");
        String staticLocation = environment.getProperty("spring.web.resources.static-locations");
        staticPathPattern = staticPathPattern == null ? "/**" : staticPathPattern;
        //此处如何配，只需要在WebMvcAutoConfiguration断点复制出来即可。
        //原因是有了WebMvcConfig之后，就不会再用默认的了，如果自己配置不对，就会有问题。
        registry.addResourceHandler("/webjars/**").addResourceLocations(
                "classpath:/META-INF/resources/webjars/");
        if (ObjectUtils.isEmpty(staticLocation)) {
            registry.addResourceHandler(staticPathPattern).addResourceLocations(
                    "classpath:/META-INF/resources/",
                    "classpath:/resources/",
                    "classpath:/static/",
                    "classpath:/public/"
            );
        } else {
            registry.addResourceHandler(staticPathPattern).addResourceLocations(staticLocation);
        }
        registry.addResourceHandler("/swagger-ui/**").addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/");

        super.addResourceHandlers(registry);
    }


    /**
     * 身份验证验证器
     *
     * @author chenchuancheng
     * @date 2023/08/03 19:14
     */
    public static class AuthValidator implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            String token = request.getHeader("Token");
            if (ObjectUtils.isEmpty(token)) {
                Resp resp = Resp.getFailureResp("授权失败");
                String s = JSONUtils.toJSONString(resp);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getOutputStream().write(s.getBytes(StandardCharsets.UTF_8));
                return false;
            } else {
                UserUtils.add(Thread.currentThread().getName());
                return true;
            }
        }

        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
            UserUtils.remove();
        }
    }
}
```

### 2.2.2 优劣

优点

1. 权限较大。可以拦截整个路由，进行自定义映射
2. 支持匹配与排除规则
3. 支持Spring的全局异常捕获(本质是基于Spring的DispatcherServlet内部实现的)

缺点

1. 页面映射需要自定义。比如swagger、部分spring配置参数等

## 2.3 两者对比

`WebMvcConfigurationSupport` 和 `WebMvcConfigurer` 都是 Spring MVC 中用于配置和自定义 Spring MVC 的组件，但它们有一些区别。

1. `WebMvcConfigurationSupport`： `WebMvcConfigurationSupport` 是一个抽象类，它提供了一个基本的 Spring MVC 配置，并且可以被继承来自定义更复杂的配置。如果你需要对 Spring MVC 进行更深入的定制，可以继承这个类，并覆盖其中的方法来配置拦截器、视图解析器、格式化器等。通过继承 `WebMvcConfigurationSupport`，你可以完全控制 Spring MVC 的配置，但同时需要自己处理更多的细节。
2. `WebMvcConfigurer`： `WebMvcConfigurer` 是一个接口，它提供了一组回调方法，允许你在 Spring MVC 的配置阶段进行自定义操作，而不需要继承任何类。通过实现这个接口，你可以注册拦截器、配置视图解析器、资源处理器、数据格式化器等。使用 `WebMvcConfigurer` 接口，可以更加简洁地实现一些配置需求，而不需要创建一个新的配置类。

总的来说，`WebMvcConfigurationSupport` 用于更复杂的配置情况，需要继承并重写方法，可以对 Spring MVC 进行全面的定制。而 `WebMvcConfigurer` 是一种更简洁的方式，通过实现接口来实现一部分配置，适用于较为简单的定制需求。

一般来说，如果你需要对 Spring MVC 进行大量的自定义配置，推荐使用 `WebMvcConfigurationSupport`；而对于简单的配置需求，`WebMvcConfigurer` 更加方便和推荐。在实际使用中，你也可以将两者结合起来，以满足不同层次的配置需求。

