package net.boreeas.enet.commands;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

/**
 * Verifies connection parameters. Verification is usually just bounds checking.
 * If sender and receiver can't agree on connection parameters, the connection should be closed.
 *
 * @author Malte Schütze
 */
@Getter
@Setter
public class VerifyConnect extends ENetCommand {

    /**
     * Verified original sender's outgoing peer id.
     */
    private int outgoingPeerId;
    /**
     * Verified MTU
     */
    private int mtu;
    /**
     * Verified window size
     */
    private long windowSize;
    /**
     * Verified channel count.
     */
    private long channelCount;
    /**
     * Verified original sender's downlink.
     */
    private long incomingBandwidth;
    /**
     * Verified original sender's uplink.
     */
    private long outgoingBandwidth;
    /**
     * Verified packet throttle interval.
     */
    private long packetThrottleInterval;
    /**
     * Verified packet throttle acceleration.
     */
    private long packetThrottleAcceleration;
    /**
     * Verified packet throttle deceleration.
     */
    private long packetThrottleDeceleration;


    public VerifyConnect(ENetProtocolHeader header, int channelId, int reliableSeqNum) {
        super(header, CommandId.VERIFY_CONNECT, channelId, reliableSeqNum, SendType.RELIABLE);
    }

    public VerifyConnect(ENetProtocolHeader header, int channelId, int reliableSeqNum,
                         int outgoingPeerId, int mtu, long windowSize, long channelCount,
                         long incomingBandwidth, long outgoingBandwidth,
                         long packetThrottleInterval, long packetThrottleAcceleration, long packetThrottleDeceleration) {
        super(header, CommandId.VERIFY_CONNECT, channelId, reliableSeqNum, SendType.RELIABLE);

        this.outgoingPeerId = outgoingPeerId;
        this.mtu = mtu;
        this.windowSize = windowSize;
        this.channelCount = channelCount;
        this.incomingBandwidth = incomingBandwidth;
        this.outgoingBandwidth = outgoingBandwidth;
        this.packetThrottleInterval = packetThrottleInterval;
        this.packetThrottleAcceleration = packetThrottleAcceleration;
        this.packetThrottleDeceleration = packetThrottleDeceleration;
    }

    public VerifyConnect(ByteBuf buffer) {
        super(buffer);

        this.outgoingPeerId = buffer.readUnsignedShort();
        this.mtu = buffer.readUnsignedShort();
        this.windowSize = buffer.readUnsignedInt();
        this.channelCount = buffer.readUnsignedInt();
        this.incomingBandwidth = buffer.readUnsignedInt();
        this.outgoingBandwidth = buffer.readUnsignedInt();
        this.packetThrottleInterval = buffer.readUnsignedInt();
        this.packetThrottleAcceleration = buffer.readUnsignedInt();
        this.packetThrottleDeceleration = buffer.readUnsignedInt();
    }

    public VerifyConnect(ByteBuf buffer, ENetProtocolHeader header) {
        super(buffer, header);

        this.outgoingPeerId = buffer.readUnsignedShort();
        this.mtu = buffer.readUnsignedShort();
        this.windowSize = buffer.readUnsignedInt();
        this.channelCount = buffer.readUnsignedInt();
        this.incomingBandwidth = buffer.readUnsignedInt();
        this.outgoingBandwidth = buffer.readUnsignedInt();
        this.packetThrottleInterval = buffer.readUnsignedInt();
        this.packetThrottleAcceleration = buffer.readUnsignedInt();
        this.packetThrottleDeceleration = buffer.readUnsignedInt();
    }


    @Override
    int internalSize() {
        return 32;
    }

    @Override
    void writeInternalToBuffer(ByteBuf buffer) {
        buffer.writeShort(outgoingPeerId);
        buffer.writeShort(mtu);
        buffer.writeInt((int) windowSize);
        buffer.writeInt((int) channelCount);
        buffer.writeInt((int) incomingBandwidth);
        buffer.writeInt((int) outgoingBandwidth);
        buffer.writeInt((int) packetThrottleInterval);
        buffer.writeInt((int) packetThrottleAcceleration);
        buffer.writeInt((int) packetThrottleDeceleration);
    }
}
