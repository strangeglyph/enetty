package net.boreeas.enetty.codec;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Malte Schütze
 */
public class PeerMap {
    /**
     * Map peer's outgoing (= peerId in command headers sent by peer) id to the peer.
     */
    private Map<Integer, Peer> byOutgoingId = new HashMap<>();
    /**
     * Map peer's incoming (= peerId in out command headers sent to the peer) id to the peer.
     */
    private Map<Integer, Peer> byIncomingId = new HashMap<>();

    /**
     * Add a peer to the map.
     * @param peer The peer to add.
     */
    public void add(Peer peer) {
        byOutgoingId.put((int) peer.getOutgoingPeerId(), peer);
        byIncomingId.put((int) peer.getIncomingPeerId(), peer);
    }

    /**
     * Remove a peer.
     * @param peer The peer to remove.
     */
    public void remove(Peer peer) {
        byOutgoingId.remove((int) peer.getOutgoingPeerId());
        byIncomingId.remove((int) peer.getIncomingPeerId());
    }

    /**
     * Retrieve a peer by the peer's incoming id.
     * The incoming id is the id we put in our headers.
     * @param id The incoming id.
     * @return The peer.
     */
    public Peer getByIncoming(int id) {
        return byIncomingId.get(id);
    }

    /**
     * Retrieve a peer by the peer's outgoing id.
     * The outgoing id is the id the peer puts into their headers.
     * @param id The outgoing id.
     * @return The peer.
     */
    public Peer getByOutgoing(int id) {
        return byOutgoingId.get(id);
    }
}
