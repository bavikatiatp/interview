package com.message.engine;

/**
 * Created by bharath on 7/28/17.
 */
public class WaitForCondition {

    private Condition condition;

    WaitForCondition(Condition condition) {
        this.condition = condition;
    }

    public void await() {
        while(true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            if(condition.isSatisfied()) {
                return;
            }
        }
    }
}
