package net.boreeas.enetty.codec;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.util.function.ToIntFunction;

/**
 * Created by malte on 12/9/14.
 */
@NoArgsConstructor
@Log4j
public class ENetClient {

    private int bindPort = 0;
    @Setter private ToIntFunction<ByteBuf> checksumCallback;

    public ENetClient(int bindPort) {
        this.bindPort = bindPort;
    }

    public void start() throws InterruptedException {

        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            bootstrap.group(group).channel(NioDatagramChannel.class);

            if (bindPort != 0) {
                bootstrap.localAddress(bindPort);
            }

            bootstrap.handler(new EnetChannelInitializer(checksumCallback));



            ChannelFuture f = bootstrap.bind().sync();
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
}
