package net.boreeas.enetty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import net.boreeas.enetty.commands.ENetCommand;

import java.util.function.ToIntFunction;

/**
 * Created by malte on 12/9/14.
 */
@AllArgsConstructor
public class ENetCommandEncoder extends MessageToByteEncoder<ENetCommand> {

    private PeerMap peerLookup;
    private ToIntFunction<ByteBuf> checksumCallback;

    @Override
    protected void encode(ChannelHandlerContext ctx, ENetCommand msg, ByteBuf out) throws Exception {

        msg.getHeader().setChecksum(peerLookup.getByOurId(msg.getHeader().getPeerId()).getSessionId());

        int beforeIndex = out.writerIndex();
        msg.writeToBuffer(out);

        if (checksumCallback != null) {
            ByteBuf slice = out.slice(beforeIndex, msg.size());
            out.setInt(beforeIndex, checksumCallback.applyAsInt(slice));
        }
    }
}
