package net.boreeas.enet.commands;


import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

/**
 * Limit up- and downstream bandwidth of the peer.
 * @author Malte Schütze
 */
@Getter
@Setter
public class BandwidthLimit extends ENetCommand {

    private long incomingBandwidth;
    private long outgoingBandwidth;

    public BandwidthLimit(ENetProtocolHeader header, int channelId, int reliableSeqNum, SendType sendType,
                       long incomingBandwidth, long outgoingBandwidth) {
        super(header, CommandId.BANDWIDTH_LIMIT, channelId, reliableSeqNum, sendType);

        this.incomingBandwidth = incomingBandwidth;
        this.outgoingBandwidth = outgoingBandwidth;
    }

    public BandwidthLimit(ByteBuf buf) {
        super(buf);

        this.incomingBandwidth = buf.readUnsignedInt();
        this.outgoingBandwidth = buf.readUnsignedInt();
    }

    public BandwidthLimit(ByteBuf buf, ENetProtocolHeader header) {
        super(buf, header);

        this.incomingBandwidth = buf.readUnsignedInt();
        this.outgoingBandwidth = buf.readUnsignedInt();
    }


    @Override
    int internalSize() {

        return 4;
    }


    @Override
    void writeInternalToBuffer(ByteBuf buffer) {
        buffer.writeInt((int) incomingBandwidth);
        buffer.writeInt((int) outgoingBandwidth);
    }
}
