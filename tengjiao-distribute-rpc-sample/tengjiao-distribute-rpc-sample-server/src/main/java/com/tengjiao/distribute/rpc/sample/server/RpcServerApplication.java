package com.tengjiao.distribute.rpc.sample.server;

import com.tengjiao.distribute.rpc.registry.impl.RegistryRegister;
import com.tengjiao.distribute.rpc.remote.net.impl.netty.server.NettyServer;
import com.tengjiao.distribute.rpc.remote.provider.RpcProvider;
import com.tengjiao.distribute.rpc.sample.api.DemoService;
import com.tengjiao.distribute.rpc.sample.server.service.DemoServiceImpl;
import com.tengjiao.distribute.rpc.serialize.impl.HessianSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RpcServerApplication {
    public static void main(String[] args) throws Exception {
        // init
        RpcProvider rpcProvider = new RpcProvider();
        rpcProvider.setServer(NettyServer.class);
        rpcProvider.setSerializer(HessianSerializer.class);
        rpcProvider.setCorePoolSize(-1);
        rpcProvider.setMaxPoolSize(-1);
        rpcProvider.setIp(null);
        rpcProvider.setPort(7080);
        rpcProvider.setAccessToken(null);
        //rpcProvider.setServiceRegistry(null);
        //rpcProvider.setServiceRegistryParam(null);

        rpcProvider.setServiceRegistry(RegistryRegister.class);

        Map<String, String> serviceRegistryParam = new HashMap<>();
        serviceRegistryParam.put(RegistryRegister.ADMIN_ADDRESS, "http://localhost:8080/tengjiao-distribute-registry");
        serviceRegistryParam.put(RegistryRegister.BIZ, "xxl-rpc");
        serviceRegistryParam.put(RegistryRegister.ENV, "test");

        rpcProvider.setServiceRegistryParam(serviceRegistryParam);

        // add services
        rpcProvider.addService(DemoService.class.getName(), null, new DemoServiceImpl());

        // start
        rpcProvider.start();

        while (!Thread.currentThread().isInterrupted()) {
            TimeUnit.HOURS.sleep(1);
        }

        // stop
        rpcProvider.stop();
    }
}
