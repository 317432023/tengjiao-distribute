package com.tengjiao.distribute.registry.service.impl;

import com.tengjiao.distribute.registry.model.Registry;
import com.tengjiao.distribute.registry.model.RegistryData;
import com.tengjiao.distribute.registry.model.RegistryMessage;
import com.tengjiao.distribute.registry.result.ReturnT;
import com.tengjiao.distribute.registry.util.JacksonUtil;
import com.tengjiao.distribute.registry.util.PropUtil;
import com.tengjiao.distribute.registry.dao.RegistryDao;
import com.tengjiao.distribute.registry.dao.RegistryDataDao;
import com.tengjiao.distribute.registry.dao.RegistryMessageDao;
import com.tengjiao.distribute.registry.service.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

@Service
public class RegistryServiceImpl implements RegistryService, InitializingBean, DisposableBean {
    private static Logger logger = LoggerFactory.getLogger(RegistryServiceImpl.class);

    @Autowired
    private RegistryDao registryDao;
    @Autowired
    private RegistryDataDao registryDataDao;
    @Autowired
    private RegistryMessageDao registryMessageDao;

    /*public RegistryServiceImpl(RegistryDao registryDao, RegistryDataDao registryDataDao, RegistryMessageDao registryMessageDao) {
        this.registryDao = registryDao;
        this.registryDataDao = registryDataDao;
        this.registryMessageDao = registryMessageDao;
    }*/


    @Value("${tengjiao.distribute.registry.data.filepath}")
    private String registryDataFilePath;
    @Value("${tengjiao.distribute.registry.accessToken}")
    private String accessToken;

    private int registryBeatTime = 10;


    @Override
    public Map<String, Object> pageList(int start, int length, String biz, String env, String key) {

        // page list
        List<Registry> list = registryDao.pageList(start, length, biz, env, key);
        int list_count = registryDao.pageListCount(start, length, biz, env, key);

        // package result
        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("recordsTotal", list_count);		// 总记录数
        maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
        maps.put("data", list);  					// 分页列表
        return maps;
    }

    @Override
    public ReturnT<String> delete(int id) {
        Registry xxlRpcRegistry = registryDao.loadById(id);
        if (xxlRpcRegistry != null) {
            registryDao.delete(id);
            registryDataDao.deleteData(xxlRpcRegistry.getBiz(), xxlRpcRegistry.getEnv(), xxlRpcRegistry.getKey());

            // sendRegistryDataUpdateMessage (delete)
            xxlRpcRegistry.setData("");
            sendRegistryDataUpdateMessage(xxlRpcRegistry);
        }

        return ReturnT.SUCCESS;
    }

    /**
     * send RegistryData Update Message
     */
    private void sendRegistryDataUpdateMessage(Registry xxlRpcRegistry){
        String registryUpdateJson = JacksonUtil.writeValueAsString(xxlRpcRegistry);

        RegistryMessage registryMessage = new RegistryMessage();
        registryMessage.setType(0);
        registryMessage.setData(registryUpdateJson);
        registryMessageDao.add(registryMessage);
    }

