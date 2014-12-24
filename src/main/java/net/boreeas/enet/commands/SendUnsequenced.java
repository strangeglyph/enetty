package net.boreeas.enet.commands;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;

/**
 * An unsequenced packet, bypassing sequence number order checks. Channelless.
 * @author Malte Schütze
 */
@Getter
@Setter
public class SendUnsequenced extends ENetCommand {
    private int unsequencedGroup;
    private int dataLength;
    private ByteBuf buffer;

    public SendUnsequenced(ENetProtocolHeader header, int channelId, int reliableSeqNum, int unsequencedGroup, byte[] data) {
        super(header, CommandId.SEND_UNSEQUENCED, channelId, reliableSeqNum, SendType.UNSEQUENCED);

        this.unsequencedGroup = unsequencedGroup;
        this.dataLength = data.length;
        this.buffer = Unpooled.wrappedBuffer(data);
    }

    public SendUnsequenced(ENetProtocolHeader header, int channelId, int reliableSeqNum, int unsequencedGroup, ByteBuf buffer) {
        super(header, CommandId.SEND_UNSEQUENCED, channelId, reliableSeqNum, SendType.UNSEQUENCED);

        this.unsequencedGroup = unsequencedGroup;
        this.dataLength = buffer.readableBytes();
        this.buffer = buffer;
    }

    public SendUnsequenced(ByteBuf buffer) {
        super(buffer);

        this.unsequencedGroup = buffer.readUnsignedShort();
        this.dataLength = buffer.readUnsignedShort();
        this.buffer = buffer.copy(buffer.readerIndex(), dataLength);
    }

    public SendUnsequenced(ByteBuf buffer, ENetProtocolHeader header) {
        super(buffer, header);

        this.unsequencedGroup = buffer.readUnsignedShort();
        this.dataLength = buffer.readUnsignedShort();
        this.buffer = buffer.copy(buffer.readerIndex(), dataLength);
    }

    @Override
    int internalSize() {
        return 4 + dataLength;
    }

    @Override
    void writeInternalToBuffer(ByteBuf buffer) {
        buffer.writeShort(unsequencedGroup);
        buffer.writeShort(dataLength);
        buffer.writeBytes(buffer);
    }
}
