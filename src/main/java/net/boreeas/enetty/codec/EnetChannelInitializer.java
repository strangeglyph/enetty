package net.boreeas.enetty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.AllArgsConstructor;

import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * Created by malte on 12/9/14.
 */
@AllArgsConstructor
public class EnetChannelInitializer extends ChannelInitializer {

    private static final EventExecutorGroup group = new DefaultEventExecutorGroup(16);

    private ToIntFunction<ByteBuf> checksumCallback;
    private Function<Peer, PeerInputHandler> newConnectionCallback;

    @Override
    protected void initChannel(Channel ch) throws Exception {
        PeerMap peerMap = new PeerMap();

        ch.pipeline()
                // outbound
                .addLast("encoder", new ENetCommandEncoder(peerMap, checksumCallback))
                .addLast("outgoingBandwidthLimiter", new BandwidthLimiter(peerMap))
                .addLast("outgoingPacketThrottle", new PacketThrottle(peerMap))
                .addLast("outgoingReliableAckHandler", new OutgoingAckHandler(peerMap, ch))

                // inbound
                .addLast("decoder", new ENetCommandDecoder(peerMap, checksumCallback))
                .addLast("incomingCommandBackend", new IncomingCommandBackend(peerMap, newConnectionCallback));
                ;
    }
}
