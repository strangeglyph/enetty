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
public class SendUnsequenced extends ENetCommand implements DataCommand {
    private int unsequencedGroup;
    private int dataLength;
    private ByteBuf data;

    public SendUnsequenced(ENetProtocolHeader header, int channelId, int reliableSeqNum, int unsequencedGroup, byte[] data) {
        super(header, CommandId.SEND_UNSEQUENCED, channelId, reliableSeqNum, SendType.UNSEQUENCED);

        this.unsequencedGroup = unsequencedGroup;
        this.dataLength = data.length;
        this.data = Unpooled.wrappedBuffer(data);
    }

    public SendUnsequenced(ENetProtocolHeader header, int channelId, int reliableSeqNum, int unsequencedGroup, ByteBuf data) {
        super(header, CommandId.SEND_UNSEQUENCED, channelId, reliableSeqNum, SendType.UNSEQUENCED);

        this.unsequencedGroup = unsequencedGroup;
        this.dataLength = data.readableBytes();
        this.data = data;
    }

    public SendUnsequenced(ByteBuf data) {
        super(data);

        this.unsequencedGroup = data.readUnsignedShort();
        this.dataLength = data.readUnsignedShort();
        this.data = data.copy(data.readerIndex(), dataLength);
    }

    public SendUnsequenced(ByteBuf data, ENetProtocolHeader header) {
        super(data, header);

        this.unsequencedGroup = data.readUnsignedShort();
        this.dataLength = data.readUnsignedShort();
        this.data = data.copy(data.readerIndex(), dataLength);
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
