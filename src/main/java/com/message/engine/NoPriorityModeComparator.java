package com.message.engine;

import java.util.Comparator;

/**
 * Created by bharath on 7/28/17.
 */
public class NoPriorityModeComparator implements Comparator<Message> {

    public static final NoPriorityModeComparator COMPARATOR = new NoPriorityModeComparator();

    @Override
    public int compare(Message o1, Message o2) {
        return o1.getDate().compareTo(o2.getDate());
    }
}
