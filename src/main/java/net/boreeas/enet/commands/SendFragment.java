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
public class SendFragment extends ENetCommand {
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
    private ByteBuf buffer;

    public SendFragment(ENetProtocolHeader header, int channelId, int reliableSeqNum,
                         int startSeqNum, int dataLength, long fragCount, long fragNum, long totalLength, long fragOffset, byte[] data) {
        super(header, CommandId.SEND_FRAGMENT, channelId, reliableSeqNum, SendType.RELIABLE);

        this.startSeqNum = startSeqNum;
        this.dataLength = dataLength;
        this.fragCount = fragCount;
        this.fragNum = fragNum;
        this.totalLength = totalLength;
        this.fragOffset = fragOffset;

        this.buffer = Unpooled.wrappedBuffer(data, (int) fragOffset, dataLength);
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

        this.buffer = data;
    }

    public SendFragment(ByteBuf buffer) {
        super(buffer);

        this.startSeqNum = buffer.readUnsignedShort();
        this.dataLength = buffer.readUnsignedShort();
        this.fragCount = buffer.readUnsignedInt();
        this.fragNum = buffer.readUnsignedInt();
        this.totalLength = buffer.readUnsignedInt();
        this.fragOffset = buffer.readUnsignedInt();

        this.buffer = buffer.copy(buffer.readerIndex(), dataLength);
    }

    public SendFragment(ByteBuf buffer, ENetProtocolHeader header) {
        super(buffer, header);

        this.startSeqNum = buffer.readUnsignedShort();
        this.dataLength = buffer.readUnsignedShort();
        this.fragCount = buffer.readUnsignedInt();
        this.fragNum = buffer.readUnsignedInt();
        this.totalLength = buffer.readUnsignedInt();
        this.fragOffset = buffer.readUnsignedInt();

        this.buffer = buffer.copy(buffer.readerIndex(), dataLength);
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
        buffer.writeBytes(this.buffer);
    }
}
