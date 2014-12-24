package net.boreeas.enet.commands;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

/**
 * Close the connection to a remote host
 * @author Malte Schütze
 */
@Getter
@Setter
public class Disconnect extends ENetCommand {

    /**
     * Application-specific data.
     */
    private int data;

    public Disconnect(ENetProtocolHeader header, int channelId, int reliableSeqNum, int data) {
        super(header, CommandId.DISCONNECT, channelId, reliableSeqNum, SendType.RELIABLE);

        this.data = data;
    }

    public Disconnect(ByteBuf buffer) {
        super(buffer);

        this.data = buffer.readInt();
    }

    public Disconnect(ByteBuf buffer, ENetProtocolHeader header) {
        super(buffer, header);

        this.data = buffer.readInt();
    }

    @Override
    int internalSize() {
        return 4;
    }

    @Override
    void writeInternalToBuffer(ByteBuf buffer) {
        buffer.writeInt(data);
    }
}
