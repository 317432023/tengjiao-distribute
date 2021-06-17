package com.tengjiao.distribute.registry;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 用于打包成war包可放在容器下面执行
 * <br>需要引入 spring-boot-starter-web并排除tomcat容器
 * @author kangtengjiao
 */
public class WarSupport extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(RegistryApplication.class);
  }

}
