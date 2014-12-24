package net.boreeas.enet.commands;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Malte Schütze
 */
public class SendUnreliable extends ENetCommand {
    private int unreliableSeqNum;
    private int dataLength;
    private ByteBuf buffer;

    public SendUnreliable(ENetProtocolHeader header, int channelId, int reliableSeqNum, int unreliableSeqNum, byte[] data) {
        super(header, CommandId.SEND_UNRELIABLE, channelId, reliableSeqNum, SendType.UNRELIABLE);

        this.unreliableSeqNum = unreliableSeqNum;
        this.dataLength = data.length;
        this.buffer = Unpooled.wrappedBuffer(data);
    }

    public SendUnreliable(ENetProtocolHeader header, int channelId, int reliableSeqNum, int unreliableSeqNum, ByteBuf buffer) {
        super(header, CommandId.SEND_UNRELIABLE, channelId, reliableSeqNum, SendType.RELIABLE);

        this.unreliableSeqNum = unreliableSeqNum;
        this.dataLength = buffer.readableBytes();
        this.buffer = buffer;
    }

    public SendUnreliable(ByteBuf buffer) {
        super(buffer);

        this.unreliableSeqNum = buffer.readUnsignedShort();
        this.dataLength = buffer.readUnsignedShort();
        this.buffer = buffer.copy(buffer.readerIndex(), dataLength);
    }

    public SendUnreliable(ByteBuf buffer, ENetProtocolHeader header) {
        super(buffer, header);

        this.unreliableSeqNum = buffer.readUnsignedShort();
        this.dataLength = buffer.readUnsignedShort();
        this.buffer = buffer.copy(buffer.readerIndex(), dataLength);
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
