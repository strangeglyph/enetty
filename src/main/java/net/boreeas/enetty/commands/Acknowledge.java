package net.boreeas.enetty.commands;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

/**
 * Acknowledge a received packet with FLAG_ACKNOWLEDGE set.
 * @author Malte Schütze
 */
@Getter
@Setter
public class Acknowledge extends ENetCommand {

    private int receivedSeqNum;
    private int receivedSendTime;

    public Acknowledge(ENetProtocolHeader header, int channelId, int reliableSeqNum, SendType sendType,
                       int receivedSeqNum, int receivedSendTime) {
        super(header, CommandId.ACKNOWLEDGE, channelId, reliableSeqNum, sendType);

        this.receivedSeqNum = receivedSeqNum;
        this.receivedSendTime = receivedSendTime;
    }

    public Acknowledge(ByteBuf buf) {
        super(buf);

        this.receivedSeqNum = buf.readUnsignedShort();
        this.receivedSendTime = buf.readUnsignedShort();
    }

    public Acknowledge(ByteBuf buf, ENetProtocolHeader header) {
        super(buf, header);

        this.receivedSeqNum = buf.readUnsignedShort();
        this.receivedSendTime = buf.readUnsignedShort();
    }



    @Override
    int internalSize() {
        return 4;
    }

    @Override
    void writeInternalToBuffer(ByteBuf buffer) {
        buffer.writeShort(receivedSeqNum);
        buffer.writeShort(receivedSendTime);
    }

}
