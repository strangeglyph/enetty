package net.boreeas.enetty.commands;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Malte Schütze
 */
@Getter
@Setter
public class SendReliable extends ENetCommand implements DataCommand {
    private int dataLength;
    private ByteBuf data;

    public SendReliable(ENetProtocolHeader header, int channelId, int reliableSeqNum, byte[] data) {
        super(header, CommandId.SEND_RELIABLE, channelId, reliableSeqNum, SendType.RELIABLE);

        this.dataLength = data.length;
        this.data = Unpooled.wrappedBuffer(data);
    }

    public SendReliable(ENetProtocolHeader header, int channelId, int reliableSeqNum, ByteBuf data) {
        super(header, CommandId.SEND_RELIABLE, channelId, reliableSeqNum, SendType.RELIABLE);

        this.dataLength = data.readableBytes();
        this.data = data;
    }

    public SendReliable(ByteBuf data) {
        super(data);

        this.dataLength = data.readUnsignedShort();
        this.data = data.copy(data.readerIndex(), dataLength);
    }

    public SendReliable(ByteBuf data, ENetProtocolHeader header) {
        super(data, header);

        this.dataLength = data.readUnsignedShort();
        this.data = data.copy(data.readerIndex(), dataLength);
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
