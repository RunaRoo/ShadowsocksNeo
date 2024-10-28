package server;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import server.menu.ServerHelper;
import server.chananelHandler.ExceptionDuplexHandler;
import server.chananelHandler.inbound.CryptInitInHandler;
import server.chananelHandler.inbound.DecodeSSHandler;
import server.chananelHandler.inbound.TcpProxyInHandler;
import server.chananelHandler.outbound.EncodeSSOutHandler;
import server.config.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.security.Security;
import java.util.concurrent.TimeUnit;
//import java.net.UnknownHostException;  //Delete if something go wrong
//todo: implement UDP server / client (Present on V3 P1)

/**
 * server start
 *
 * @author zk
 * @since 2018/8/11
 */
public class ServerStart {

    private static final Logger logger = LoggerFactory.getLogger(ServerStart.class);

    /**
     * boosLoopGroup
     */
    private static final EventLoopGroup bossLoopGroup = new NioEventLoopGroup(ServerConfig.serverConfig.getBossThreadNumber());
    /**
     * worksLoopGroup
     */
    private static final EventLoopGroup worksLoopGroup = new NioEventLoopGroup(ServerConfig.serverConfig.getWorkersThreadNumber());
    /**
     * serverBootstrap
     */
    private static final ServerBootstrap serverBootstrap = new ServerBootstrap();

    public static void main(String[] args) throws InterruptedException {
        Security.insertProviderAt(new BouncyCastleProvider(), 2);
        ServerHelper.useHelp(args);
        startupServer();
    }

    private static void startupServer() throws InterruptedException {
        serverBootstrap.group(bossLoopGroup, worksLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
                    protected void initChannel(Channel ch) {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(0, 0, ServerConfig.serverConfig.getClientIdle(), TimeUnit.SECONDS))
                                .addLast(new CryptInitInHandler())
                                .addLast(new DecodeSSHandler())
                                .addLast(new TcpProxyInHandler())
                                .addLast(new EncodeSSOutHandler())
                                .addLast(new ExceptionDuplexHandler());
                    }
                });
        InetSocketAddress bindAddress = getAddress(ServerConfig.serverConfig.getLocalAddress());
        ChannelFuture channelFuture = serverBootstrap.bind(bindAddress).sync();
        logger.info("We living on a mad world");
        logger.info("ShadowsocksNeo server [tcp] running at {}", bindAddress);
        channelFuture.channel().closeFuture().sync();
    }

    //Fix1: Now support both ipv4 and ipv6
    //todo: Apply to NeoClient, Maybe get rid of "IpAddress" library :P
    private static InetSocketAddress getAddress(String address) {
        String host;
        int port;

        if (address.startsWith("[")) {
            // IPv6
            int closingBracketIndex = address.indexOf(']');
            if (closingBracketIndex == -1) {
                throw new IllegalArgumentException("illegal address: " + address);
            }
            host = address.substring(1, closingBracketIndex);
            port = Integer.parseInt(address.substring(closingBracketIndex + 2));
        } else {
            // IPv4
            int colonIndex = address.lastIndexOf(':');
            if (colonIndex == -1) {
                throw new IllegalArgumentException("illegal address: " + address);
            }
            host = address.substring(0, colonIndex);
            port = Integer.parseInt(address.substring(colonIndex + 1));
        }

        return new InetSocketAddress(host, port);
    }
}

// Legacy IPaddress loader
/*
    private static InetSocketAddress getAddress(String address) {
        if (!address.contains(":")) {
            throw new IllegalArgumentException("illegal address: " + address);
        }
        String host = address.substring(0, address.indexOf(":"));
        int port = Integer.parseInt(address.substring(address.indexOf(":") + 1));
        return new InetSocketAddress(host, port);
    }
 */

