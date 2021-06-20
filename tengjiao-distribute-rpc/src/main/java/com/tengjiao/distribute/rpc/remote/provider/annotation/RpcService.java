package com.tengjiao.distribute.rpc.remote.provider.annotation;

import java.lang.annotation.*;

/**
 * @author
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RpcService {
    String version() default "";
}
