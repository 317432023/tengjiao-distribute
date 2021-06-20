package com.tengjiao.distribute.rpc.remote.net.impl.netty.client;

import com.tengjiao.distribute.rpc.remote.invoker.RpcInvoker;
import com.tengjiao.distribute.rpc.remote.net.param.Beat;
import com.tengjiao.distribute.rpc.remote.net.param.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rpc netty client handler
 *
 * @author
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
	private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);


	private RpcInvoker rpcInvoker;
	private NettyConnectClient nettyConnectClient;
	public NettyClientHandler(final RpcInvoker rpcInvoker, NettyConnectClient nettyConnectClient) {
		this.rpcInvoker = rpcInvoker;
		this.nettyConnectClient = nettyConnectClient;
	}


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {

		// notify response
		rpcInvoker.notifyInvokerFuture(rpcResponse.getRequestId(), rpcResponse);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error(">>>>>>>>>>> netty client caught exception", cause);
		ctx.close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent){
			/*ctx.channel().close();      // close idle channel
			logger.debug(">>>>>>>>>>> netty client close an idle channel.");*/

			nettyConnectClient.send(Beat.BEAT_PING);	// beat N, close if fail(may throw error)
			logger.debug(">>>>>>>>>>> netty client send beat-ping.");

		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

}
