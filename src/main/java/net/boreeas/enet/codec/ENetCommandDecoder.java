package net.boreeas.enet.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.AllArgsConstructor;
import net.boreeas.enet.commands.*;

import java.util.List;
import java.util.function.ToIntFunction;

/**
 * @author Malte Schütze
 */
@AllArgsConstructor
public class ENetCommandDecoder extends ReplayingDecoder<Void> {

    private PeerMap peerLookup;
    private ToIntFunction<ByteBuf> checksumCallback;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> objects) throws Exception {

        int indexBefore = byteBuf.readerIndex(); // For checksum slice below

        ENetProtocolHeader header = new ENetProtocolHeader(byteBuf);

        ENetCommand command;
        switch (CommandId.getById(byteBuf.getByte(byteBuf.readerIndex()) & ENetCommand.COMMAND_MASK)) {
            case ACKNOWLEDGE:
                command = new Acknowledge(byteBuf, header);
                break;
            case CONNECT:
                command = new Connect(byteBuf, header);
                break;
            case VERIFY_CONNECT:
                command = new VerifyConnect(byteBuf, header);
                break;
            case DISCONNECT:
                command = new Disconnect(byteBuf, header);
                break;
            case PING:
                command = new Ping(byteBuf, header);
                break;
            case SEND_RELIABLE:
                command = new SendReliable(byteBuf, header);
                break;
            case SEND_UNRELIABLE:
                command = new SendUnreliable(byteBuf, header);
                break;
            case SEND_FRAGMENT:
                command = new SendFragment(byteBuf, header);
                break;
            case SEND_UNSEQUENCED:
                command = new SendUnsequenced(byteBuf, header);
                break;
            case BANDWIDTH_LIMIT:
                command = new BandwidthLimit(byteBuf, header);
                break;
            case THROTTLE_CONFIGURE:
                command = new ThrottleConfigure(byteBuf, header);
                break;
            default:
                throw new IllegalArgumentException("Bad CommandId");
        }


        // Check checksum
        int checksum = command.getHeader().getChecksum();
        int sessionId = peerLookup.getByOutgoing(command.getHeader().getPeerId()).getSessionId();
        boolean checksumOk;

        if (checksumCallback != null) {
            ByteBuf buffer = byteBuf.slice(indexBefore, command.size());
            buffer.setInt(0, sessionId); // Replace checksum with session id for checksum generation

            checksumOk = checksum == checksumCallback.applyAsInt(buffer);
        } else {
            checksumOk = checksum == sessionId;
        }

        if (!checksumOk) {
            throw new InvalidChecksumException(checksum, sessionId, command);
        }



        objects.add(command);
    }
}
