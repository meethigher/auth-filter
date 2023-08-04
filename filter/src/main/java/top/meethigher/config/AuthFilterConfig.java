package top.meethigher.config;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;
import top.meethigher.utils.JSONUtils;
import top.meethigher.utils.Resp;
import top.meethigher.utils.UserUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 身份验证过滤器配置
 *
 * @author chenchuancheng
 * @date 2023/08/03 15:48
 */
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
        //拦截规则
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
                response.setContentType("application/json;charset=utf-8");
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
