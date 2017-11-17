package com.illegalsimon.hodler.dashboard;

public class DashboardBalance extends DashboardListItem {
    final String symbol;
    final String available;
    final String balance;

    public DashboardBalance(String symbol, String available, String balance) {
        super(DashboardListItem.BALANCE);
        this.symbol = symbol;
        this.available = available;
        this.balance = balance;
    }
}

