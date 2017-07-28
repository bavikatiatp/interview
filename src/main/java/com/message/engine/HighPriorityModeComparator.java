package com.message.engine;

import java.util.Comparator;

/**
 * Created by bharath on 7/28/17.
 */
public class HighPriorityModeComparator implements Comparator<Message> {

    public static final HighPriorityModeComparator COMPARATOR = new HighPriorityModeComparator();

    @Override
    public int compare(Message o1, Message o2) {
        int priorityComparison = o2.getPriority().compareTo(o1.getPriority());

        if(priorityComparison != 0) {
            return priorityComparison;
        }

        return NoPriorityModeComparator.COMPARATOR.compare(o1, o2);

    }
}
