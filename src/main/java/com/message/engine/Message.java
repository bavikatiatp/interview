package com.message.engine;

import java.util.Date;

/**
 * Created by bharath on 7/27/17.
 */
public class Message {

    private Integer priority;
    private Date date;

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getPriority() {
        return priority;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String toString() {
        return "Priority: "+ getPriority() +", Date: "+ date.toString();
    }
}
