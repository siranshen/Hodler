package com.illegalsimon.hodler.dashboard;

public class DashboardBalance extends DashboardListItem {
    final String mSymbol;
    final String mAvailable;
    final String mBalance;

    public DashboardBalance(String symbol, String available, String balance) {
        super(DashboardListItem.BALANCE);
        mSymbol = symbol;
        mAvailable = available;
        mBalance = balance;
    }
}

