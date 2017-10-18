package com.illegalsimon.hodler.data;

/**
 * Created by shens on 10/15/2017.
 */

public final class TimePrice {
    private final long timestamp;
    private final float price;

    public TimePrice(long timestamp, float price) {
        this.timestamp = timestamp;
        this.price = price;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public float getPrice() {
        return price;
    }
}
