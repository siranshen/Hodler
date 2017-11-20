package com.illegalsimon.hodler.dashboard;

import java.io.Serializable;

/**
 * Created by shens on 11/8/2017.
 */

public abstract class DashboardListItem implements Serializable {
    public static final int LABEL = 0;
    public static final int BALANCE = 1;
    public static final int ORDER = 2;
    public static final int MESSAGE = 3;

    final int type;

    protected DashboardListItem(int type) {
        this.type = type;
    }
}

