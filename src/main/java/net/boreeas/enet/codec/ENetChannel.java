package net.boreeas.enet.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.*;
import net.boreeas.enet.commands.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author Malte Schütze
 */
public class ENetChannel {
    @Getter private final Peer peer;
    @Getter private final int channelId;

    @Setter private Consumer<ByteBuf> reliableDataCallback;
    @Setter private Consumer<ByteBuf> unreliableDataCallback;
    @Setter private Consumer<ByteBuf> unifiedDataCallback;

    private AtomicInteger nextReliableSequenceNumber = new AtomicInteger();
    private AtomicInteger nextUnreliableSequenceNumber = new AtomicInteger();

    private int lastDeliveredReliableSeqNum;
    private int lastDeliveredUnreliableSeqNum;
    private int cycle; // Incremented every time the reliable seq num wraps around, for easier ordering

    private PriorityBlockingQueue<WaitingData> waitingCommands = new PriorityBlockingQueue<>();
    private ConcurrentMap<Integer, FragmentAccumulator> fragmentMap = new ConcurrentHashMap<>();


    public ENetChannel(Peer peer, int channelId) {
        this.peer = peer;
        this.channelId = channelId;
    }

    private void dispatch(ENetCommand command) {
        // TODO window management...
    }

    private int nextExpectedSeqNum() {
        return (lastDeliveredReliableSeqNum + 1) % Short.MAX_VALUE;
    }

    private void queueDelivery(WaitingData data) {
        waitingCommands.add(data);

        if (data.getSeqnum() == nextExpectedSeqNum()) {
            synchronized (this) {
                if (data.getSeqnum() == nextExpectedSeqNum()) {
                    deliverWaiting();
                }
            }
        }
    }

    private void deliverWaiting() {
        while (!waitingCommands.isEmpty() && waitingCommands.peek().getSeqnum() == nextExpectedSeqNum()) {
            WaitingData data = waitingCommands.poll();

            if (data.getSeqnum() < lastDeliveredReliableSeqNum) cycle++; // Next cycle begins on overflow
            lastDeliveredReliableSeqNum = data.getSeqnum();
            deliver(data);
        }
    }

    private void deliver(WaitingData data) {
        if (reliableDataCallback != null) {
            reliableDataCallback.accept(data.buffer);
        } else if (unifiedDataCallback != null) {
            unifiedDataCallback.accept(data.buffer);
        } else if (peer.getDataCallback() != null) {
            peer.getDataCallback().accept(data.buffer);
        }
    }

    void writePacket(ENetCommand command) {
        if (channelId == 0xff || command.getSendType() == ENetCommand.SendType.RELIABLE) {
            command.setReliableSequenceNumber(nextReliableSequenceNumber.incrementAndGet());
        } else {
            command.setReliableSequenceNumber(nextReliableSequenceNumber.get());
        }

        command.setChannelId(channelId);
        dispatch(command);
    }

    private ENetProtocolHeader makeProtoHeader() {
        return new ENetProtocolHeader(0, true, peer.getIncomingPeerId(), peer.connectionTime());
    }

    /**
     * <p>
     *     Writes the entire readable part of the buffer to the channel. The readerIndex of the buffer
     *     is modified and moved to the end of the buffer in the process.
     * </p>
     * <p>
     *     The data is fragmented if necessary and split up over several <code>SEND_FRAGMENT</code> packets.
     * </p>
     * <p>
     *     In all cases, an acknowledgement is requested by the peer. The data is retransmitted if the acknowledgement
     *     is not received within a certain timeframe.
     * </p>
     *
     * @param buf The data to send
     */
    public void sendReliable(ByteBuf buf) {
        if (peer.getMtu() < buf.readableBytes() + 14) { // Resulting packet length = readable bytes + up to 14 bytes for headers
            sendFragmented(buf);
            return;
        }

        SendReliable packet = new SendReliable(makeProtoHeader(), channelId, nextReliableSequenceNumber.incrementAndGet(), buf);
        dispatch(packet);
    }

