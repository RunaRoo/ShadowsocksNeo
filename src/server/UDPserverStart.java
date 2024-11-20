package client;

import com.google.common.net.HostAndPort;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.chananelHandler.ExceptionDuplexHandler;
import server.chananelHandler.inbound.CryptInitInHandler;
import server.chananelHandler.inbound.DecodeSSHandler;
import server.chananelHandler.inbound.UdpProxyInHandler;
import server.chananelHandler.outbound.EncodeSSOutHandler;
import server.menu.ServerHelper;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class UDPserverStart {
    private static final Logger logger = LoggerFactory.getLogger(UDPserverStart.class);

    private static server.config.ServerConfig ServerConfig;
    private static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(server.config.ServerConfig.serverConfig.getBossThreadNumber());

    private static final Bootstrap serverBootstrap = new Bootstrap();

    public static void main(String[] args) throws InterruptedException {
        ServerHelper.useHelp(args);
        startupServer();
    }

    private static void startupServer() throws InterruptedException {
        serverBootstrap.group(eventLoopGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    protected void initChannel(DatagramChannel ch) {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(1000, 1000, ServerConfig.serverConfig.getClientIdle(), TimeUnit.SECONDS))
                                .addLast(new CryptInitInHandler()) // Assuming CryptInitInHandler works with UDP
                                .addLast(new DecodeSSHandler()) // Assuming DecodeSSHandler works with UDP
                                .addLast(new UdpProxyInHandler()) // New UDP handler
                                .addLast(new EncodeSSOutHandler())
                                .addLast(new ExceptionDuplexHandler());
                    }
                });

        InetSocketAddress bindAddress = getAddress(ServerConfig.serverConfig.getLocalAddress());
        ChannelFuture channelFuture = serverBootstrap.bind(bindAddress).sync();
        logger.info("shadowsocks server [udp] running at {}", bindAddress);
        channelFuture.channel().closeFuture().sync();
    }

    public static InetSocketAddress getAddress(String address) {
        HostAndPort hostAndPort = HostAndPort.fromString(address);
        return new InetSocketAddress(hostAndPort.getHost(), hostAndPort.getPort());
    }
}
