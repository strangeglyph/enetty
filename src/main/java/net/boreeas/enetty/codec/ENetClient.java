package net.boreeas.enetty.codec;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import net.boreeas.enetty.commands.Connect;
import net.boreeas.enetty.commands.ENetProtocolHeader;

import java.net.InetAddress;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

/**
 * Created by malte on 12/9/14.
 */
@Log4j
public class ENetClient {

    private int bindPort = 0;
    private PeerIdGenerator peerIds = new PeerIdGenerator();
    @Setter private ToIntFunction<ByteBuf> checksumCallback;
    @Setter private Consumer<Peer> newConnectionCallback;
    @Setter @Getter private ConnectionOptions connectionOptions;

    private Channel channel;

    public ENetClient(Consumer<Peer> newConnectionCallback) {
        this(0, newConnectionCallback, new ConnectionOptions());
    }

    public ENetClient(Consumer<Peer> newConnectionCallback, ConnectionOptions connectionOptions) {
        this(0, newConnectionCallback, connectionOptions);
    }

    public ENetClient(int bindPort, Consumer<Peer> newConnectionCallback, ConnectionOptions connectionOptions) {
        this.bindPort = bindPort;
        this.newConnectionCallback = newConnectionCallback;
        this.connectionOptions = connectionOptions;
    }

    public void start() throws InterruptedException {

        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            bootstrap.group(group).channel(NioDatagramChannel.class);

            if (bindPort != 0) {
                bootstrap.localAddress(bindPort);
            }

            bootstrap.handler(new EnetChannelInitializer(checksumCallback, newConnectionCallback, peerIds));


            ChannelFuture f = bootstrap.bind().sync();
            this.channel = f.channel();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();

            try {
                group.terminationFuture().sync();
            } catch (InterruptedException ex) {
                log.warn("NioEventLoopGroup: sync interrupted, returning immediately");
            }
        }

    }

    public Peer connectTo(InetAddress address, int port) {
        return connectTo(address, port, connectionOptions);
    }

    public Peer connectTo(InetAddress address, int port, ConnectionOptions options) {
        Peer peer = new Peer();
        try {
            peer.setPeerId(peerIds.next(1));
        } catch (InterruptedException e) {
            throw new IllegalStateException("Too many connected peers");
        }
        peer.setChannel(channel);
        peer.setAddress(address);
        peer.setPort(port);
        peer.setMtu(options.getMtuDefault());
        peer.setChannelCount(options.getChannelCountDefault());
        peer.setWindowSize(options.getWindowSizeDefault());
        peer.setSessionId(new Random().nextInt());
        peer.setPacketThrottleAcceleration(options.getPacketThrottleAcceleration());
        peer.setPacketThrottleInterval(options.getPacketThrottleIntervalDefault());
        peer.setPacketThrottleDeceleration(options.getPacketThrottleDeceleration());
        peer.setCurrentThrottleValue(options.getPacketThrottleValueDefault());
        peer.setIncomingBandwidth(options.getBandwidthDownstreamDefault());
        peer.setOutgoingBandwidth(options.getBandwidthUpstreamDefault());

        ENetProtocolHeader header = new ENetProtocolHeader(0, true, peer.getOurId(), 0);
        peer.getENetChannel(0xff).writePacket(new Connect(header, 0xff, 0, peer.getPeerId(),
                peer.getMtu(), peer.getWindowSize(), peer.getChannelCount(),
                peer.getIncomingBandwidth(), peer.getOutgoingBandwidth(),
                peer.getPacketThrottleInterval(), peer.getPacketThrottleAcceleration(), peer.getPacketThrottleDeceleration(),
                peer.getSessionId()));

        return peer;
    }
}
