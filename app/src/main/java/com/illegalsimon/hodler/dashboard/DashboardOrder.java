package com.illegalsimon.hodler.dashboard;

import java.util.Date;

public class DashboardOrder extends DashboardListItem {
    final Date mPlacedDate;
    final String mDescription;
    final double mStatus;

    public DashboardOrder(Date placedDate, String description, double status) {
        super(DashboardListItem.ORDER);
        mPlacedDate = placedDate;
        mDescription = description;
        mStatus = status;
    }
}