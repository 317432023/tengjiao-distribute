package com.tengjiao.distribute.rpc.remote.net.impl.netty_http.client;


import com.tengjiao.distribute.rpc.remote.net.Client;
import com.tengjiao.distribute.rpc.remote.net.common.ConnectClient;
import com.tengjiao.distribute.rpc.remote.net.param.RpcRequest;

/**
 * netty_http client
 *
 * @author
 */
public class NettyHttpClient extends Client {

    private Class<? extends ConnectClient> connectClientImpl = NettyHttpConnectClient.class;

    @Override
    public void asyncSend(String address, RpcRequest rpcRequest) throws Exception {
        ConnectClient.asyncSend(rpcRequest, address, connectClientImpl, rpcReferenceBean);
    }

}
