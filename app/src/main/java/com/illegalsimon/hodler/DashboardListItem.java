package com.illegalsimon.hodler;

import java.util.Date;

/**
 * Created by shens on 11/8/2017.
 */

public abstract class DashboardListItem {
    public enum Type {
        LABEL(0), BALANCE(1), ORDER(2);

        private final int mValue;

        Type(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }
    }

    Type mType;
}

class DashboardLabel extends DashboardListItem {
    String mLabel;

    public DashboardLabel(Type type, String label) {
        mType = type;
        mLabel = label;
    }
}

class DashboardBalance extends DashboardListItem {
    String mSymbol;
    String mAvailable;
    String mBalance;

    public DashboardBalance(Type type, String symbol, String available, String balance) {
        mType = type;
        mSymbol = symbol;
        mAvailable = available;
        mBalance = balance;
    }
}

class DashboardOrder extends DashboardListItem {
    Date mPlacedDate;
    String mDescription;
    double mStatus;

    public DashboardOrder(Type type, Date placedDate, String description, double status) {
        mType = type;
        mPlacedDate = placedDate;
        mDescription = description;
        mStatus = status;
    }
}