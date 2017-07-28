package com.message.engine;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.Test;

import java.sql.Time;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by bharath on 7/27/17.
 */
public class PriorityMessageEnginePerformanceTest {

    private PriorityMessageEngine underTest;
    // Used for message producer metrics purpose
    private AtomicLong producerCount;
    // Used for message consumer metrics purpose
    private AtomicLong consumerCount;

    private static final int NUM_OF_MSG_GENERATED_PER_PRODUCER = 50000;

    @Before
    public void setUp() {
        underTest = new PriorityMessageEngine();
        producerCount = new AtomicLong();
        consumerCount = new AtomicLong();
    }

    /**
     * This is performance test by generating 10 million dummy messages using 200 parallel producers in ~5 seconds.
     * I haven't asserted any SLA's.
     * @throws InterruptedException
     */
    @Test
    public void performanceTestOnGeneratingMessages() throws InterruptedException {
        int numOfProducers = 200;
        CountDownLatch startAllProducersLatch = new CountDownLatch(1);
        ExecutorService producerExecutorService = Executors.newFixedThreadPool(numOfProducers);
        for(int i =0 ;i < numOfProducers; i++) {
            producerExecutorService.submit(new MessageGenerator(underTest, startAllProducersLatch));
        }
        StopWatch stopWatch = StopWatch.createStarted();
        startAllProducersLatch.countDown();

        producerExecutorService.shutdown();
        producerExecutorService.awaitTermination(50, TimeUnit.SECONDS);
        System.out.println("Generating 10 million messages taken :"+ stopWatch.getTime(TimeUnit.SECONDS)+ " seconds");
    }

    /**
     * This is performance test by generating 5 million messages with 100 parallel producers and 30 consumers.
     * Entire operation taken average 10 seconds in a laptop with i7 Quad core processor MacBook Pro.
     * I haven't asserted any SLA's.
     * @throws InterruptedException
     */
    @Test
    public void overAllPerformanceTest() throws InterruptedException {
        int numOfProducers = 100;
        int numOfConsumers = 30;

        CountDownLatch startAllProducersLatch = new CountDownLatch(1);
        ExecutorService producerExecutorService = Executors.newFixedThreadPool(numOfProducers);
        ExecutorService consumerExecutorService = Executors.newFixedThreadPool(numOfConsumers);
        ScheduledExecutorService switchPriorityModeService = Executors.newScheduledThreadPool(1);
        ScheduledExecutorService producerCountPrinter = Executors.newScheduledThreadPool(1);
        ScheduledExecutorService consumerCountPrinter = Executors.newScheduledThreadPool(1);

        CountDownLatch waitForMessageConsumptionLatch = new CountDownLatch(numOfProducers*NUM_OF_MSG_GENERATED_PER_PRODUCER);

        for(int i =0 ;i < numOfProducers; i++) {
            producerExecutorService.submit(new MessageGenerator(underTest, startAllProducersLatch));
        }

        PriorityModeSwitcher switcher = new PriorityModeSwitcher(underTest, startAllProducersLatch);
        switchPriorityModeService.scheduleAtFixedRate(switcher, 2,4, TimeUnit.SECONDS);

        for(int i=0; i < numOfConsumers; i++) {
            consumerExecutorService.submit(new MessageConsumer(underTest, startAllProducersLatch, waitForMessageConsumptionLatch));
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Number of Messages Generated/Consumed/Collected Items/Mode: "+producerCount.get()+"/"+consumerCount.get()+"/"
                        + (numOfProducers * NUM_OF_MSG_GENERATED_PER_PRODUCER - waitForMessageConsumptionLatch.getCount())+"/"+underTest.getHighPriorityMode());
            }
        }, 0, 1000);


        StopWatch stopWatch = StopWatch.createStarted();
        startAllProducersLatch.countDown();

        waitForMessageConsumptionLatch.await();

        System.out.println("Total time taken to perform the operation: "+ stopWatch.getTime(TimeUnit.SECONDS) +" seconds");

        System.out.println("Final Producer Count: "+ producerCount.get());
        System.out.println("Final Consumer Count: "+ consumerCount.get());

        producerExecutorService.shutdown();
        consumerExecutorService.shutdown();
        switchPriorityModeService.shutdown();
        producerCountPrinter.shutdown();
        consumerCountPrinter.shutdown();
        timer.cancel();

    }


    /**
     * Helper class to generate the Messages
     */
    class MessageGenerator implements Runnable {

        private PriorityMessageEngine engine;
        private CountDownLatch latch;

        MessageGenerator(PriorityMessageEngine engine, CountDownLatch latch) {
            this.engine = engine;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                latch.await();
                for(int i = 0; i < NUM_OF_MSG_GENERATED_PER_PRODUCER; i++) {
                    Message message = new Message();
                    message.setPriority(i %10);
                    message.setDate(new Date());
                    engine.put(message);
                    producerCount.incrementAndGet();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Helper class to switch the priority mode during runtime
     */
    class PriorityModeSwitcher implements Runnable {

        private PriorityMessageEngine engine;
        private CountDownLatch latch;
        private boolean currentHighPriorityMode = false;

        PriorityModeSwitcher(PriorityMessageEngine engine, CountDownLatch latch){
            this.engine = engine;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                latch.await();
                currentHighPriorityMode = !currentHighPriorityMode;
                engine.setHighPriorityMode(currentHighPriorityMode);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Helper Test class to consume the generated messages.
     */
    class MessageConsumer implements Runnable {
        private PriorityMessageEngine engine;
        private CountDownLatch latch;
        private CountDownLatch waitForMessageConsumptionLatch;

        MessageConsumer(PriorityMessageEngine engine, CountDownLatch latch, CountDownLatch waitForMessageConsumptionLatch) {
            this.engine = engine;
            this.latch = latch;
            this.waitForMessageConsumptionLatch = waitForMessageConsumptionLatch;
        }

        @Override
        public void run() {
            try {
                latch.await();
                while(waitForMessageConsumptionLatch.getCount() > 0) {
                    Message msg = engine.get();
                    if(msg != null) {
                        consumerCount.incrementAndGet();
                        waitForMessageConsumptionLatch.countDown();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
