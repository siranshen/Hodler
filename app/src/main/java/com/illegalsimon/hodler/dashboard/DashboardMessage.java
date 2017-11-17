package com.illegalsimon.hodler.dashboard;

/**
 * Created by shens on 11/14/2017.
 */

public class DashboardMessage extends DashboardListItem {
    final String message;
    final boolean isPastOrdersLoader;

    public DashboardMessage(String message) {
        this(message, false);
    }

    public DashboardMessage(String message, boolean isPastOrdersLoader) {
        super(DashboardListItem.MESSAGE);
        this.message = message;
        this.isPastOrdersLoader = isPastOrdersLoader;
    }
}
