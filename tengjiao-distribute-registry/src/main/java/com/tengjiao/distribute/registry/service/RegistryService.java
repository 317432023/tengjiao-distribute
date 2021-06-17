package com.tengjiao.distribute.registry.service;


import com.tengjiao.distribute.registry.model.Registry;
import com.tengjiao.distribute.registry.model.RegistryData;
import com.tengjiao.distribute.registry.result.ReturnT;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Map;

public interface RegistryService {

    // ------------------------ registry admin management ------------------------

    Map<String,Object> pageList(int start, int length, String biz, String env, String key);
    /** 手动删除服务 */
    ReturnT<String> delete(int id);
    /** 手动修改服务 */
    ReturnT<String> update(Registry xxlRpcRegistry);
    /** 手动添加服务 */
    ReturnT<String> add(Registry xxlRpcRegistry);


    // ------------------------ remote registry ------------------------

    /**
     * refresh registry-value, check update and broadcast<br>
     *     服务注册，成功后广播
     */
    ReturnT<String> registry(String accessToken, String biz, String env, List<RegistryData> registryDataList);

    /**
     * remove registry-value, check update and broadcast<br>
     *     服务摘除，成功后广播
     */
    ReturnT<String> remove(String accessToken, String biz, String env, List<RegistryData> registryDataList);

    /**
     * discovery registry-data, read file<br>
     *     服务发现
     */
    ReturnT<Map<String, List<String>>> discovery(String accessToken, String biz, String env, List<String> keys);

    /**
     * monitor update<br>
     *     服务监视
     */
    DeferredResult<ReturnT<String>> monitor(String accessToken, String biz, String env, List<String> keys);

}
