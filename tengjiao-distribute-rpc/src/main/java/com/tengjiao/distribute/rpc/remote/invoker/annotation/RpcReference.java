package com.tengjiao.distribute.rpc.remote.invoker.annotation;


import com.tengjiao.distribute.rpc.remote.invoker.call.CallType;
import com.tengjiao.distribute.rpc.remote.invoker.route.LoadBalance;
import com.tengjiao.distribute.rpc.remote.net.Client;
import com.tengjiao.distribute.rpc.remote.net.impl.netty.client.NettyClient;
import com.tengjiao.distribute.rpc.serialize.Serializer;
import com.tengjiao.distribute.rpc.serialize.impl.HessianSerializer;

import java.lang.annotation.*;

/**
 * rpc service annotation, skeleton of stub ("@Inherited" allow service use "Transactional")
 *
 * @author 2015-10-29 19:44:33
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RpcReference {

    Class<? extends Client> client() default NettyClient.class;
    Class<? extends Serializer> serializer() default HessianSerializer.class;
    CallType callType() default CallType.SYNC;
    LoadBalance loadBalance() default LoadBalance.ROUND;

    //Class<?> iface;
    String version() default "";

    long timeout() default 1000;

    String address() default "";
    String accessToken() default "";

    //RpcInvokeCallback invokeCallback() ;

}
