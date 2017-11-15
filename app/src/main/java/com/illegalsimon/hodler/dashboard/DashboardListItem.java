package com.illegalsimon.hodler.dashboard;

/**
 * Created by shens on 11/8/2017.
 */

public abstract class DashboardListItem {
    public static final int LABEL = 0;
    public static final int BALANCE = 1;
    public static final int ORDER = 2;
    public static final int MESSAGE = 3;

    final int mType;

    protected DashboardListItem(int type) {
        mType = type;
    }
}

