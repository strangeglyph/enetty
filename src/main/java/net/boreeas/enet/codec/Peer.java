package net.boreeas.enet.codec;

import lombok.Getter;
import lombok.Setter;
import net.boreeas.enet.commands.ENetCommand;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by malte on 12/18/14.
 */
@Getter
@Setter
public class Peer {

    /**
     * Session id for the connection.
     */
    private int sessionId;
    /**
     * Our id on the peer's side.
     */
    private short incomingPeerId;
    /**
     * Peer's id on our side.
     */
    private short outgoingPeerId;



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
     */
    private Set<ENetCommand> ackPending = new HashSet<>();
    /**
     * Time in milliseconds until a packet is assumed lost.
     */
    private long ackTimeout = 500;
}
