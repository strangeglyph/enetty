package net.boreeas.enet.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import net.boreeas.enet.commands.ENetCommand;
import net.boreeas.enet.commands.SendUnsequenced;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by malte on 12/18/14.
 */
@Getter
@Setter
public class Peer {
    /**
     * The maximum value for the window size param
     */
    public static final long MAX_RELIABLE_WINDOWS = 16;
    /**
     * The number of windows assumed to lie in the future
     */
    public static final int FREE_RELIABLE_WINDOWS = 8;


    /**
     * Callback for unsequenced data and data delivered to channels where no data callbacks are set
     */
    private Consumer<ByteBuf> dataCallback;

    /**
     * Time the connection was first attempted.
     */
    private long startTime = System.currentTimeMillis();

    /**
     * Session id for the connection.
     */
    private int sessionId;
    /**
     * Our id on the peer's side.
     */
    private int incomingPeerId;
    /**
     * Peer's id on our side.
     */
    private int outgoingPeerId;

    /**
     * MTU for the peer
     */
    private int mtu;
    /**
     * Amount of channels to allocate
     */
    private long channelCount;
    /**
     * Maximum amount on unacknowledged reliable commands
     */
    private long windowSize;


    /**
     * RTT measurement time frame.
     */
    private long packetThrottleInterval;
    /**
     * Amount to increase <code>currentThrottleValue</code> by if the average RTT is very low.
     */
    private long packetThrottleAcceleration;
    /**
     * Amount to decrease <code>currentThrottleValue</code> by if the average RTT is too high.
     */
    private long packetThrottleDeceleration;
    /**
     * Packet drop cutoff value.
     */
    private int currentThrottleValue;
    /**
     * Current packet drop score. If score is below <code>currentThrottleValue</code>, the next unreliable packet
     * will be dropped.
     */
    private int currentThrottleScore;



    /**
     * Incoming bandwidth limit. Minimum of the hosts incoming limit and the peers outgoing limit
     */
    private long incomingBandwidth;
    /**
     * Outgoing bandwidth limit. Minimum of the hosts outgoing limit and the peers incoming limit.
     */
    private long outgoingBandwidth;



    /**
     * Reliable commands still pending acknowledgement.
     * Key is the sequence number of the command.
     */
    private Map<Integer, ENetCommand> ackPending = new HashMap<>();
    /**
     * Time in milliseconds until a packet is assumed lost.
     */
    private long ackTimeout = 500;

    /**
     * Netty communication channel
     */
    private Channel channel;

    private Function<ByteBuf, ByteBuf> encryptionFunction;
    private Function<ByteBuf, ByteBuf> decryptionFunction;


    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ENetChannel[] enetChannels = new ENetChannel[256]; // Maximum number of channels is 256 since channelId is a byte
    {
        for (int i = 0; i < enetChannels.length; i++) {
            enetChannels[i] = new ENetChannel(this, i);
        }
    }


    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private BitSet unsequencedGroup = new BitSet(1024);
    @Setter(AccessLevel.NONE)
    private int lastReceivedUnsequencedGroup;



    /**
     * Writes an ENetCommand directly to the netty outbound channel
     * @param command The command to send
     */
    void write(ENetCommand command) {
        channel.writeAndFlush(command);
    }

    /**
     * Returns the ENetChannel with the given id.
     * @param id The id of the channel
     * @return The channel with the given id
     * @throws java.lang.IndexOutOfBoundsException If the id exceeds the amount of channels
     */
    public ENetChannel getENetChannel(int id) {
        if (id >= channelCount) throw new IndexOutOfBoundsException("Channel count: " + channelCount);
        return enetChannels[id];
    }

    /**
     * Sends reliable data over the specified channel. Functionally equivalent
     * to <code>getENetChannel(channel).sendReliable(data)</code>
     * @param channel The channel id
     * @param data The data to send
     * @see net.boreeas.enet.codec.ENetChannel#sendReliable(io.netty.buffer.ByteBuf)
     */
    public void sendReliable(int channel, ByteBuf data) {
        getENetChannel(channel).sendReliable(data);
    }

    /**
     * Sends unreliable data over te specified channel. Functionally equivalent to
     * <code>getENetChannel(channel).sendUnreliable(data)</code>
     * @param channel The channel id
     * @param data The data to send
     * @throws MtuExceededException If the data length exceeds the maximum transmittable amount
     * @see net.boreeas.enet.codec.ENetChannel#sendUnreliable(io.netty.buffer.ByteBuf)
     */
    public void sendUnreliable(int channel, ByteBuf data) throws MtuExceededException {
        getENetChannel(channel).sendUnreliable(data);
    }


    /**
     * Returns the time in milliseconds since the start of the connection.
     * @return The connection time
     */
    public int connectionTime() {
        return (int) (System.currentTimeMillis() - startTime);
    }


    @Synchronized
    void onUnsequencedReceived(SendUnsequenced cmd) {
        int group = cmd.getUnsequencedGroup();
        int index = group % 1024;
        if (group < lastReceivedUnsequencedGroup) group += 0x10000;

        if (group > lastReceivedUnsequencedGroup + 0x8000) return; // Drop data

        if (group > lastReceivedUnsequencedGroup && group <= lastReceivedUnsequencedGroup + 1024) {
            if (unsequencedGroup.get(index)) return; // Already delivered
        } else {
            lastReceivedUnsequencedGroup = group - index;
            unsequencedGroup.clear();
        }

        unsequencedGroup.set(index);
        if (dataCallback != null) dataCallback.accept(decryptionFunction == null ? cmd.getData() : decryptionFunction.apply(cmd.getData()));
    }
}
