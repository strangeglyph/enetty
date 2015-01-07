package net.boreeas.enet.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.boreeas.enet.commands.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @author Malte Schütze
 */
public class IncomingCommandBackend extends ChannelInboundHandlerAdapter {
    private AtomicInteger nextPeerId = new AtomicInteger(0);

    private PeerMap peers;
    private Function<Peer, PeerInputHandler> newConnectionCallback;

    private Map<Peer, PeerInputHandler> peerInputHandlers = new HashMap<>();

    public IncomingCommandBackend(PeerMap peerMap, Function<Peer, PeerInputHandler> newConnectionCallback) {
        this.peers = peerMap;
        this.newConnectionCallback = newConnectionCallback;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ENetCommand)) {
            ctx.fireChannelRead(msg);
            return;
        }

        ENetCommand cmd = (ENetCommand) msg;
        int peerId = cmd.getHeader().getPeerId();
        Peer peer = peers.getByOutgoing(peerId);

        if (cmd instanceof Acknowledge) {
            // TODO RTT measurement
            peer.getAckPending().remove(((Acknowledge) cmd).getReceivedSeqNum());
        } else if (cmd instanceof BandwidthLimit) {
            // TODO bandwidth limit
        } else if (cmd instanceof Connect) {
            handleConnect(ctx, (Connect) cmd);
        } else if (cmd instanceof Disconnect) {
            peers.remove(peer);
        } else if (cmd instanceof Ping) {
            // Pings are ignored
        } else if (cmd instanceof SendFragment) {
            peer.getENetChannel(cmd.getChannelId()).onFragmentReceived((SendFragment) cmd);
        } else if (cmd instanceof SendReliable) {
            peer.getENetChannel(cmd.getChannelId()).onReliableReceived((SendReliable) cmd);
        } else if (cmd instanceof SendUnreliable) {
            peer.getENetChannel(cmd.getChannelId()).onUnreliableReceived((SendUnreliable) cmd);
        } else if (cmd instanceof SendUnsequenced) {
            peer.onUnsequencedReceived((SendUnsequenced) cmd);
        } else if (cmd instanceof ThrottleConfigure) {
            // TODO Throttle configure
        } else if (cmd instanceof VerifyConnect) {
            handleVerifyConnect(peer, ctx, (VerifyConnect) cmd);
        }

        if (cmd.getSendType() == ENetCommand.SendType.RELIABLE) {
            peer.getENetChannel(cmd.getChannelId()).acknowledge(cmd);
        }

        ctx.fireChannelRead(msg);
    }

    private void handleVerifyConnect(Peer peer, ChannelHandlerContext ctx, VerifyConnect cmd) {
        // TODO verify new params
        peer.setIncomingBandwidth(cmd.getIncomingBandwidth());
        peer.setOutgoingBandwidth(cmd.getOutgoingBandwidth());
        // TODO lookup peer id values in verify
        peer.setIncomingPeerId(cmd.getOutgoingPeerId());
        peer.setPacketThrottleInterval(cmd.getPacketThrottleInterval());
        peer.setPacketThrottleAcceleration(cmd.getPacketThrottleAcceleration());
        peer.setPacketThrottleDeceleration(cmd.getPacketThrottleDeceleration());
        peer.setMtu(cmd.getMtu());
        peer.setChannelCount(cmd.getChannelCount());
        peer.setWindowSize(cmd.getWindowSize());

        // Needs to be added by connect call beforehand: outgoing peer id, netty channel
    }

    private void handleConnect(ChannelHandlerContext ctx, Connect cmd) {
        Peer peer = new Peer();
        peer.setChannel(ctx.channel());

        peer.setCurrentThrottleScore(0);
        peer.setCurrentThrottleValue(0);
        peer.setIncomingBandwidth(cmd.getOutgoingBandwidth());
        peer.setOutgoingBandwidth(cmd.getIncomingBandwidth());
        peer.setIncomingPeerId(cmd.getOutgoingPeerId());
        peer.setOutgoingPeerId(nextPeerId.getAndIncrement());
        peer.setPacketThrottleAcceleration(cmd.getPacketThrottleAcceleration());
        peer.setPacketThrottleDeceleration(cmd.getPacketThrottleDeceleration());
        peer.setPacketThrottleInterval(cmd.getPacketThrottleInterval());
        peer.setSessionId((int) cmd.getSessionId());
        peer.setMtu(cmd.getMtu());
        peer.setChannelCount(cmd.getChannelCount());
        peer.setWindowSize(cmd.getWindowSize());

        peerInputHandlers.put(peer, newConnectionCallback.apply(peer));
        peers.add(peer);

        // TODO actually verify connection parameters
        ENetProtocolHeader header = new ENetProtocolHeader(0, true, peer.getIncomingPeerId(), peer.connectionTime());
        VerifyConnect verification = new VerifyConnect(header, 0xff, 0, peer.getIncomingPeerId(), peer.getMtu(), peer.getWindowSize(), peer.getChannelCount(),
                peer.getIncomingBandwidth(), peer.getOutgoingBandwidth(), peer.getPacketThrottleInterval(), peer.getPacketThrottleAcceleration(), peer.getPacketThrottleDeceleration());

        peer.getENetChannel(0xff).writePacket(verification);
    }
}
