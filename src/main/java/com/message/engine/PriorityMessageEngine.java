package com.message.engine;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by bharath on 7/27/17.
 */
public class PriorityMessageEngine {

    private static final int DEFAULT_CAPACITY_SIZE = 11;

    private BlockingQueue<Message> queue;
    private AtomicLong threadsCount;
    private WaitForCondition waitForCondition;

    private CountDownLatch latch;

    private boolean highPriorityMode;

    public PriorityMessageEngine() {
        queue = new PriorityBlockingQueue<>(DEFAULT_CAPACITY_SIZE, NoPriorityModeComparator.COMPARATOR);
        latch = new CountDownLatch(0);
        threadsCount = new AtomicLong(0);
        waitForCondition = new WaitForCondition(() -> threadsCount.get() <= 0);
    }

    public void setHighPriorityMode(boolean highPriorityMode) {
        if(this.highPriorityMode == highPriorityMode){
            return;
        }
        this.highPriorityMode = highPriorityMode;
        latch = new CountDownLatch(1);
        waitForCondition.await();
        refreshQueuePriority();
    }

    /**
     * This is the expensive operation in the entire implementation of this MessageQueue Functionality. I assumed that the switching between
     * high priority mode and regular mode will happen very rarely.
     */
    private synchronized void refreshQueuePriority() {
        BlockingQueue<Message> newQueue;
        if(highPriorityMode) {
            newQueue = new PriorityBlockingQueue<>(DEFAULT_CAPACITY_SIZE, HighPriorityModeComparator.COMPARATOR);
        } else {
            newQueue = new PriorityBlockingQueue<>(DEFAULT_CAPACITY_SIZE, NoPriorityModeComparator.COMPARATOR);
        }
        this.queue.drainTo(newQueue);
        this.queue = newQueue;
        latch.countDown();
    }

    public boolean getHighPriorityMode() {
        return highPriorityMode;
    }

    public void put(Message message) throws InterruptedException {
        latch.await();
        threadsCount.incrementAndGet();
        queue.put(message);
        threadsCount.decrementAndGet();
    }

    public Message get() throws InterruptedException {
        return get(2, TimeUnit.SECONDS);
    }

    public Message get(long time, TimeUnit timeUnit) throws InterruptedException {
        latch.await();
        threadsCount.incrementAndGet();
        Message msg = queue.poll(time, timeUnit);
        threadsCount.decrementAndGet();
        return msg;
    }

}