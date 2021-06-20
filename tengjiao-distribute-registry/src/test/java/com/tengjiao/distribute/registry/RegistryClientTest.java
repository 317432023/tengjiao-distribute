package com.tengjiao.distribute.registry;

import com.tengjiao.distribute.rpc.registry.client.RegistryClient;
import com.tengjiao.distribute.rpc.registry.model.RegistryDataParamVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class RegistryClientTest {
    public static void main(String[] args) throws InterruptedException {
        RegistryClient registryClient = new RegistryClient("http://localhost:8080/tengjiao-distribute-registry", null, "xxl-rpc", "test");

        // registry test
        List<RegistryDataParamVO> registryDataList = new ArrayList<>();
        registryDataList.add(new RegistryDataParamVO("service01", "address01"));
        registryDataList.add(new RegistryDataParamVO("service02", "address02"));

        System.out.println("registry:" + registryClient.registry(registryDataList));
        TimeUnit.SECONDS.sleep(2);

        // discovery test
        Set<String> keys = new TreeSet<>();
        keys.add("service01");
        keys.add("service02");

        System.out.println("discovery:" + registryClient.discovery(keys));

        while (true) {
            TimeUnit.SECONDS.sleep(1);
        }
    }
}
