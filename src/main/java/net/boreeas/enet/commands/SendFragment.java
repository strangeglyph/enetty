package net.boreeas.enet.commands;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;

/**
 * A fragment of a larger reliable packet.
 * @author Malte Schütze
 */
@Getter
@Setter
public class SendFragment extends ENetCommand implements DataCommand {
    /**
     * The sequence number of the complete packet, equal to the sequence number of
     * the first fragment.
     */
    private int startSeqNum;
    /**
     * The length of this fragment - the number of bytes that follow.
     */
    private int dataLength;
    /**
     * The number of fragments in this packet.
     */
    private long fragCount;
    /**
     * The index of this fragment, starting at 0 and going to <code>fragCount - 1</code>.
     */
    private long fragNum;
    /**
     * The total length of this packet.
     */
    private long totalLength;
    /**
     * <p>
     *     The offset of this fragment in bytes from the start of the packet.
     * </p>
     * → The following <code>dataLength</code> bytes occupy <code>[fragOffset .. fragOffset + DataLength - 1</code>
     * of the total packet.
     */
    private long fragOffset;
    /**
     * The data to be sent.
     */
    private ByteBuf data;

    public SendFragment(ENetProtocolHeader header, int channelId, int reliableSeqNum,
                         int startSeqNum, int dataLength, long fragCount, long fragNum, long totalLength, long fragOffset, byte[] data) {
        super(header, CommandId.SEND_FRAGMENT, channelId, reliableSeqNum, SendType.RELIABLE);

        this.startSeqNum = startSeqNum;
        this.dataLength = dataLength;
        this.fragCount = fragCount;
        this.fragNum = fragNum;
        this.totalLength = totalLength;
        this.fragOffset = fragOffset;

        this.data = Unpooled.wrappedBuffer(data, (int) fragOffset, dataLength);
    }

    public SendFragment(ENetProtocolHeader header, int channelId, int reliableSeqNum,
                         int startSeqNum, int dataLength, long fragCount, long fragNum, long totalLength, long fragOffset, ByteBuf data) {
        super(header, CommandId.SEND_FRAGMENT, channelId, reliableSeqNum, SendType.RELIABLE);

        this.startSeqNum = startSeqNum;
        this.dataLength = dataLength;
        this.fragCount = fragCount;
        this.fragNum = fragNum;
        this.totalLength = totalLength;
        this.fragOffset = fragOffset;

        this.data = data;
    }

    public SendFragment(ByteBuf data) {
        super(data);

        this.startSeqNum = data.readUnsignedShort();
        this.dataLength = data.readUnsignedShort();
        this.fragCount = data.readUnsignedInt();
        this.fragNum = data.readUnsignedInt();
        this.totalLength = data.readUnsignedInt();
        this.fragOffset = data.readUnsignedInt();

        this.data = data.copy(data.readerIndex(), dataLength);
    }

    public SendFragment(ByteBuf data, ENetProtocolHeader header) {
        super(data, header);

        this.startSeqNum = data.readUnsignedShort();
        this.dataLength = data.readUnsignedShort();
        this.fragCount = data.readUnsignedInt();
        this.fragNum = data.readUnsignedInt();
        this.totalLength = data.readUnsignedInt();
        this.fragOffset = data.readUnsignedInt();

        this.data = data.copy(data.readerIndex(), dataLength);
    }


    @Override
    int internalSize() {

        return 20 + dataLength;
    }


    @Override
    void writeInternalToBuffer(ByteBuf buffer) {

        buffer.writeShort(startSeqNum);
        buffer.writeShort(dataLength);
        buffer.writeInt((int) fragCount);
        buffer.writeInt((int) fragNum);
        buffer.writeInt((int) totalLength);
        buffer.writeInt((int) fragOffset);
        buffer.writeBytes(this.data);
    }
}
