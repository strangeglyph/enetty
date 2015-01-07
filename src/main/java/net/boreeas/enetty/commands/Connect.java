package net.boreeas.enetty.commands;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

/**
 * Initiate a connection to a remote host.
 * @author Malte Schütze
 */
@Getter
@Setter
public class Connect extends ENetCommand {

    /**
     * Peer id on the sender's side.
     */
    private int outgoingPeerId;
    /**
     * Maximum packet size, including all headers.
     */
    private int mtu;
    /**
     * Sender's receive window size - receiver should send more packets if this many packets are unacknowledged.
     */
    private long windowSize;
    /**
     * The number of channels for the connection.
     */
    private long channelCount;
    /**
     * Sender's downlink.
     */
    private long incomingBandwidth;
    /**
     * Sender's uplink.
     */
    private long outgoingBandwidth;
    private long packetThrottleInterval;
    private long packetThrottleAcceleration;
    private long packetThrottleDeceleration;
    /**
     * Id for the session. Initializes checksum.
     */
    private long sessionId;

    public Connect(ENetProtocolHeader header, int channelId, int reliableSeqNum) {
        super(header, CommandId.CONNECT, channelId, reliableSeqNum, SendType.RELIABLE);
    }

    public Connect(ENetProtocolHeader header, int channelId, int reliableSeqNum,
                   int outgoingPeerId, int mtu, long windowSize, long channelCount,
                   long incomingBandwidth, long outgoingBandwidth,
                   long packetThrottleInterval, long packetThrottleAcceleration, long packetThrottleDeceleration,
                   long sessionId) {

        super(header, CommandId.CONNECT, channelId, reliableSeqNum, SendType.RELIABLE);
        this.outgoingPeerId = outgoingPeerId;
        this.mtu = mtu;
        this.windowSize = windowSize;
        this.channelCount = channelCount;
        this.incomingBandwidth = incomingBandwidth;
        this.outgoingBandwidth = outgoingBandwidth;
        this.packetThrottleInterval = packetThrottleInterval;
        this.packetThrottleAcceleration = packetThrottleAcceleration;
        this.packetThrottleDeceleration = packetThrottleDeceleration;
        this.sessionId = sessionId;
    }

    public Connect(ByteBuf buffer) {
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
        this.sessionId = buffer.readUnsignedInt();
    }

    public Connect(ByteBuf buffer, ENetProtocolHeader header) {
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
        this.sessionId = buffer.readUnsignedInt();
    }


    @Override
    int internalSize() {
        return 36;
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
        buffer.writeInt((int) sessionId);
    }
}
