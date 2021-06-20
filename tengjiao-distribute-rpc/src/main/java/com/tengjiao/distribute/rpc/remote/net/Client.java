package com.tengjiao.distribute.rpc.remote.net;

import com.tengjiao.distribute.rpc.remote.invoker.reference.RpcReferenceBean;
import com.tengjiao.distribute.rpc.remote.net.param.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * i client
 * @author
 */
public abstract class Client {
	protected static final Logger logger = LoggerFactory.getLogger(Client.class);


	// ---------------------- init ----------------------

	protected volatile RpcReferenceBean rpcReferenceBean;

	public void init(RpcReferenceBean rpcReferenceBean) {
		this.rpcReferenceBean = rpcReferenceBean;
	}


    // ---------------------- send ----------------------

	/**
	 * async send, bind requestId and future-response
	 *
	 * @param address
	 * @param rpcRequest
	 * @return
	 * @throws Exception
	 */
	public abstract void asyncSend(String address, RpcRequest rpcRequest) throws Exception;

}
