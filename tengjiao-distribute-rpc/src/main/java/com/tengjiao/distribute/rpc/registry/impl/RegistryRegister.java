package com.tengjiao.distribute.rpc.registry.impl;

import com.tengjiao.distribute.rpc.registry.Register;
import com.tengjiao.distribute.rpc.registry.client.RegistryClient;
import com.tengjiao.distribute.rpc.registry.model.RegistryDataParamVO;

import java.util.*;

/**
 * @author
 */
public class RegistryRegister extends Register {
    /** ---------------------- 键常量 ---------------------- */
    public static final String
            ADMIN_ADDRESS = "ADMIN_ADDRESS",
            ACCESS_TOKEN = "ACCESS_TOKEN",
            BIZ = "BIZ",
            ENV = "ENV";

    private RegistryClient registryClient;
    public RegistryClient getRegistryClient() {
        return registryClient;
    }

    @Override
    public void start(Map<String, String> param) {
        String registryAddress = param.get(ADMIN_ADDRESS);
        String accessToken = param.get(ACCESS_TOKEN);
        String biz = param.get(BIZ);
        String env = param.get(ENV);

        // fill
        biz = (biz!=null&&biz.trim().length()>0)?biz:"default";
        env = (env!=null&&env.trim().length()>0)?env:"default";

        registryClient = new RegistryClient(registryAddress, accessToken, biz, env);

    }

    @Override
    public void stop() {
        if (registryClient != null) {
            registryClient.stop();
        }
    }

    @Override
    public boolean registry(Set<String> keys, String value) {
        if (keys==null || keys.size() == 0 || value==null || value.trim().length()==0) {
            return false;
        }

        // init
        List<RegistryDataParamVO> registryDataList = new ArrayList<>();
        for (String key:keys) {
            registryDataList.add(new RegistryDataParamVO(key, value));
        }

        return registryClient.registry(registryDataList);
    }

    @Override
    public boolean remove(Set<String> keys, String value) {
        if (keys==null || keys.size() == 0 || value==null || value.trim().length()==0) {
            return false;
        }

        // init
        List<RegistryDataParamVO> registryDataList = new ArrayList<>();
        for (String key:keys) {
            registryDataList.add(new RegistryDataParamVO(key, value));
        }

        return registryClient.remove(registryDataList);
    }

    @Override
    public TreeSet<String> discovery(String key) {
        return registryClient.discovery(key);
    }

    @Override
    public Map<String, TreeSet<String>> discovery(Set<String> keys) {
        return registryClient.discovery(keys);
    }
}
