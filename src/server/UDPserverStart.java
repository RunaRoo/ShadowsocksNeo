package server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;

import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.FutureListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.chananelHandler.ExceptionDuplexHandler;
import server.chananelHandler.inbound.CryptInitInHandler;
import server.chananelHandler.inbound.DecodeSSHandler;
import server.chananelHandler.inbound.UdpProxyInHandler;
import server.chananelHandler.outbound.EncodeSSOutHandler;
import server.config.ServerConfig;
import server.menu.ServerHelper;

import java.net.InetSocketAddress;

public class ServerStart {

    private static final Logger logger = LoggerFactory.getLogger(ServerStart.class);

    private static server.config.ServerConfig ServerConfig;
    private static final EventLoopGroup bossLoopGroup = new NioEventLoopGroup(server.config.ServerConfig.serverConfig.getBossThreadNumber());
    private static final EventLoopGroup worksLoopGroup = new NioEventLoopGroup(server.config.ServerConfig.serverConfig.getWorkersThreadNumber());

    private static final Bootstrap serverBootstrap = new Bootstrap();

    public static void main(String[] args) throws InterruptedException {
        ServerHelper.useHelp(args);
        startupServer();
    }

    private static void startupServer() throws InterruptedException {
        serverBootstrap.group(bossLoopGroup, worksLoopGroup)
                .channel(NioDatagramChannel.class)
                .childHandler(new ChannelInitializer<DatagramChannel>() {
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


    private static InetSocketAddress getAddress(String address) {
        if (!address.contains(":")) {
            throw new IllegalArgumentException("illegal address: " + address);
        }
        String host;
        int port;
        if (address.startsWith("[")) {
            // IPv6 address
            int closingBracketIndex = address.indexOf("]");
            if (closingBracketIndex == -1) {
                throw new IllegalArgumentException("illegal address: " + address);
            }
            host = address.substring(1, closingBracketIndex);
            port = Integer.parseInt(address.substring(closingBracketIndex + 2));
        } else {
            // IPv4 address
            host = address.substring(0, address.indexOf(":"));
            port = Integer.parseInt(address.substring(address.indexOf(":") + 1));
        }
        return new InetSocketAddress(host, port);
    }

}