    private void sendFragmented(ByteBuf buf) {
        int startSeqNum = nextReliableSequenceNumber.incrementAndGet();
        int totalLength = buf.readableBytes();
        long fragCount = (long) Math.ceil(((double) totalLength) / (peer.getMtu() - 14)); // Split the packet into units à (MTU - 14) bytes
        int lastLength = (int) (totalLength - ((fragCount - 1) * (peer.getMtu() - 14)));

        for (long i = 0; i < fragCount; i++) {
            int thisSeqNum = i == 0 ? startSeqNum : nextReliableSequenceNumber.incrementAndGet();
            int thisDataLength = (i == fragCount - 1) ? lastLength : peer.getMtu() - 14;
            long offset = i * (peer.getMtu() - 14);

            SendFragment packet = new SendFragment(makeProtoHeader(), channelId, thisSeqNum, startSeqNum, thisDataLength, fragCount, i, totalLength, offset, buf.slice((int) offset, thisDataLength));
            dispatch(packet);
        }
    }

    /**
     * <p>
     *     Writes the entire readable part of the buffer to the channel. The readerIndex of the buffer
     *     is modified and moved to the end of the buffer in the process.
     * </p>
     * <p>
     *     If the buffer length exceeds <code>peer.getMtu() - 14</code>, the write fails.
     * </p>
     * <p>
     *     No acknowledgement is requested, and no attempt is made to retransmit the data. Unreliable data
     *     may be dropped by discretion of the pipeline.
     * </p>
     * @param buf
     */
    public void sendUnreliable(ByteBuf buf) throws MtuExceededException {
        if (peer.getMtu() < buf.readableBytes() + 14) {
            throw new MtuExceededException(peer.getMtu() + " < " + (buf.readableBytes() + 14));
        }

        SendUnreliable packet = new SendUnreliable(makeProtoHeader(), channelId, nextReliableSequenceNumber.get(), nextUnreliableSequenceNumber.incrementAndGet(), buf);
        dispatch(packet);
    }


    void onFragmentReceived(SendFragment cmd) throws OutOfWindowException {
        int lastDeliveredReliableSeqNum;
        int cycle;
        synchronized (this) { // So concurrent dispatch doesn't mess us up
            lastDeliveredReliableSeqNum = this.lastDeliveredReliableSeqNum; // Copy so we have a local copy on the stack and concurrent modifications don't affect us
            cycle = this.cycle;
        }

        int startWindow = (int) (cmd.getStartSeqNum() / peer.getWindowSize());
        int currentWindow = (int) (lastDeliveredReliableSeqNum / peer.getWindowSize());

        if (cmd.getStartSeqNum() < lastDeliveredReliableSeqNum) {
            startWindow += Peer.MAX_RELIABLE_WINDOWS;
        }


        // Correctness checks

        // -> Packet in acceptable range
        if (startWindow < currentWindow || startWindow >= currentWindow + Peer.FREE_RELIABLE_WINDOWS - 1) {
            throw new OutOfWindowException("For fragment starting at " + cmd.getStartSeqNum() + ", startWindow=" + startWindow + " but currentWindow=" + currentWindow);
        }

        // -> Packet not too long
        if (cmd.getFragOffset() + cmd.getDataLength() > cmd.getTotalLength()) {
            throw new IllegalArgumentException("Packet too long: Total length is " + cmd.getTotalLength()
                    + " but this fragment increases length to " + cmd.getFragOffset() + "+" + cmd.getDataLength() + " = " + (cmd.getFragOffset() + cmd.getDataLength()));
        }

        // -> Not too many fragments in packet
        if (cmd.getFragNum() >= cmd.getFragCount()) {
            throw new IllegalArgumentException("Too many fragments: Packet has " + cmd.getFragCount() + " fragments, but received fragment " + cmd.getFragNum());
        }

        fragmentMap.putIfAbsent(cmd.getReliableSequenceNumber(), new FragmentAccumulator());
        FragmentAccumulator acc = fragmentMap.get(cmd.getReliableSequenceNumber());
        acc.add(cmd);

        if (acc.getFragCount() == cmd.getFragCount()) {
            fragmentMap.remove(cmd.getReliableSequenceNumber());
            queueDelivery(new WaitingData(cmd.getStartSeqNum(), cmd.getStartSeqNum() < lastDeliveredReliableSeqNum ? cycle + 1 : cycle, acc.byteBuf));
        }
    }

