package com.illegalsimon.hodler.dashboard;

import java.util.Date;

public class DashboardOrder extends DashboardListItem {
    final Date placedDate;
    final String description;
    final String status;
    final boolean isPast;

    public DashboardOrder(Date placedDate, String description, String status, boolean isPast) {
        super(DashboardListItem.ORDER);
        this.placedDate = placedDate;
        this.description = description;
        this.status = status;
        this.isPast = isPast;
    }
}