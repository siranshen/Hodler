package com.illegalsimon.hodler.dashboard;

public class DashboardLabel extends DashboardListItem {
    final String label;

    public DashboardLabel(String label) {
        super(DashboardListItem.LABEL);
        this.label = label;
    }
}

