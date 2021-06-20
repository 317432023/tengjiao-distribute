package com.tengjiao.distribute.rpc.remote.net.impl.netty.client;

import com.tengjiao.distribute.rpc.remote.net.Client;
import com.tengjiao.distribute.rpc.remote.net.common.ConnectClient;
import com.tengjiao.distribute.rpc.remote.net.param.RpcRequest;

/**
 * netty client
 *
 * @author
 */
public class NettyClient extends Client {

	private Class<? extends ConnectClient> connectClientImpl = NettyConnectClient.class;

	@Override
	public void asyncSend(String address, RpcRequest rpcRequest) throws Exception {
		ConnectClient.asyncSend(rpcRequest, address, connectClientImpl, rpcReferenceBean);
	}

}
