package net.boreeas.enet.commands;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Header for ENet commands
 * @author Malte Schütze
 */
@Getter
@Setter
@AllArgsConstructor
public class ENetProtocolHeader {
    public static final int PEER_ID_MASK = 0x7fff;
    public static final byte FLAG_SENT_TIME = (byte) (1 << 7);

    /**
     * Checksum for the entire command. Checksum process is
     * user-defined.
     */
    private int checksum;
    /**
     * Indicates whether the <code>sentTime</code> field is included or not.
     */
    private boolean hasSentTime;
    /**
     * Sender's peer id on the receiving side.
     */
    private int peerId;
    /**
     * Time the packet was sent.
     */
    private int sentTime;

    public ENetProtocolHeader(ByteBuf buffer) {

        this.checksum = buffer.readInt();

        int peerId = buffer.readUnsignedShort();
        this.peerId = peerId & PEER_ID_MASK;
        this.hasSentTime = (peerId & (FLAG_SENT_TIME << 8)) > 0;

        if (hasSentTime) {
            this.sentTime = buffer.readShort();
        }
    }

    /**
     * The size of the header, in bytes. 8 bytes if <code>sentTime</code> is included, 6 otherwise.
     * @return The size of the header.
     */
    public int size() {
        return hasSentTime ? 8 : 6;
    }

    /**
     * Writes this header to a buffer.
     * @param buffer The buffer to write to.
     */
    public void writeToBuffer(ByteBuf buffer) {
        buffer.writeInt(checksum);
        buffer.writeShort(peerId | ((hasSentTime ? 1 : 0) << 15));

        if (hasSentTime) {
            buffer.writeShort(sentTime);
        }
    }
}