    void onReliableReceived(SendReliable cmd) throws OutOfWindowException {
        int lastDeliveredReliableSeqNum;
        int cycle;
        synchronized (this) { // So concurrent dispatch doesn't mess us up
            lastDeliveredReliableSeqNum = this.lastDeliveredReliableSeqNum; // Copy so we have a local copy on the stack and concurrent modifications don't affect us
            cycle = this.cycle;
        }

        int reliableSeqNum = cmd.getReliableSequenceNumber();
        int startWindow = (int) (reliableSeqNum / peer.getWindowSize());
        int currentWindow = (int) (lastDeliveredReliableSeqNum / peer.getWindowSize());

        if (reliableSeqNum < lastDeliveredReliableSeqNum) {
            startWindow += Peer.MAX_RELIABLE_WINDOWS;
        }


        // Correctness checks
        // -> Packet in acceptable range
        if (startWindow < currentWindow || startWindow >= currentWindow + Peer.FREE_RELIABLE_WINDOWS - 1) {
            throw new OutOfWindowException("For data with seq num " + reliableSeqNum + ", startWindow=" + startWindow + " but currentWindow=" + currentWindow);
        }

        queueDelivery(new WaitingData(reliableSeqNum, reliableSeqNum < lastDeliveredReliableSeqNum ? this.cycle + 1 : this.cycle, cmd.getData()));
    }

    @Synchronized
    void onUnreliableReceived(SendUnreliable cmd) {
        // TODO relation of unreliable commands to reliable seq num

        // Case a) lastDelivered <= cmd.seqnum <= maxFutureSeqNum
        //      b) lastDelivered <= cmd.seqnum, but maxFutureSeqNum overflows -> everything up to 0xFFFF is still in range
        if (cmd.getUnreliableSeqNum() < lastDeliveredUnreliableSeqNum + Short.MAX_VALUE ||
                cmd.getUnreliableSeqNum() > lastDeliveredUnreliableSeqNum && cmd.getUnreliableSeqNum() < 0xFFFF) {
            deliverUnreliable(cmd);
        }
    }

    private void deliverUnreliable(SendUnreliable cmd) {
        if (unreliableDataCallback != null) {
            unreliableDataCallback.accept(cmd.getData());
        } else if (unifiedDataCallback != null) {
            unifiedDataCallback.accept(cmd.getData());
        } else if (peer.getDataCallback() != null) {
            peer.getDataCallback().accept(cmd.getData());
        }
    }

    void acknowledge(ENetCommand cmd) {
        dispatch(new Acknowledge(makeProtoHeader(), channelId, nextReliableSequenceNumber.get(), ENetCommand.SendType.UNRELIABLE, cmd.getReliableSequenceNumber(), peer.connectionTime()));
    }


    /**
     * Accumulates fragments of a larger packets while storing which fragments have been received already
     */
    private class FragmentAccumulator {
        private ByteBuf byteBuf = Unpooled.buffer();
        private Set<Long> receivedFragments = new HashSet<>();

        public synchronized void add(SendFragment fragment) {
            byteBuf.writerIndex((int) fragment.getFragOffset());
            byteBuf.writeBytes(fragment.getData(), 0, fragment.getDataLength());

            receivedFragments.add(fragment.getFragNum());
        }

        public int getFragCount() {
            return receivedFragments.size();
        }
    }

    /**
     * Container class for data waiting to be delivered to the user
     */
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"seqnum", "cycle"})
    private class WaitingData implements Comparable<WaitingData> {
        @Getter private int seqnum;
        private int cycle;
        private ByteBuf buffer;

        @Override
        public int compareTo(WaitingData o) {
            if (this.equals(o)) {
                return 0;
            }

            if (this.seqnum > o.seqnum && this.cycle >= o.cycle) {
                return 1;
            }

            return -1;
        }
    }
}
