package com.tengjiao.distribute.rpc.registry;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 注册器接口
 * @author
 */
public abstract class Register {
    /**
     * 启动
     */
    public abstract void start(Map<String, String> param);

    /**
     * 停止
     */
    public abstract void stop();


    /**
     * 注册
     *
     * @param keys
     * @param value ip:port
     * @return
     */
    public abstract boolean registry(Set<String> keys, String value);


    /**
     * 摘除
     *
     * @param keys
     * @param value
     * @return
     */
    public abstract boolean remove(Set<String> keys, String value);

    /**
     * 查找
     *
     * @param key
     * @return ip:port
     */
    public abstract TreeSet<String> discovery(String key);

    /**
     * 查找
     *
     * @param keys
     * @return
     */
    public abstract Map<String, TreeSet<String>> discovery(Set<String> keys);

}
