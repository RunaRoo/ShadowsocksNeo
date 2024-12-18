package server.chananelHandler.inbound;

import cipher.SSCipher;
import util.ShadowsocksUtils;
import server.config.ServerContextConstant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * decode secret bytes
 *
 * @author zk
 * @since 2018/8/11
 */
public class DecodeSSHandler extends ByteToMessageDecoder {

    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        SSCipher cipher = ctx.channel().attr(ServerContextConstant.SERVER_CIPHER).get();

        byte[] secretBytes = new byte[msg.readableBytes()];
        msg.readBytes(secretBytes);
        byte[] originBytes = cipher.decodeSSBytes(secretBytes);

        if (originBytes != null && originBytes.length != 0) {
            ByteBuf nextMsg = Unpooled.buffer().writeBytes(originBytes);
            // get Ip
            if (ctx.channel().attr(ServerContextConstant.REMOTE_INET_SOCKET_ADDRESS).get() == null) {
                InetSocketAddress inetSocketAddress = ShadowsocksUtils.getIp(nextMsg);
                if (inetSocketAddress == null) {
                    ctx.channel().close();
                    return;
                }

                ctx.channel().attr(ServerContextConstant.REMOTE_INET_SOCKET_ADDRESS).set(inetSocketAddress);
            }
            out.add(nextMsg);
        }
    }
}
