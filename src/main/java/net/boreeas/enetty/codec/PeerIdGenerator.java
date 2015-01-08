package net.boreeas.enetty.codec;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Malte Schütze
 */
public class PeerIdGenerator {
    private ArrayBlockingQueue<Integer> ids = new ArrayBlockingQueue<Integer>(0x80);
    {
        for (int i = 0; i < 0x80; i++) ids.offer(i);
    }

    public int next(int timeout) throws InterruptedException {
        return ids.poll(timeout, TimeUnit.MILLISECONDS);
    }

    public void returnId(int id) {
        ids.offer(id);
    }
}
