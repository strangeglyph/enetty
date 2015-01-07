package net.boreeas.enetty.commands;

import io.netty.buffer.ByteBuf;

/**
 * @author Malte Schütze
 */
public class Ping extends ENetCommand {
    public Ping(ByteBuf buffer) {
        super(buffer);
    }

    public Ping(ByteBuf buffer, ENetProtocolHeader header) {
        super(buffer, header);
    }

    public Ping(ENetProtocolHeader header, int channelId, int reliableSeqNum) {
        super(header, CommandId.PING, channelId, reliableSeqNum, SendType.RELIABLE);
    }

    @Override
    int internalSize() {
        return 0;
    }

    @Override
    void writeInternalToBuffer(ByteBuf buffer) {

    }
}
