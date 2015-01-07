package net.boreeas.enet.commands;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents any ENet command that can be sent.
 * @author Malte Schütze
 */
@Getter
@Setter
@AllArgsConstructor
public abstract class ENetCommand {
    public enum SendType {RELIABLE, UNRELIABLE, UNSEQUENCED}

    public static final byte FLAG_ACKNOWLEDGE = (byte) (1 << 7);
    public static final byte FLAG_UNSEQUENCED = (byte) (1 << 6);
    public static final int COMMAND_MASK = 0b111111;

    private ENetProtocolHeader header;
    private CommandId command;
    private int channelId;
    private int reliableSequenceNumber;
    private SendType sendType;

    public ENetCommand(ByteBuf buffer) {
        this(buffer, new ENetProtocolHeader(buffer));
    }

    public ENetCommand(ByteBuf buffer, ENetProtocolHeader header) {
        this.header = header;

        int command = buffer.readUnsignedByte();
        this.command = CommandId.getById(command & COMMAND_MASK);

        boolean hasAck = (command & FLAG_ACKNOWLEDGE) > 0;
        boolean hasUnseq = (command & FLAG_UNSEQUENCED) > 0;

        if (hasAck && hasUnseq) {
            throw new IllegalArgumentException("Both ACKNOWLEDGE and UNSEQUENCED have been set");
        } else if (hasAck) {
            this.sendType = SendType.RELIABLE;
        } else if (hasUnseq) {
            this.sendType = SendType.UNSEQUENCED;
        } else {
            this.sendType = SendType.UNRELIABLE;
        }

        this.channelId = buffer.readUnsignedByte();
        this.reliableSequenceNumber = buffer.readUnsignedShort();
    }

    /**
     * The size of this command, excluding the header size.
     * @return The inner size of this command.
     */
    abstract int internalSize();

    /**
     * The total size of this command.
     * @return The total size of this command.
     */
    public int size() {
        return header.size() + internalSize() + 4;
    }

    /**
     * Writes this command, excluding the header, to the specified data.
     * @param buffer The data to write to.
     */
    abstract void writeInternalToBuffer(ByteBuf buffer);

    /**
     * Writes this command to the specified data.
     * @param buffer The data to write to.
     */
    public void writeToBuffer(ByteBuf buffer) {
        if (!buffer.isWritable(size())) {
            throw new IllegalArgumentException("Buffer too small");
        }

        header.writeToBuffer(buffer);
        buffer.writeByte(command.id | (sendType == SendType.RELIABLE ? FLAG_ACKNOWLEDGE : 0) | (sendType == SendType.UNSEQUENCED ? FLAG_UNSEQUENCED : 0));
        buffer.writeByte(channelId);
        buffer.writeShort(reliableSequenceNumber);
        writeInternalToBuffer(buffer);
    }
}
