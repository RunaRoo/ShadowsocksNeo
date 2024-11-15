package server.chananelHandler.inbound;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.chananelHandler.ExceptionDuplexHandler;
import server.config.ServerConfig;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static server.config.ServerContextConstant.CLIENT_CHANNEL;
import static server.config.ServerContextConstant.REMOTE_CHANNEL;


public class UdpProxyInHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LoggerFactory.getLogger(UdpProxyInHandler.class);
    private final AtomicInteger requestIdGenerator = new AtomicInteger(0);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        InetSocketAddress remoteAddress = msg.sender();
        // Get client address

        // Create a new Bootstrap instance for each incoming packet
        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(ctx.channel().eventLoop())
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(0, 0, ServerConfig.serverConfig.getRemoteIdle(), TimeUnit.SECONDS))
                                .addLast(new SimpleChannelInboundHandler<DatagramPacket>() { // Use DatagramPacket here
                            @Override
                            protected void channelRead0(ChannelHandlerContext remoteCtx, DatagramPacket msg) throws Exception {
                            // Send data back to client
                            ctx.channel().writeAndFlush(new DatagramPacket(msg.content().retain(), remoteAddress));
                        }

                            @Override
                            public void channelInactive(ChannelHandlerContext remoteCtx) {
                            // Handle remote channel inactive (optional)
                            logger.debug("Remote channel [{}] is inactive", remoteCtx.channel().id());
                        }
                        })
                                .addLast(new ExceptionDuplexHandler());
                    }
                });

        // Generate a unique request ID (optional)
        int requestId = requestIdGenerator.incrementAndGet();

        // Connect to the remote address with the Bootstrap
        bootstrap.connect(remoteAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                // Get the actual DatagramChannel from the future
                DatagramChannel remoteChannel = (DatagramChannel) future.channel();
                // Send the received data to the remote address
                remoteChannel.writeAndFlush(new DatagramPacket(msg.content().retain(), msg.sender()));
                logger.debug("Sent UDP request [{}] to remote: {}", requestId, remoteAddress);
            } else {
                logger.error("Failed to connect to remote: " + remoteAddress, future.cause());
                // Handle connection failure (optional)
            }
        });
    }
}




