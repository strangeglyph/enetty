package net.boreeas.enet.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.AllArgsConstructor;
import net.boreeas.enet.commands.ENetCommand;

/**
 * Created by malte on 12/19/14.
 */
@AllArgsConstructor
public class PacketThrottle extends ChannelOutboundHandlerAdapter {

    // Maximum throttle score. Should be a prime for uniform distribution
    private static final int MAX_THROTTLE = 31;
    // Since MAX_THROTTLE is prime, and the throttle value is calculated as (value + increment) % max
    // this is going to hit every value between 0 an MAX_THROTTLE once, and since this is slightly
    // above half MAX_THROTTLE, it is going to alternate between dropping and not dropping
    // (for a throttle value of MAX_THROTTLE / 2)
    private static final int THROTTLE_VALUE_INCREMENT = (MAX_THROTTLE / 2) + 1;

    private PeerMap peers;


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if (msg instanceof ENetCommand && packetThrottled((ENetCommand) msg)) {
            return;
        }

        ctx.writeAndFlush(msg, promise);
    }

    public boolean packetThrottled(ENetCommand msg) {
        Peer peer = peers.getByIncoming(msg.getHeader().getPeerId());
        peer.setCurrentThrottleScore((peer.getCurrentThrottleScore() + THROTTLE_VALUE_INCREMENT) % MAX_THROTTLE);

        return peer.getCurrentThrottleScore() <= peer.getCurrentThrottleValue();
    }
}
