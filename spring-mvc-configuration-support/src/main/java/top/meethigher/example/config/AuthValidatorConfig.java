package top.meethigher.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import top.meethigher.utils.JSONUtils;
import top.meethigher.utils.Resp;
import top.meethigher.utils.UserUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * 身份验证验证器配置
 *
 * @author chenchuancheng
 * @date 2023/08/03 19:36
 */
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
