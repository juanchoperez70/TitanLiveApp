package com.bpt.tipi.streaming.model;

public class CycleCount {
    private String date;
    private float cycleCount;

    public CycleCount(String str, float f) {
        this.date = str;
        this.cycleCount = f;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getCycleCount() {
        return cycleCount;
    }

    public void setCycleCount(float cycleCount) {
        this.cycleCount = cycleCount;
    }
}
