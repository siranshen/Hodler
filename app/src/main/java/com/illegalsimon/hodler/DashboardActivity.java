package com.illegalsimon.hodler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.illegalsimon.hodler.data.Symbol;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    List<DashboardListItem> mListItems = new ArrayList<DashboardListItem>() {{
        add(new DashboardLabel(DashboardListItem.Type.LABEL, "account balances"));
        add(new DashboardBalance(DashboardListItem.Type.BALANCE, Symbol.USD.getName(), "0.0", "0.0"));
        add(new DashboardBalance(DashboardListItem.Type.BALANCE, Symbol.BTC.getName(), "0.0", "0.0"));
        add(new DashboardBalance(DashboardListItem.Type.BALANCE, Symbol.ETH.getName(), "0.0", "0.0"));
        add(new DashboardLabel(DashboardListItem.Type.LABEL, "open orders"));
        add(new DashboardOrder(DashboardListItem.Type.ORDER, new Date(), "Limit buy order for 43.64 ETH @ $293.20", 0));
        add(new DashboardOrder(DashboardListItem.Type.ORDER, new Date(), "Maker-or-Cancel buy order for 1111.643626 ETH @ $293.20", 0));
        add(new DashboardOrder(DashboardListItem.Type.ORDER, new Date(), "Limit buy order for 1111.643 ETH @ $293.20", 0));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_dashboard));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Dashboard");
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_dashboard);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        DashboardAdapter adapter = new DashboardAdapter(mListItems);
        mRecyclerView.setAdapter(adapter);
    }
}

