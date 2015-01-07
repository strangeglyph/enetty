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
public class SendUnreliable extends ENetCommand implements DataCommand {
    private int unreliableSeqNum;
    private int dataLength;
    private ByteBuf data;

    public SendUnreliable(ENetProtocolHeader header, int channelId, int reliableSeqNum, int unreliableSeqNum, byte[] data) {
        super(header, CommandId.SEND_UNRELIABLE, channelId, reliableSeqNum, SendType.UNRELIABLE);

        this.unreliableSeqNum = unreliableSeqNum;
        this.dataLength = data.length;
        this.data = Unpooled.wrappedBuffer(data);
    }

    public SendUnreliable(ENetProtocolHeader header, int channelId, int reliableSeqNum, int unreliableSeqNum, ByteBuf data) {
        super(header, CommandId.SEND_UNRELIABLE, channelId, reliableSeqNum, SendType.RELIABLE);

        this.unreliableSeqNum = unreliableSeqNum;
        this.dataLength = data.readableBytes();
        this.data = data;
    }

    public SendUnreliable(ByteBuf data) {
        super(data);

        this.unreliableSeqNum = data.readUnsignedShort();
        this.dataLength = data.readUnsignedShort();
        this.data = data.copy(data.readerIndex(), dataLength);
    }

    public SendUnreliable(ByteBuf data, ENetProtocolHeader header) {
        super(data, header);

        this.unreliableSeqNum = data.readUnsignedShort();
        this.dataLength = data.readUnsignedShort();
        this.data = data.copy(data.readerIndex(), dataLength);
    }

    @Override
    int internalSize() {
        return 4 + dataLength;
    }

    @Override
    void writeInternalToBuffer(ByteBuf buffer) {
        buffer.writeShort(unreliableSeqNum);
        buffer.writeShort(dataLength);
        buffer.writeBytes(buffer);
    }
}
