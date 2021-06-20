package com.tengjiao.distribute.rpc.remote.net.impl.netty_http.client;

import com.tengjiao.distribute.rpc.RpcException;
import com.tengjiao.distribute.rpc.remote.invoker.RpcInvoker;
import com.tengjiao.distribute.rpc.remote.net.param.Beat;
import com.tengjiao.distribute.rpc.remote.net.param.RpcResponse;
import com.tengjiao.distribute.rpc.serialize.Serializer;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * netty_http
 *
 * @author xuxueli 2015-11-24 22:25:15
 */
public class NettyHttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private static final Logger logger = LoggerFactory.getLogger(NettyHttpClientHandler.class);


    private RpcInvoker rpcInvoker;
    private Serializer serializer;
    private NettyHttpConnectClient nettyHttpConnectClient;
    public NettyHttpClientHandler(final RpcInvoker rpcInvoker, Serializer serializer, final NettyHttpConnectClient nettyHttpConnectClient) {
        this.rpcInvoker = rpcInvoker;
        this.serializer = serializer;
        this.nettyHttpConnectClient = nettyHttpConnectClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {

        // valid status
        if (!HttpResponseStatus.OK.equals(msg.status())) {
            throw new RpcException("response status invalid.");
        }

        // response parse
        byte[] responseBytes = ByteBufUtil.getBytes(msg.content());

        // valid length
        if (responseBytes.length == 0) {
            throw new RpcException("response data empty.");
        }

        // response deserialize
        RpcResponse rpcResponse = (RpcResponse) serializer.deserialize(responseBytes, RpcResponse.class);

        // notify response
        rpcInvoker.notifyInvokerFuture(rpcResponse.getRequestId(), rpcResponse);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //super.exceptionCaught(ctx, cause);
        logger.error(">>>>>>>>>>> netty_http client caught exception", cause);
        ctx.close();
    }

    /*@Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // retry
        super.channelInactive(ctx);
    }*/

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            /*ctx.channel().close();      // close idle channel
            logger.debug(">>>>>>>>>>> netty_http client close an idle channel.");*/

            nettyHttpConnectClient.send(Beat.BEAT_PING);    // beat N, close if fail(may throw error)
            logger.debug(">>>>>>>>>>> netty_http client send beat-ping.");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
