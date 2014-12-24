package net.boreeas.enet.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j;
import net.boreeas.enet.commands.*;

/**
 * Created by malte on 12/9/14.
 */
@Log4j
public class EnetIncomingBackend extends ChannelInboundHandlerAdapter {

    private ENetOutgoingBackend backend;

    public EnetIncomingBackend(ENetOutgoingBackend backend) {
        this.backend = backend;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ENetCommand) {
            ENetCommand cmd = (ENetCommand) msg;
            switch (((ENetCommand) msg).getCommand()) {

                case ACKNOWLEDGE:
                    backend.markAcknowledged(ctx, (Acknowledge) cmd);
                    break;
                case CONNECT:
                    backend.acceptConnection(ctx, (Connect) cmd);
                    break;
                case VERIFY_CONNECT:
                    backend.verifyConnection(ctx, (VerifyConnect) cmd);
                    break;
                case DISCONNECT:
                    backend.onDisconnect(ctx, (Disconnect) cmd);
                    break;
                case PING:
                    backend.pinged(ctx, (Ping) cmd);
                    break;
                case SEND_RELIABLE:
                    backend.receiveReliable(ctx, (SendReliable) cmd);
                    break;
                case SEND_UNRELIABLE:
                    backend.receiveUnreliable(ctx, (SendUnreliable) cmd);
                    break;
                case SEND_FRAGMENT:
                    backend.receiveFragment(ctx, (SendUnreliable) cmd);
                    break;
                case SEND_UNSEQUENCED:
                    backend.receiveUnsequenced(ctx, (SendUnsequenced) cmd);
                    break;
                case BANDWIDTH_LIMIT:
                    backend.adjustBandwidthLimit(ctx, (BandwidthLimit) cmd);
                    break;
                case THROTTLE_CONFIGURE:
                    bacend.adjustThrottle(ctx, (ThrottleConfigure) cmd);
                    break;
            }
        }

        ctx.fireChannelRead(msg);
    }
}
