package net.boreeas.enet.commands;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Malte Schütze
 */
@Getter
@Setter
public class SendReliable extends ENetCommand {
    private int dataLength;
    private ByteBuf buffer;

    public SendReliable(ENetProtocolHeader header, int channelId, int reliableSeqNum, byte[] data) {
        super(header, CommandId.SEND_RELIABLE, channelId, reliableSeqNum, SendType.RELIABLE);

        this.dataLength = data.length;
        this.buffer = Unpooled.wrappedBuffer(data);
    }

    public SendReliable(ENetProtocolHeader header, int channelId, int reliableSeqNum, ByteBuf buffer) {
        super(header, CommandId.SEND_RELIABLE, channelId, reliableSeqNum, SendType.RELIABLE);

        this.dataLength = buffer.readableBytes();
        this.buffer = buffer;
    }

    public SendReliable(ByteBuf buffer) {
        super(buffer);

        this.dataLength = buffer.readUnsignedShort();
        this.buffer = buffer.copy(buffer.readerIndex(), dataLength);
    }

    public SendReliable(ByteBuf buffer, ENetProtocolHeader header) {
        super(buffer, header);

        this.dataLength = buffer.readUnsignedShort();
        this.buffer = buffer.copy(buffer.readerIndex(), dataLength);
    }

    @Override
    int internalSize() {
        return 2 + dataLength;
    }

    @Override
    void writeInternalToBuffer(ByteBuf buffer) {
        buffer.writeShort(dataLength);
        buffer.writeBytes(buffer);
    }
}
