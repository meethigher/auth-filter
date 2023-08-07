package top.meethigher.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.meethigher.utils.JSONUtils;
import top.meethigher.utils.Resp;
import top.meethigher.utils.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * 身份验证验证器配置
 *
 * @author chenchuancheng
 * @date 2023/08/03 19:13
 */
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
