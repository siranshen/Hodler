package com.illegalsimon.hodler;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.illegalsimon.hodler.dashboard.DashboardAdapter;
import com.illegalsimon.hodler.dashboard.DashboardBalance;
import com.illegalsimon.hodler.dashboard.DashboardLabel;
import com.illegalsimon.hodler.dashboard.DashboardListItem;
import com.illegalsimon.hodler.dashboard.DashboardMessage;
import com.illegalsimon.hodler.dashboard.DashboardOrder;
import com.illegalsimon.hodler.data.Symbol;
import com.illegalsimon.hodler.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity implements DashboardAdapter.OnClickHandler, LoaderManager.LoaderCallbacks<String[]> {

    private static final int MAIN_LOADER_ID = 3;
    private static final int PAST_ORDERS_LOADER_ID = 4;
    private static final String[] TRADING_SYMBOLS = new String[] { "btcusd", "ethusd", "ethbtc" };

    private ProgressBar mLoadingIndicator;
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

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
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
        listItems.add(listItems.size() - 1, new DashboardMessage(na));
        listItems.add(new DashboardMessage(na));
        mDashboardAdapter = new DashboardAdapter(this);
        mDashboardAdapter.setListItems(listItems);
        mRecyclerView.setAdapter(mDashboardAdapter);
        getSupportLoaderManager().initLoader(MAIN_LOADER_ID, null, this);
    }

    private static List<DashboardListItem> buildBaseDashboardListItems(Map<String, String> availableBalances, Map<String, String> totalBalances) {
        List<DashboardListItem> listItems = new LinkedList<>();
        listItems.add(new DashboardLabel("account balances"));
        for (Map.Entry<String, String> totalBalance : totalBalances.entrySet()) {
            String symbol = totalBalance.getKey();
            listItems.add(new DashboardBalance(symbol, availableBalances.get(symbol), totalBalance.getValue()));
        }
        listItems.add(new DashboardLabel("open orders"));
        listItems.add(new DashboardLabel("past orders"));
        listItems.add(new DashboardMessage("Load", true));
        return listItems;
    }

    @Override
    public void handleOnClickOrder(Date orderDate, String description, String status, boolean isPast) {
        // TODO: Create fragment
        Toast.makeText(this, orderDate.toString() + " " + description + (isPast ? "" : " " + String.valueOf(status)), Toast.LENGTH_LONG).show();
    }

    @Override
    public void handleOnClickLoadPastOrders() {
        if (getSupportLoaderManager().getLoader(PAST_ORDERS_LOADER_ID) == null) {
            getSupportLoaderManager().initLoader(PAST_ORDERS_LOADER_ID, null, this);
        } else {
            getSupportLoaderManager().restartLoader(PAST_ORDERS_LOADER_ID, null, this);
        }
    }

    @Override
    public Loader<String[]> onCreateLoader(int id, Bundle args) {
        if (MAIN_LOADER_ID == id) {
            return new AsyncTaskLoader<String[]>(this) {

                @Override
                protected void onStartLoading() {
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }

                @Override
                public String[] loadInBackground() {
                    try {
                        String balancesResponse = NetworkUtils.getGeminiPrivateResponseFromUrl("/v1/balances", new JSONObject());
                        String activeOrdersResponse = NetworkUtils.getGeminiPrivateResponseFromUrl("/v1/orders", new JSONObject());
                        return new String[] { balancesResponse, activeOrdersResponse };
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };
        } else if (PAST_ORDERS_LOADER_ID == id) {
            return new AsyncTaskLoader<String[]>(this) {

                @Override
                protected void onStartLoading() {
                    forceLoad();
                }

                @Override
                public String[] loadInBackground() {
                    JSONObject jsonRequest = new JSONObject();
                    try {
                        String[] result = new String[TRADING_SYMBOLS.length];
                        for (int i = 0; i < TRADING_SYMBOLS.length; i++) {
                            jsonRequest.put("symbol", TRADING_SYMBOLS[i]);
                            jsonRequest.put("limit_trades", 500);
                            result[i] = NetworkUtils.getGeminiPrivateResponseFromUrl("/v1/mytrades", jsonRequest);
                        }
                        return result;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };
        }
        throw new IllegalArgumentException("Loader not supported");
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] data) {
        if (data == null) {
            Toast.makeText(this, "Failed to load...", Toast.LENGTH_LONG).show();
            return;
        }

        if (MAIN_LOADER_ID == loader.getId()) {
            if (data.length < 2) {
                Toast.makeText(this, "Something went very wrong.", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                System.out.println(data[0]);
                System.out.println(data[1]);
                List<DashboardListItem> listItems = new ArrayList<>();
                listItems.add(new DashboardLabel("account balances"));
                JSONArray balances = new JSONArray(data[0]);
                for (int i = 0; i < balances.length(); i++) {
                    JSONObject balance = balances.getJSONObject(i);
                    listItems.add(new DashboardBalance(balance.getString("currency"), balance.getString("available"), balance.getString("amount")));
                }
                listItems.add(new DashboardLabel("open orders"));
                JSONArray orders = new JSONArray(data[1]);
                if (0 == orders.length()) {
                    listItems.add(new DashboardMessage("No open orders"));
                } else {
                    for (int i = 0; i < orders.length(); i++) {
                        JSONObject order = orders.getJSONObject(i);
                        listItems.add(new DashboardOrder(new Date(order.getLong("timestampms")), parseDescription(order), parseStatus(order), false));
                    }
                }
                listItems.add(new DashboardLabel("past orders"));
                listItems.add(new DashboardMessage("Load", true));
                mDashboardAdapter.setListItems(listItems);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else if (PAST_ORDERS_LOADER_ID == loader.getId()) {
            // TODO
            System.out.println(data[0]);
            System.out.println(data[1]);
            System.out.println(data[2]);
        }
    }

    @Override
    public void onLoaderReset(Loader<String[]> loader) {}

    private String parseStatus(JSONObject order) throws JSONException {
        String type = order.getString("type");
        if (type.equals("exchange limit")) {
            double filled = order.getDouble("executed_amount") / order.getDouble("original_amount");
            return String.format(Locale.US, "%.2f%%", filled);
        } else if (type.startsWith("auction-only")){
            return "Waiting for auction";
        }
        return "Unknown";
    }

    private String parseDescription(JSONObject order) throws JSONException {
        String symbols = order.getString("symbol").toUpperCase();
        String fromSymbol = symbols.substring(0, 3);
        String toSymbol = symbols.substring(3);

        String type = order.getString("type");
        if (type.equals("exchange limit")) {
            JSONArray options = order.getJSONArray("options");
            String orderType = options.length() == 0 ? "limit" : options.getString(0);
            orderType = orderType.substring(0, 1).toUpperCase() + orderType.substring(1);
            String side = order.getString("side");
            String amount = order.getString("original_amount");
            String price = order.getString("price");
            if ("USD".equals(toSymbol)) {
                price = "$" + price;
            } else {
                price += " " + toSymbol;
            }
            return String.format("%s %s order for %s %s @ %s", orderType, side, amount, fromSymbol, price);
        } else if (type.startsWith("auction-only")) {
            type = type.substring(0, 1).toUpperCase() + type.substring(1);
            String totalSpend = order.getString("total_spend");
            if ("USD".equals(toSymbol)) {
                totalSpend = "$" + totalSpend;
            } else {
                totalSpend += " " + toSymbol;
            }
            return String.format("%s of %s for %s", type, fromSymbol, totalSpend);
        }
        return "Mysterious order";
    }
}
