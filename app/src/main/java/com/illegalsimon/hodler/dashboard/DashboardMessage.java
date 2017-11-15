package com.illegalsimon.hodler.dashboard;

/**
 * Created by shens on 11/14/2017.
 */

public class DashboardMessage extends DashboardListItem {
    final String mMessage;

    public DashboardMessage(String message) {
        super(DashboardListItem.MESSAGE);
        mMessage = message;
    }
}
