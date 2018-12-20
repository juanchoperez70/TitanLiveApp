package com.bpt.tipi.streaming.model;

public class CycleCountWeekDay {

    private int weekday;
    private String lastDate;
    private float weekdayCycleCount;
    private int numOfWeekdays;

    public int getWeekday() {
        return weekday;
    }

    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }

    public String getLastDate() {
        return lastDate;
    }

    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }

    public float getWeekdayCycleCount() {
        return weekdayCycleCount;
    }

    public void setWeekdayCycleCount(float weekdayCycleCount) {
        this.weekdayCycleCount = weekdayCycleCount;
    }

    public int getNumOfWeekdays() {
        return numOfWeekdays;
    }

    public void setNumOfWeekdays(int numOfWeekdays) {
        this.numOfWeekdays = numOfWeekdays;
    }
}
