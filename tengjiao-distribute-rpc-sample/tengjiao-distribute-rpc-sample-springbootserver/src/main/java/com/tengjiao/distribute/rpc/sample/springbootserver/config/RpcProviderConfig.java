package com.tengjiao.distribute.rpc.sample.springbootserver.config;

import com.tengjiao.distribute.rpc.registry.impl.RegistryRegister;
import com.tengjiao.distribute.rpc.remote.net.impl.netty.server.NettyServer;
import com.tengjiao.distribute.rpc.remote.provider.RpcSpringProvider;
import com.tengjiao.distribute.rpc.serialize.impl.HessianSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * provider config
 *
 * @author
 */
@Configuration
public class RpcProviderConfig {
    private Logger logger = LoggerFactory.getLogger(RpcProviderConfig.class);

    @Value("${tengjiao.distribute.rpc.remoting.port}")
    private int port;

    @Value("${tengjiao.distribute.rpc.registry.address}")
    private String address;

    @Value("${tengjiao.distribute.rpc.registry.env}")
    private String env;

    @Bean
    public RpcSpringProvider rpcSpringProvider() {

        RpcSpringProvider providerFactory = new RpcSpringProvider();
        providerFactory.setServer(NettyServer.class);
        providerFactory.setSerializer(HessianSerializer.class);
        providerFactory.setCorePoolSize(-1);
        providerFactory.setMaxPoolSize(-1);
        providerFactory.setIp(null);
        providerFactory.setPort(port);
        providerFactory.setAccessToken(null);
        providerFactory.setServiceRegistry(RegistryRegister.class);
        providerFactory.setServiceRegistryParam(new HashMap<String, String>() {{
            put(RegistryRegister.ADMIN_ADDRESS, address);
            put(RegistryRegister.ENV, env);
        }});

        logger.info(">>>>>>>>>>> provider config init finish.");
        return providerFactory;
    }

}