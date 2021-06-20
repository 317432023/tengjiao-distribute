package com.tengjiao.distribute.rpc.remote.invoker.reference.impl;

import com.tengjiao.distribute.rpc.remote.invoker.reference.RpcReferenceBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * rpc reference bean, use by spring xml and annotation (for spring)
 *
 * @author xuxueli 2015-10-29 20:18:32
 */
public class RpcSpringReferenceBean implements FactoryBean<Object>, InitializingBean {


    // ---------------------- util ----------------------

    private RpcReferenceBean rpcReferenceBean;

    @Override
    public void afterPropertiesSet() {

        // init config
        this.rpcReferenceBean = new RpcReferenceBean();
    }


    @Override
    public Object getObject() throws Exception {
        return rpcReferenceBean.getObject();
    }

    @Override
    public Class<?> getObjectType() {
        return rpcReferenceBean.getIface();
    }

    @Override
    public boolean isSingleton() {
        return false;
    }


    /**
     *	public static <T> ClientProxy ClientProxy<T> getFuture(Class<T> type) {
     *		<T> ClientProxy proxy = (<T>) new ClientProxy();
     *		return proxy;
     *	}
     */


}
