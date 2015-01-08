package net.boreeas.enetty.codec;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Malte Schütze
 */
public class PeerMap {
    /**
     * Map peer's outgoing (= peerId in command headers sent by peer) id to the peer.
     */
    private Map<Integer, Peer> byPeerId = new HashMap<>();
    /**
     * Map peer's incoming (= peerId in our command headers sent to the peer) id to the peer.
     */
    private Map<CompositeKey, Peer> byOurId = new HashMap<>();

    /**
     * Add a peer to the map.
     * @param peer The peer to add.
     */
    public void add(Peer peer) {
        byPeerId.put(peer.getPeerId(), peer);
        byOurId.put(new CompositeKey(peer.getOurId(), peer.getAddress(), peer.getPort()), peer);
    }

    /**
     * Remove a peer.
     * @param peer The peer to remove.
     */
    public void remove(Peer peer) {
        byPeerId.remove(peer.getPeerId());
        byOurId.remove(new CompositeKey(peer.getOurId(), peer.getAddress(), peer.getPort()));
    }

    /**
     * Retrieve a peer by the peer's incoming id.
     * The incoming id is the id we put in our headers.
     * @param id The incoming id.
     * @return The peer.
     */
    public Peer getByOurId(int id, InetAddress address, int port) {
        return byOurId.get(new CompositeKey(id, address, port));
    }

    /**
     * Retrieve a peer by the peer's outgoing id.
     * The outgoing id is the id the peer puts into their headers.
     * @param id The outgoing id.
     * @return The peer.
     */
    public Peer getByOutgoing(int id) {
        return byPeerId.get(id);
    }

    @Data
    @AllArgsConstructor
    private class CompositeKey {
        int id;
        InetAddress address;
        int port;
    }
}
