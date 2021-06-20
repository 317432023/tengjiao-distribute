package com.tengjiao.distribute.rpc.registry.client;

import com.tengjiao.distribute.rpc.registry.model.RegistryDataParamVO;
import com.tengjiao.distribute.rpc.registry.model.RegistryParamVO;
import com.tengjiao.tool.core.HttpTool;
import com.tengjiao.tool.core.json.BasicJsonTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * base util for registry
 *
 * @author
 */
public class RegistryBaseClient {
    private static Logger logger = LoggerFactory.getLogger(RegistryBaseClient.class);

    public static final int
        /** 服务注册，5秒超时 */
        RegistryTimeout = 5,
        /** 服务查找，5秒超时 */
        DiscoveryTimeout = 5,
        /** 服务摘除，5秒超时 */
        RemoveTimeout = 5,
        /** 服务监视，60秒超时 */
        MonitorTimeout = 60;

    private String adminAddress;
    private String accessToken;
    private String biz;
    private String env;

    private List<String> adminAddressArr;


    public RegistryBaseClient(String adminAddress, String accessToken, String biz, String env) {
        this.adminAddress = adminAddress;
        this.accessToken = accessToken;
        this.biz = biz;
        this.env = env;

        // valid
        if (adminAddress==null || adminAddress.trim().length()==0) {
            throw new RuntimeException("adminAddress empty");
        }
        if (biz==null || biz.trim().length()<4 || biz.trim().length()>255) {
            throw new RuntimeException("biz empty Invalid[4~255]");
        }
        if (env==null || env.trim().length()<2 || env.trim().length()>255) {
            throw new RuntimeException("biz env Invalid[2~255]");
        }

        // parse
        adminAddressArr = new ArrayList<>();
        if (adminAddress.contains(",")) {
            adminAddressArr.addAll(Arrays.asList(adminAddress.split(",")));
        } else {
            adminAddressArr.add(adminAddress);
        }

    }

    /**
     * registry
     *
     * @param registryDataList
     * @return
     */
    public boolean registry(List<RegistryDataParamVO> registryDataList){

        // valid
        if ( registryDataList == null || registryDataList.size() == 0 ) {
            throw new RuntimeException("registryDataList empty");
        }
        for (RegistryDataParamVO registryParam: registryDataList) {
            if (registryParam.getKey()==null || registryParam.getKey().trim().length()<4 || registryParam.getKey().trim().length()>255) {
                throw new RuntimeException("registryDataList#key Invalid[4~255]");
            }
            if (registryParam.getValue()==null || registryParam.getValue().trim().length()<4 || registryParam.getValue().trim().length()>255) {
                throw new RuntimeException("registryDataList#value Invalid[4~255]");
            }
        }

        // pathUrl
        String pathUrl = "/api/registry";

        // param
        RegistryParamVO registryParamVO = new RegistryParamVO();
        registryParamVO.setAccessToken(this.accessToken);
        registryParamVO.setBiz(this.biz);
        registryParamVO.setEnv(this.env);
        registryParamVO.setRegistryDataList(registryDataList);

        String paramsJson = BasicJsonTool.toJson(registryParamVO);

        // result
        Map<String, Object> respObj = requestAndValid(pathUrl, paramsJson, RegistryTimeout);
        return respObj!=null?true:false;
    }

    private Map<String, Object> requestAndValid(String pathUrl, String requestBody, int timeout){

        for (String adminAddressUrl: adminAddressArr) {
            String finalUrl = adminAddressUrl + pathUrl;

            // request
            String responseData = HttpTool.postBody(finalUrl, requestBody, timeout);
            if (responseData == null) {
                return null;
            }

            // parse response
            Map<String, Object> responseMap = null;
            try {
                responseMap = BasicJsonTool.parseMap(responseData);
            } catch (Exception e) { }


            // valid resopnse
            if (responseMap==null
                    || !responseMap.containsKey("code")
                    || !"200".equals(String.valueOf(responseMap.get("code")))
                    ) {
                logger.warn("RegistryBaseClient response fail, responseData={}", responseData);
                return null;
            }

            return responseMap;
        }


        return null;
    }

    /**
     * remove
     *
     * @param registryDataList
     * @return
     */
    public boolean remove(List<RegistryDataParamVO> registryDataList) {
        // valid
        if (registryDataList==null || registryDataList.size()==0) {
            throw new RuntimeException("registryDataList empty");
        }
        for (RegistryDataParamVO registryParam: registryDataList) {
            if (registryParam.getKey()==null || registryParam.getKey().trim().length()<4 || registryParam.getKey().trim().length()>255) {
                throw new RuntimeException("registryDataList#key Invalid[4~255]");
            }
            if (registryParam.getValue()==null || registryParam.getValue().trim().length()<4 || registryParam.getValue().trim().length()>255) {
                throw new RuntimeException("registryDataList#value Invalid[4~255]");
            }
        }

        // pathUrl
        String pathUrl = "/api/remove";

        // param
        RegistryParamVO registryParamVO = new RegistryParamVO();
        registryParamVO.setAccessToken(this.accessToken);
        registryParamVO.setBiz(this.biz);
        registryParamVO.setEnv(this.env);
        registryParamVO.setRegistryDataList(registryDataList);

        String paramsJson = BasicJsonTool.toJson(registryParamVO);

        // result
        Map<String, Object> respObj = requestAndValid(pathUrl, paramsJson, RemoveTimeout);
        return respObj!=null?true:false;
    }

    /**
     * discovery
     *
     * @param keys
     * @return
     */
    public Map<String, TreeSet<String>> discovery(Set<String> keys) {
        // valid
        if (keys==null || keys.size()==0) {
            throw new RuntimeException("keys empty");
        }

        // pathUrl
        String pathUrl = "/api/discovery";

        // param
        RegistryParamVO registryParamVO = new RegistryParamVO();
        registryParamVO.setAccessToken(this.accessToken);
        registryParamVO.setBiz(this.biz);
        registryParamVO.setEnv(this.env);
        registryParamVO.setKeys(new ArrayList<String>(keys));

        String paramsJson = BasicJsonTool.toJson(registryParamVO);

        // result
        Map<String, Object> respObj = requestAndValid(pathUrl, paramsJson, DiscoveryTimeout);

        // parse
        if (respObj!=null && respObj.containsKey("data")) {
            Map<String, TreeSet<String>> data = (Map<String, TreeSet<String>>) respObj.get("data");
            return data;
        }

        return null;
    }

    /**
     * discovery
     *
     * @param keys
     * @return
     */
    public boolean monitor(Set<String> keys) {
        // valid
        if (keys==null || keys.size()==0) {
            throw new RuntimeException("keys empty");
        }

        // pathUrl
        String pathUrl = "/api/monitor";

        // param
        RegistryParamVO registryParamVO = new RegistryParamVO();
        registryParamVO.setAccessToken(this.accessToken);
        registryParamVO.setBiz(this.biz);
        registryParamVO.setEnv(this.env);
        registryParamVO.setKeys(new ArrayList<String>(keys));

        String paramsJson = BasicJsonTool.toJson(registryParamVO);

        // result
        Map<String, Object> respObj = requestAndValid(pathUrl, paramsJson, MonitorTimeout);
        return respObj!=null?true:false;
    }

}
