package com.tengjiao.distribute.rpc.remote.net.impl.netty.server;

import com.tengjiao.distribute.rpc.remote.net.Server;
import com.tengjiao.distribute.rpc.remote.net.impl.netty.codec.NettyDecoder;
import com.tengjiao.distribute.rpc.remote.net.impl.netty.codec.NettyEncoder;
import com.tengjiao.distribute.rpc.remote.net.param.Beat;
import com.tengjiao.distribute.rpc.remote.net.param.RpcRequest;
import com.tengjiao.distribute.rpc.remote.net.param.RpcResponse;
import com.tengjiao.distribute.rpc.remote.provider.RpcProvider;
import com.tengjiao.distribute.rpc.util.ThreadPoolUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * netty rpc server
 *
 * @author
 */
public class NettyServer extends Server {

    private Thread thread;

    @Override
    public void start(final RpcProvider rpcProvider) throws Exception {

        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                // param
                final ThreadPoolExecutor serverHandlerPool = ThreadPoolUtil.makeServerThreadPool(
                        NettyServer.class.getSimpleName(),
                        rpcProvider.getCorePoolSize(),
                        rpcProvider.getMaxPoolSize());
                EventLoopGroup bossGroup = new NioEventLoopGroup();
                EventLoopGroup workerGroup = new NioEventLoopGroup();

                try {
                    // start server
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                public void initChannel(SocketChannel channel) throws Exception {
                                    channel.pipeline()
                                            .addLast(new IdleStateHandler(0,0, Beat.BEAT_INTERVAL*3, TimeUnit.SECONDS))     // beat 3N, close if idle
                                            .addLast(new NettyDecoder(RpcRequest.class, rpcProvider.getSerializerInstance()))
                                            .addLast(new NettyEncoder(RpcResponse.class, rpcProvider.getSerializerInstance()))
                                            .addLast(new NettyServerHandler(rpcProvider, serverHandlerPool));
                                }
                            })
                            .childOption(ChannelOption.TCP_NODELAY, true)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);

                    // bind
                    ChannelFuture future = bootstrap.bind(rpcProvider.getPort()).sync();

                    logger.info(">>>>>>>>>>> remoting server start success, nettype = {}, port = {}", NettyServer.class.getName(), rpcProvider.getPort());
                    onStarted();

                    // wait util stop
                    future.channel().closeFuture().sync();

                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        logger.info(">>>>>>>>>>> remoting server stop.");
                    } else {
                        logger.error(">>>>>>>>>>> remoting server error.", e);
                    }
                } finally {

                    // stop
                    try {
                        serverHandlerPool.shutdown();    // shutdownNow
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    try {
                        workerGroup.shutdownGracefully();
                        bossGroup.shutdownGracefully();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }

                }
            }
        });
        thread.setDaemon(true);
        thread.start();

    }

    @Override
    public void stop() throws Exception {

        // destroy server thread
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }

        // on stop
        onStoped();
        logger.info(">>>>>>>>>>> remoting server destroy success.");
    }

}