    @Override
    public ReturnT<String> update(Registry registry) {

        // valid
        if (registry.getBiz()==null || registry.getBiz().trim().length()<4 || registry.getBiz().trim().length()>255) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "业务线格式非法[4~255]");
        }
        if (registry.getEnv()==null || registry.getEnv().trim().length()<2 || registry.getEnv().trim().length()>255 ) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "环境格式非法[2~255]");
        }
        if (registry.getKey()==null || registry.getKey().trim().length()<4 || registry.getKey().trim().length()>255) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "注册Key格式非法[4~255]");
        }
        if (registry.getData()==null || registry.getData().trim().length()==0) {
            registry.setData(JacksonUtil.writeValueAsString(new ArrayList<String>()));
        }
        List<String> valueList = JacksonUtil.readValue(registry.getData(), List.class);
        if (valueList == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "注册Value数据格式非法；限制为字符串数组JSON格式，如 [address,address2]");
        }

        // valid exist
        Registry exist = registryDao.loadById(registry.getId());
        if (exist == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "ID参数非法");
        }

        // if refresh
        boolean needMessage = !registry.getData().equals(exist.getData());

        int ret = registryDao.update(registry);
        needMessage = ret>0?needMessage:false;

        if (needMessage) {
            // sendRegistryDataUpdateMessage (update)
            sendRegistryDataUpdateMessage(registry);
        }

        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    @Override
    public ReturnT<String> add(Registry registry) {

        // valid
        if (registry.getBiz()==null || registry.getBiz().trim().length()<4 || registry.getBiz().trim().length()>255) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "业务线格式非法[4~255]");
        }
        if (registry.getEnv()==null || registry.getEnv().trim().length()<2 || registry.getEnv().trim().length()>255 ) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "环境格式非法[2~255]");
        }
        if (registry.getKey()==null || registry.getKey().trim().length()<4 || registry.getKey().trim().length()>255) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "注册Key格式非法[4~255]");
        }
        if (registry.getData()==null || registry.getData().trim().length()==0) {
            registry.setData(JacksonUtil.writeValueAsString(new ArrayList<String>()));
        }
        List<String> valueList = JacksonUtil.readValue(registry.getData(), List.class);
        if (valueList == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "注册Value数据格式非法；限制为字符串数组JSON格式，如 [address,address2]");
        }

        // valid exist
        Registry exist = registryDao.load(registry.getBiz(), registry.getEnv(), registry.getKey());
        if (exist != null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "注册Key请勿重复");
        }

        int ret = registryDao.add(registry);
        boolean needMessage = ret>0?true:false;

        if (needMessage) {
            // sendRegistryDataUpdateMessage (add)
            sendRegistryDataUpdateMessage(registry);
        }

        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }


    // ------------------------ remote registry ------------------------

    @Override
    public ReturnT<String> registry(String accessToken, String biz, String env, List<RegistryData> registryDataList) {

        // valid
        if (this.accessToken!=null && this.accessToken.trim().length()>0 && !this.accessToken.equals(accessToken)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "AccessToken Invalid");
        }
        if (biz==null || biz.trim().length()<4 || biz.trim().length()>255) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Biz Invalid[4~255]");
        }
        if (env==null || env.trim().length()<2 || env.trim().length()>255) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Env Invalid[2~255]");
        }
        if (registryDataList==null || registryDataList.size()==0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Registry DataList Invalid");
        }
        for (RegistryData registryData: registryDataList) {
            if (registryData.getKey()==null || registryData.getKey().trim().length()<4 || registryData.getKey().trim().length()>255) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "Registry Key Invalid[4~255]");
            }
            if (registryData.getValue()==null || registryData.getValue().trim().length()<4 || registryData.getValue().trim().length()>255) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "Registry Value Invalid[4~255]");
            }
        }

        // fill + add queue
        for (RegistryData registryData: registryDataList) {
            registryData.setBiz(biz);
            registryData.setEnv(env);
        }
        registryQueue.addAll(registryDataList);

        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> remove(String accessToken, String biz, String env, List<RegistryData> registryDataList) {

        // valid
        if (this.accessToken!=null && this.accessToken.trim().length()>0 && !this.accessToken.equals(accessToken)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "AccessToken Invalid");
        }
        if (biz==null || biz.trim().length()<4 || biz.trim().length()>255) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Biz Invalid[4~255]");
        }
        if (env==null || env.trim().length()<2 || env.trim().length()>255) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Env Invalid[2~255]");
        }
        if (registryDataList==null || registryDataList.size()==0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "Registry DataList Invalid");
        }
        for (RegistryData registryData: registryDataList) {
            if (registryData.getKey()==null || registryData.getKey().trim().length()<4 || registryData.getKey().trim().length()>255) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "Registry Key Invalid[4~255]");
            }
            if (registryData.getValue()==null || registryData.getValue().trim().length()<4 || registryData.getValue().trim().length()>255) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "Registry Value Invalid[4~255]");
            }
        }

        // fill + add queue
        for (RegistryData registryData: registryDataList) {
            registryData.setBiz(biz);
            registryData.setEnv(env);
        }
        removeQueue.addAll(registryDataList);

        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<Map<String, List<String>>> discovery(String accessToken, String biz, String env, List<String> keys) {

        // valid
        if (this.accessToken!=null && this.accessToken.trim().length()>0 && !this.accessToken.equals(accessToken)) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "AccessToken Invalid");
        }
        if (biz==null || biz.trim().length()<2 || biz.trim().length()>255) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "Biz Invalid[2~255]");
        }
        if (env==null || env.trim().length()<2 || env.trim().length()>255) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "Env Invalid[2~255]");
        }
        if (keys==null || keys.size()==0) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "keys Invalid.");
        }
        for (String key: keys) {
            if (key==null || key.trim().length()<4 || key.trim().length()>255) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "Key Invalid[4~255]");
            }
        }

        Map<String, List<String>> result = new HashMap<String, List<String>>();
        for (String key: keys) {
            RegistryData registryData = new RegistryData();
            registryData.setBiz(biz);
            registryData.setEnv(env);
            registryData.setKey(key);

            List<String> dataList = new ArrayList<>();
            Registry fileRegistry = getFileRegistryData(registryData);
            if (fileRegistry !=null) {
                dataList = fileRegistry.getDataList();
            }

            result.put(key, dataList);
        }

        return new ReturnT<Map<String, List<String>>>(result);
    }

    @Override
    public DeferredResult<ReturnT<String>> monitor(String accessToken, String biz, String env, List<String> keys) {

        // init
        DeferredResult deferredResult = new DeferredResult(30 * 1000L, new ReturnT<>(ReturnT.SUCCESS_CODE, "Monitor timeout, no key updated."));

        // valid
        if (this.accessToken!=null && this.accessToken.trim().length()>0 && !this.accessToken.equals(accessToken)) {
            deferredResult.setResult(new ReturnT<>(ReturnT.FAIL_CODE, "AccessToken Invalid"));
            return deferredResult;
        }
        if (biz==null || biz.trim().length()<4 || biz.trim().length()>255) {
            deferredResult.setResult(new ReturnT<>(ReturnT.FAIL_CODE, "Biz Invalid[4~255]"));
            return deferredResult;
        }
        if (env==null || env.trim().length()<2 || env.trim().length()>255) {
            deferredResult.setResult(new ReturnT<>(ReturnT.FAIL_CODE, "Env Invalid[2~255]"));
            return deferredResult;
        }
        if (keys==null || keys.size()==0) {
            deferredResult.setResult(new ReturnT<>(ReturnT.FAIL_CODE, "keys Invalid."));
            return deferredResult;
        }
        for (String key: keys) {
            if (key==null || key.trim().length()<4 || key.trim().length()>255) {
                deferredResult.setResult(new ReturnT<>(ReturnT.FAIL_CODE, "Key Invalid[4~255]"));
                return deferredResult;
            }
        }

        // monitor by client
        for (String key: keys) {
            String fileName = parseRegistryDataFileName(biz, env, key);

            List<DeferredResult> deferredResultList = registryDeferredResultMap.get(fileName);
            if (deferredResultList == null) {
                deferredResultList = new ArrayList<>();
                registryDeferredResultMap.put(fileName, deferredResultList);
            }

            deferredResultList.add(deferredResult);
        }

        return deferredResult;
    }

    /**
     * update Registry And Message
     */
    private void checkRegistryDataAndSendMessage(RegistryData xxlRpcRegistryData){
        // data json
        List<RegistryData> xxlRpcRegistryDataList = registryDataDao.findData(xxlRpcRegistryData.getBiz(), xxlRpcRegistryData.getEnv(), xxlRpcRegistryData.getKey());
        List<String> valueList = new ArrayList<>();
        if (xxlRpcRegistryDataList !=null && xxlRpcRegistryDataList.size()>0) {
            for (RegistryData dataItem: xxlRpcRegistryDataList) {
                valueList.add(dataItem.getValue());
            }
        }
        String dataJson = JacksonUtil.writeValueAsString(valueList);

        // update registry and message
        Registry xxlRpcRegistry = registryDao.load(xxlRpcRegistryData.getBiz(), xxlRpcRegistryData.getEnv(), xxlRpcRegistryData.getKey());
        boolean needMessage = false;
        if (xxlRpcRegistry == null) {
            xxlRpcRegistry = new Registry();
            xxlRpcRegistry.setBiz(xxlRpcRegistryData.getBiz());
            xxlRpcRegistry.setEnv(xxlRpcRegistryData.getEnv());
            xxlRpcRegistry.setKey(xxlRpcRegistryData.getKey());
            xxlRpcRegistry.setData(dataJson);
            registryDao.add(xxlRpcRegistry);
            needMessage = true;
        } else {

            // check status, locked and disabled not use
            if (xxlRpcRegistry.getStatus() != 0) {
                return;
            }

            if (!xxlRpcRegistry.getData().equals(dataJson)) {
                xxlRpcRegistry.setData(dataJson);
                registryDao.update(xxlRpcRegistry);
                needMessage = true;
            }
        }

        if (needMessage) {
            // sendRegistryDataUpdateMessage (registry update)
            sendRegistryDataUpdateMessage(xxlRpcRegistry);
        }

    }

    // ------------------------ broadcase + file data ------------------------

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private volatile boolean executorStoped = false;
    private volatile List<Integer> readedMessageIds = Collections.synchronizedList(new ArrayList<Integer>());

    private volatile LinkedBlockingQueue<RegistryData> registryQueue = new LinkedBlockingQueue<RegistryData>();
    private volatile LinkedBlockingQueue<RegistryData> removeQueue = new LinkedBlockingQueue<RegistryData>();
    private Map<String, List<DeferredResult>> registryDeferredResultMap = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {

        // valid
        if (registryDataFilePath==null || registryDataFilePath.trim().length()==0) {
            throw new RuntimeException("xxl-rpc, registryDataFilePath empty.");
        }

        /**
         * registry registry data         (client-num/10 s)
         */
        for (int i = 0; i < 10; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    while (!executorStoped) {
                        try {
                            RegistryData registryData = registryQueue.take();
                            if (registryData !=null) {

                                // refresh or add
                                int ret = registryDataDao.refresh(registryData);
                                if (ret == 0) {
                                    registryDataDao.add(registryData);
                                }

                                // valid file status
                                Registry fileRegistry = getFileRegistryData(registryData);
                                if (fileRegistry == null) {
                                    // go on
                                } else if (fileRegistry.getStatus() != 0) {
                                    continue;     // "Status limited."
                                } else {
                                    if (fileRegistry.getDataList().contains(registryData.getValue())) {
                                        continue;     // "Repeated limited."
                                    }
                                }

                                // checkRegistryDataAndSendMessage
                                checkRegistryDataAndSendMessage(registryData);
                            }
                        } catch (Exception e) {
                            if (!executorStoped) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                }
            });
        }

        /**
         * remove registry data         (client-num/start-interval s)
         */
        for (int i = 0; i < 10; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    while (!executorStoped) {
                        try {
                            RegistryData registryData = removeQueue.take();
                            if (registryData != null) {

                                // delete
                                registryDataDao.deleteDataValue(registryData.getBiz(), registryData.getEnv(), registryData.getKey(), registryData.getValue());

                                // valid file status
                                Registry fileRegistry = getFileRegistryData(registryData);
                                if (fileRegistry == null) {
                                    // go on
                                } else if (fileRegistry.getStatus() != 0) {
                                    continue;   // "Status limited."
                                } else {
                                    if (!fileRegistry.getDataList().contains(registryData.getValue())) {
                                        continue;   // "Repeated limited."
                                    }
                                }

                                // checkRegistryDataAndSendMessage
                                checkRegistryDataAndSendMessage(registryData);
                            }
                        } catch (Exception e) {
                            if (!executorStoped) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                }
            });
        }

        /**
         * broadcast new one registry-data-file     (1/1s)
         *
         * clean old message   (1/10s)
         */
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!executorStoped) {
                    try {
                        // new message, filter readed
                        List<RegistryMessage> messageList = registryMessageDao.findMessage(readedMessageIds);
                        if (messageList!=null && messageList.size()>0) {
                            for (RegistryMessage message: messageList) {
                                readedMessageIds.add(message.getId());

                                if (message.getType() == 0) {   // from registry、add、update、delete，ne need sync from db, only write

                                    Registry registry = JacksonUtil.readValue(message.getData(), Registry.class);

                                    // process data by status
                                    if (registry.getStatus() == 1) {
                                        // locked, not updated
                                    } else if (registry.getStatus() == 2) {
                                        // disabled, write empty
                                        registry.setData(JacksonUtil.writeValueAsString(new ArrayList<String>()));
                                    } else {
                                        // default, sync from db （already sync before message, only write）
                                    }

                                    // sync file
                                    setFileRegistryData(registry);
                                }
                            }
                        }

                        // clean old message;
                        if ( (System.currentTimeMillis()/1000) % registryBeatTime ==0) {
                            registryMessageDao.cleanMessage(registryBeatTime);
                            readedMessageIds.clear();
                        }
                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });

        /**
         *  clean old registry-data     (1/10s)
         *
         *  sync total registry-data db + file      (1+N/10s)
         *
         *  clean old registry-data file
         */
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!executorStoped) {

                    // align to beat-time
                    try {
                        long sleepSecond = registryBeatTime - (System.currentTimeMillis()/1000)%registryBeatTime;
                        if (sleepSecond>0 && sleepSecond<registryBeatTime) {
                            TimeUnit.SECONDS.sleep(sleepSecond);
                        }
                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }

                    try {
                        // clean old registry-data in db
                        registryDataDao.cleanData(registryBeatTime * 3);

                        // sync registry-data, db + file
                        int offset = 0;
                        int pagesize = 1000;
                        List<String> registryDataFileList = new ArrayList<>();

                        List<Registry> registryList = registryDao.pageList(offset, pagesize, null, null, null);
                        while (registryList!=null && registryList.size()>0) {

                            for (Registry registryItem: registryList) {

                                // process data by status
                                if (registryItem.getStatus() == 1) {
                                    // locked, not updated
                                } else if (registryItem.getStatus() == 2) {
                                    // disabled, write empty
                                    String dataJson = JacksonUtil.writeValueAsString(new ArrayList<String>());
                                    registryItem.setData(dataJson);
                                } else {
                                    // default, sync from db
                                    List<RegistryData> xxlRpcRegistryDataList = registryDataDao.findData(registryItem.getBiz(), registryItem.getEnv(), registryItem.getKey());
                                    List<String> valueList = new ArrayList<String>();
                                    if (xxlRpcRegistryDataList !=null && xxlRpcRegistryDataList.size()>0) {
                                        for (RegistryData dataItem: xxlRpcRegistryDataList) {
                                            valueList.add(dataItem.getValue());
                                        }
                                    }
                                    String dataJson = JacksonUtil.writeValueAsString(valueList);

                                    // check update, sync db
                                    if (!registryItem.getData().equals(dataJson)) {
                                        registryItem.setData(dataJson);
                                        registryDao.update(registryItem);
                                    }
                                }

                                // sync file
                                String registryDataFile = setFileRegistryData(registryItem);

                                // collect registryDataFile
                                registryDataFileList.add(registryDataFile);
                            }


                            offset += 1000;
                            registryList = registryDao.pageList(offset, pagesize, null, null, null);
                        }

                        // clean old registry-data file
                        cleanFileRegistryData(registryDataFileList);

                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    try {
                        TimeUnit.SECONDS.sleep(registryBeatTime);
                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });


    }

    @Override
    public void destroy() throws Exception {
        executorStoped = true;
        executorService.shutdownNow();
    }


    // ------------------------ file opt ------------------------

    // get
    public Registry getFileRegistryData(RegistryData registryData){

        // fileName
        String fileName = parseRegistryDataFileName(registryData.getBiz(), registryData.getEnv(), registryData.getKey());

        // read
        Properties prop = PropUtil.loadProp(fileName);
        if (prop!=null) {
            Registry fileRegistry = new Registry();
            fileRegistry.setData(prop.getProperty("data"));
            fileRegistry.setStatus(Integer.valueOf(prop.getProperty("status")));
            fileRegistry.setDataList(JacksonUtil.readValue(fileRegistry.getData(), List.class));
            return fileRegistry;
        }
        return null;
    }
    private String parseRegistryDataFileName(String biz, String env, String key){
        // fileName
        String fileName = registryDataFilePath
                .concat(File.separator).concat(biz)
                .concat(File.separator).concat(env)
                .concat(File.separator).concat(key)
                .concat(".properties");
        return fileName;
    }

    // set
    public String setFileRegistryData(Registry registry){

        // fileName
        String fileName = parseRegistryDataFileName(registry.getBiz(), registry.getEnv(), registry.getKey());

        // valid repeat update
        Properties existProp = PropUtil.loadProp(fileName);
        if (existProp != null
                && existProp.getProperty("data").equals(registry.getData())
                && existProp.getProperty("status").equals(String.valueOf(registry.getStatus()))
                ) {
            return new File(fileName).getPath();
        }

        // write
        Properties prop = new Properties();
        prop.setProperty("data", registry.getData());
        prop.setProperty("status", String.valueOf(registry.getStatus()));

        PropUtil.writeProp(prop, fileName);

        logger.info(">>>>>>>>>>> xxl-rpc, setFileRegistryData: biz={}, env={}, key={}, data={}"
                , registry.getBiz(), registry.getEnv(), registry.getKey(), registry.getData());


        // broadcast monitor client
        List<DeferredResult> deferredResultList = registryDeferredResultMap.get(fileName);
        if (deferredResultList != null) {
            registryDeferredResultMap.remove(fileName);
            for (DeferredResult deferredResult: deferredResultList) {
                deferredResult.setResult(new ReturnT<>(ReturnT.SUCCESS_CODE, "Monitor key update."));
            }
        }

        return new File(fileName).getPath();
    }
    // clean
    public void cleanFileRegistryData(List<String> registryDataFileList){
        filterChildPath(new File(registryDataFilePath), registryDataFileList);
    }

    public void filterChildPath(File parentPath, final List<String> registryDataFileList){
        if (!parentPath.exists() || parentPath.list()==null || parentPath.list().length==0) {
            return;
        }
        File[] childFileList = parentPath.listFiles();
        for (File childFile: childFileList) {
            if (childFile.isFile() && !registryDataFileList.contains(childFile.getPath())) {
                childFile.delete();

                logger.info(">>>>>>>>>>> xxl-rpc, cleanFileRegistryData, RegistryData Path={}", childFile.getPath());
            }
            if (childFile.isDirectory()) {
                if (parentPath.listFiles()!=null && parentPath.listFiles().length>0) {
                    filterChildPath(childFile, registryDataFileList);
                } else {
                    childFile.delete();
                }

            }
        }

    }

}
