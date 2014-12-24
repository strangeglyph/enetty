package net.boreeas.enet.codec;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.boreeas.enet.commands.ENetCommand;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Malte Schütze
 */
public class OutgoingAckHandler extends ChannelOutboundHandlerAdapter {
    private final ScheduledExecutorService ackTimer = Executors.newScheduledThreadPool(1);

    private final PeerMap peers;
    private final Channel channel;

    public OutgoingAckHandler(PeerMap peerMap, Channel channel) {
        this.peers = peerMap;
        this.channel = channel;
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.disconnect(ctx, promise);
        ackTimer.shutdownNow();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);

        if (msg instanceof ENetCommand && ((ENetCommand) msg).getSendType() == ENetCommand.SendType.RELIABLE) {
            ENetCommand command = (ENetCommand) msg;

            Peer peer = peers.getByIncoming(command.getHeader().getPeerId());
            peer.getAckPending().add(command);

            ackTimer.schedule(() -> {
                if (peer.getAckPending().contains(command)) {
                    ctx.writeAndFlush(msg);
                }
            }, peer.getAckTimeout(), TimeUnit.MILLISECONDS);
        }
    }
}
