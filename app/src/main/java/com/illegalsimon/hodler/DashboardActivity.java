package com.illegalsimon.hodler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.illegalsimon.hodler.dashboard.DashboardAdapter;
import com.illegalsimon.hodler.dashboard.DashboardBalance;
import com.illegalsimon.hodler.dashboard.DashboardLabel;
import com.illegalsimon.hodler.dashboard.DashboardListItem;
import com.illegalsimon.hodler.dashboard.DashboardMessage;
import com.illegalsimon.hodler.dashboard.DashboardOrder;
import com.illegalsimon.hodler.data.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private DashboardAdapter mDashboardAdapter;

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

        Map<String, String> availableBalances = new HashMap<>();
        Map<String, String> totalBalances = new HashMap<>();
        String na = "N/A";
        for (Symbol symbol : Symbol.values()) {
            availableBalances.put(symbol.getName(), na);
            totalBalances.put(symbol.getName(), na);
        }
        List<DashboardListItem> listItems = buildBaseDashboardListItems(availableBalances, totalBalances);
        listItems.add(new DashboardMessage(na));
        mDashboardAdapter = new DashboardAdapter(listItems);
        mRecyclerView.setAdapter(mDashboardAdapter);
    }

    private static List<DashboardListItem> buildBaseDashboardListItems(Map<String, String> availableBalances, Map<String, String> totalBalances) {
        List<DashboardListItem> listItems = new ArrayList<>();
        listItems.add(new DashboardLabel("account balances"));
        for (Map.Entry<String, String> totalBalance : totalBalances.entrySet()) {
            String symbol = totalBalance.getKey();
            listItems.add(new DashboardBalance(symbol, availableBalances.get(symbol), totalBalance.getValue()));
        }
        listItems.add(new DashboardLabel("open orders"));
        return listItems;
    }
}
