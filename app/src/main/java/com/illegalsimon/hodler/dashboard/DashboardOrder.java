package com.illegalsimon.hodler.dashboard;

import java.util.Date;

public class DashboardOrder extends DashboardListItem {
    final String orderId;
    final Date placedDate;
    final String description;
    final String status;
    final boolean isPast;

    public DashboardOrder(String orderId, Date placedDate, String description, String status, boolean isPast) {
        super(DashboardListItem.ORDER);
        this.orderId = orderId;
        this.placedDate = placedDate;
        this.description = description;
        this.status = status;
        this.isPast = isPast;
    }
}