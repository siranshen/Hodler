package com.illegalsimon.hodler.data;

/**
 * Created by shens on 10/14/2017.
 */

public enum Symbol {
    BTC("BTC"),
    ETH("ETH"),
    XRP("XRP"),
    USD("USD");

    private final String name;

    Symbol(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}