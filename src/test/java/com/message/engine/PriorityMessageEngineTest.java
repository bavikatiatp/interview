package com.message.engine;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by bharath on 7/27/17.
 */
public class PriorityMessageEngineTest {

    private PriorityMessageEngine underTest;

    @Before
    public void setUp() {
        underTest = new PriorityMessageEngine();
    }

    @Test
    public void shouldSetTheGivenPriorityModeFlag() {
        underTest.setHighPriorityMode(true);
        assertTrue(underTest.getHighPriorityMode());
    }

    @Test
    public void underHighPriorityModeShouldInsertMessage() throws InterruptedException {
        Message message = new Message();
        underTest.setHighPriorityMode(true);
        underTest.put(message);
        assertEquals(message, underTest.get());
    }

    @Test
    public void underNoPriorityModeShouldInsertMessage() throws InterruptedException {
        Message message = new Message();
        underTest.put(message);
        assertEquals(message, underTest.get());
    }

    @Test
    public void underHighPriorityModeMessageShouldConsumeAccordingToThePriority() throws InterruptedException {
        int highPriority = 10;
        int lowPriority = 3;
        Message message = prepareMessage(lowPriority);
        Message message1 = prepareMessage(highPriority);

        underTest.setHighPriorityMode(true);
        underTest.put(message);
        underTest.put(message1);

        assertEquals(message1, underTest.get());
        assertEquals(message, underTest.get());
    }

    @Test
    public void underHighPriorityModeMessageShouldConsumeAccordingToThePriorityAnsDate() throws InterruptedException {
        int highPriority = 10;
        int lowPriority = 3;
        Date date = new Date();

        Date dateWithOneHourFuture = new Date(date.getTime() + 360000);
        Date dateWithOneHourBack = new Date(date.getTime() - 360000);
        Date dateWithHalfNHourBack = new Date(date.getTime() - 150000);

        Message messageOne = prepareMessage(highPriority, dateWithOneHourBack);
        Message messageTwo = prepareMessage(highPriority, dateWithOneHourFuture);
        Message messageThree = prepareMessage(highPriority, dateWithHalfNHourBack);
        Message messageFour = prepareMessage(lowPriority, new Date());

        underTest.setHighPriorityMode(true);
        underTest.put(messageOne);
        underTest.put(messageTwo);
        underTest.put(messageThree);
        underTest.put(messageFour);

        assertEquals(messageOne, underTest.get());
        assertEquals(messageThree, underTest.get());
        assertEquals(messageTwo, underTest.get());
        assertEquals(messageFour, underTest.get());
    }

    @Test
    public void underConstantPriorityModeMessageShouldConsumeAccordingToTheDate() throws InterruptedException {
        int priority = 5;
        Date date = new Date();

        Date dateWithOneHourFuture = new Date(date.getTime() + 360000);
        Date dateWithOneHourBack = new Date(date.getTime() - 360000);
        Date dateWithHalfNHourBack = new Date(date.getTime() - 150000);

        Message messageOne = prepareMessage(priority, dateWithOneHourBack);
        Message messageTwo = prepareMessage(priority, dateWithOneHourFuture);
        Message messageThree = prepareMessage(priority, dateWithHalfNHourBack);

        underTest.put(messageOne);
        underTest.put(messageTwo);
        underTest.put(messageThree);

        assertEquals(messageOne, underTest.get());
        assertEquals(messageThree, underTest.get());
        assertEquals(messageTwo, underTest.get());
    }

    @Test
    public void switchedBetweenModesInTheMiddleOfConsumption() throws InterruptedException {
        int highPriority = 9;
        int mediumPriority = 5;
        int lowPriority = 2;
        Date date = new Date();

        Date dateWithOneHourFuture = new Date(date.getTime() + 360000);
        Date dateWithOneHourBack = new Date(date.getTime() - 360000);
        Date dateWithHalfNHourBack = new Date(date.getTime() - 150000);

        Message highPriorityMessageWithOneHourBackDate = prepareMessage(highPriority, dateWithOneHourBack);
        Message highPriorityMessageWithFutureDate = prepareMessage(highPriority, dateWithOneHourFuture);
        Message highPriorityMessageWithHalfNHourBackDate  = prepareMessage(lowPriority, dateWithHalfNHourBack);

        Message mediumPriorityMessageWithOneHourBackDate = prepareMessage(mediumPriority, dateWithOneHourBack);
        Message mediumPriorityMessageWithFutureDate = prepareMessage(mediumPriority, dateWithOneHourFuture);
        Message mediumPriorityMessageWithHalfNHourBackDate  = prepareMessage(mediumPriority, dateWithHalfNHourBack);

        Message lowPriorityMessageWithOneHourBackDate = prepareMessage(lowPriority, dateWithOneHourBack);
        Message lowPriorityMessageWithFutureDate = prepareMessage(lowPriority, dateWithOneHourFuture);
        Message lowPriorityMessageWithHalfNHourBackDate  = prepareMessage(lowPriority, dateWithHalfNHourBack);

        underTest.put(highPriorityMessageWithOneHourBackDate);
        underTest.put(highPriorityMessageWithFutureDate);
        underTest.put(highPriorityMessageWithHalfNHourBackDate);
        underTest.put(mediumPriorityMessageWithOneHourBackDate);
        underTest.put(mediumPriorityMessageWithFutureDate);
        underTest.put(mediumPriorityMessageWithHalfNHourBackDate);
        underTest.put(lowPriorityMessageWithOneHourBackDate);
        underTest.put(lowPriorityMessageWithFutureDate);
        underTest.put(lowPriorityMessageWithHalfNHourBackDate);

        assertEquals(highPriorityMessageWithOneHourBackDate, underTest.get());
        assertEquals(mediumPriorityMessageWithOneHourBackDate, underTest.get());
        assertEquals(lowPriorityMessageWithOneHourBackDate, underTest.get());
        assertEquals(highPriorityMessageWithHalfNHourBackDate, underTest.get());

        underTest.setHighPriorityMode(true);

        assertEquals(highPriorityMessageWithFutureDate, underTest.get());
        assertEquals(mediumPriorityMessageWithHalfNHourBackDate, underTest.get());
        assertEquals(mediumPriorityMessageWithFutureDate, underTest.get());
        assertEquals(lowPriorityMessageWithHalfNHourBackDate, underTest.get());
        assertEquals(lowPriorityMessageWithFutureDate, underTest.get());
    }

    private Message prepareMessage(int priority) {
        return prepareMessage(priority, new Date());
    }

    private Message prepareMessage(int priority, Date date) {
        Message message = new Message();
        message.setPriority(priority);
        message.setDate(date);
        return message;
    }

}
