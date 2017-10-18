package com.illegalsimon.hodler.data;

/**
 * Created by shens on 10/15/2017.
 */

public class PriceOverview {

    private float mCurrentPrice;
    private float mOpenPrice; // The price at the start of the period
    private TimeRange mTimeRange;
    private TimePrice[] mPriceHistory;

    public PriceOverview(float currentPrice, TimeRange timeRange, TimePrice[] priceHistory) {
        mCurrentPrice = currentPrice;
        mTimeRange = timeRange;
        mPriceHistory = priceHistory;
        if (priceHistory.length > 0) {
            mOpenPrice = priceHistory[0].getPrice();
        }
    }

    public float getCurrentPrice() {
        return mCurrentPrice;
    }

    public float getOpenPrice() {
        return mOpenPrice;
    }

    public TimeRange getTimeRange() {
        return mTimeRange;
    }

    public TimePrice[] getPriceHistory() {
        return mPriceHistory;
    }
}
