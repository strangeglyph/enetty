package net.boreeas.enet.commands;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

/**
 * Configure throttle acceleration and deceleration. See wiki for
 * throttle documentation.
 * @author Malte Schütze
 */
@Getter
@Setter
public class ThrottleConfigure extends ENetCommand {

    private long packetThrottleInterval;
    private long packetThrottleAcceleration;
    private long packetThrottleDeceleration;

    public ThrottleConfigure(ENetProtocolHeader header, int channelId, int reliableSeqNum, SendType sendType,
                          long packetThrottleInterval, long packetThrottleAcceleration, long packetThrottleDeceleration) {
        super(header, CommandId.THROTTLE_CONFIGURE, channelId, reliableSeqNum, sendType);

        this.packetThrottleInterval = packetThrottleInterval;
        this.packetThrottleAcceleration = packetThrottleAcceleration;
        this.packetThrottleDeceleration = packetThrottleDeceleration;
    }

    public ThrottleConfigure(ByteBuf buf) {
        super(buf);

        this.packetThrottleInterval = buf.readUnsignedInt();
        this.packetThrottleAcceleration = buf.readUnsignedInt();
        this.packetThrottleDeceleration = buf.readUnsignedInt();
    }

    public ThrottleConfigure(ByteBuf buf, ENetProtocolHeader header) {
        super(buf, header);

        this.packetThrottleInterval = buf.readUnsignedInt();
        this.packetThrottleAcceleration = buf.readUnsignedInt();
        this.packetThrottleDeceleration = buf.readUnsignedInt();
    }

    @Override
    int internalSize() {

        return 12;
    }


    @Override
    void writeInternalToBuffer(ByteBuf buffer) {
        buffer.writeInt((int) packetThrottleInterval);
        buffer.writeInt((int) packetThrottleAcceleration);
        buffer.writeInt((int) packetThrottleDeceleration);
    }
}
