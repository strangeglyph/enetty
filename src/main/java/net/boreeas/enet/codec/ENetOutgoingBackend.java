package net.boreeas.enet.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.boreeas.enet.commands.Acknowledge;

import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;

/**
 * Created by malte on 12/11/14.
 */
public class ENetOutgoingBackend extends ChannelOutboundHandlerAdapter {

    private Timer timer;
    private Set<Integer> pendingAcks = new HashSet<>();

    public void markAcknowledged(ChannelHandlerContext ctx, Acknowledge cmd) {
        pendingAcks.remove(cmd.getReceivedSeqNum());
    }


    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {

        timer.cancel();
        pendingAcks.clear();
    }


    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {

        timer = new Timer();
    }
}
