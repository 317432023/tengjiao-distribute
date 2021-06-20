package com.tengjiao.distribute.rpc.sample.springbootclient.config;

import com.tengjiao.distribute.rpc.registry.impl.RegistryRegister;
import com.tengjiao.distribute.rpc.remote.invoker.RpcSpringInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * invoker config
 *
 * @author
 */
@Configuration
public class RpcInvokerConfig {
    private Logger logger = LoggerFactory.getLogger(RpcInvokerConfig.class);


    @Value("${tengjiao.distribute.rpc.registry.address}")
    private String address;

    @Value("${tengjiao.distribute.rpc.registry.env}")
    private String env;


    @Bean
    public RpcSpringInvoker xxlJobExecutor() {

        RpcSpringInvoker rpcSpringInvoker = new RpcSpringInvoker();
        rpcSpringInvoker.setServiceRegistryClass(RegistryRegister.class);
        rpcSpringInvoker.setServiceRegistryParam(new HashMap<String, String>(){{
            put(RegistryRegister.ADMIN_ADDRESS, address);
            put(RegistryRegister.ENV, env);
            // 默认"true"
            put(RegistryRegister.IS_PROVIDER, "false");
        }});

        logger.info(">>>>>>>>>>> invoker config init finish.");
        return rpcSpringInvoker;
    }

}