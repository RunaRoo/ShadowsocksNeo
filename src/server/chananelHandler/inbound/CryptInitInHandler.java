package server.chananelHandler.inbound;

import cipher.CipherProvider;
import cipher.SSCipher;
import server.config.ServerConfig;
import server.config.ServerContextConstant;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * init crypt
 *
 * @author zk
 * @since 2018/8/11
 */
public class CryptInitInHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (ctx.channel().attr(ServerContextConstant.SERVER_CIPHER).get() == null) {
            initAttribute(ctx);
        }

        super.channelRead(ctx, msg);
    }

    /**
     * init client attribute
     *
     * @param ctx client context
     */
    private void initAttribute(ChannelHandlerContext ctx) {
        // cipher
        SSCipher cipher = CipherProvider.getByName(ServerConfig.serverConfig.getMethod(), ServerConfig.serverConfig.getPassword());
        if (cipher == null) {
            ctx.channel().close();
            throw new IllegalArgumentException("un support server method: " + ServerConfig.serverConfig.getMethod());
        } else {
            ctx.channel().attr(ServerContextConstant.SERVER_CIPHER).set(cipher);
        }
    }
}
