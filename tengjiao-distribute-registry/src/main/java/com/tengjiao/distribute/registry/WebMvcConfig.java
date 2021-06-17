package com.tengjiao.distribute.registry;

import com.tengjiao.distribute.registry.web.interceptor.CookieInterceptor;
import com.tengjiao.distribute.registry.web.interceptor.PermissionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * web mvc config
 *
 * @author
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private PermissionInterceptor permissionInterceptor;
    private CookieInterceptor cookieInterceptor;

    public WebMvcConfig(PermissionInterceptor permissionInterceptor, CookieInterceptor cookieInterceptor) {
        this.permissionInterceptor = permissionInterceptor;
        this.cookieInterceptor = cookieInterceptor;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor).addPathPatterns("/**");
        registry.addInterceptor(cookieInterceptor).addPathPatterns("/**");
    }

}