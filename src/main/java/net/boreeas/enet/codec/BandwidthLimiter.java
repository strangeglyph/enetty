package net.boreeas.enet.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.AllArgsConstructor;
import net.boreeas.enet.commands.ENetCommand;

/**
 * Created by malte on 12/18/14.
 */
@AllArgsConstructor
public class BandwidthLimiter extends ChannelOutboundHandlerAdapter {

    private PeerMap peerMap;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if (msg instanceof ENetCommand && bandwidthExceeded((ENetCommand) msg)) {
            return;
        }

        ctx.writeAndFlush(msg, promise);
    }

    public boolean bandwidthExceeded(ENetCommand msg) {
        return false; // TODO throttle
    }
}
