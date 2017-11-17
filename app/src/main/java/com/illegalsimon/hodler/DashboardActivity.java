package com.illegalsimon.hodler;

import android.content.DialogInterface;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.illegalsimon.hodler.dashboard.DashboardAdapter;
import com.illegalsimon.hodler.dashboard.DashboardBalance;
import com.illegalsimon.hodler.dashboard.DashboardLabel;
import com.illegalsimon.hodler.dashboard.DashboardListItem;
import com.illegalsimon.hodler.dashboard.DashboardMessage;
import com.illegalsimon.hodler.dashboard.DashboardOrder;
import com.illegalsimon.hodler.utils.JsonParser;
import com.illegalsimon.hodler.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity implements DashboardAdapter.OnClickHandler, LoaderManager.LoaderCallbacks<String[]> {

    private static final int MAIN_LOADER_ID = 3;
    private static final int PAST_ORDERS_LOADER_ID = 4;
    private static final int CANCEL_ORDER_LOADER_ID = 5;
    private static final String ORDER_ID_KEY = "ORDER_ID";
    private static final String USD = "USD";
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

        mDashboardAdapter = new DashboardAdapter(this);
        mRecyclerView.setAdapter(mDashboardAdapter);
        mDashboardAdapter.setListItems(new ArrayList<DashboardListItem>());
        getSupportLoaderManager().initLoader(MAIN_LOADER_ID, null, this);
    }

    @Override
    public void handleOnClickOrder(final String orderId, Date orderDate, String description, String status, boolean isPast) {
        String date = new SimpleDateFormat("M/d/YYYY HH:mm:ss", Locale.US).format(orderDate);
        if (isPast) {
            String message = date + "\n" + description;
            new AlertDialog.Builder(this, R.style.CustomAlertDialogStyle).setTitle("Past order").setMessage(message)
                    .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
        } else {
            String message = date + "\n" + description + "\n" + String.valueOf(status) + "\n\nChoose your action:";
            new AlertDialog.Builder(this, R.style.CustomAlertDialogStyle).setTitle("Open order").setMessage(message)
                    .setPositiveButton("Cancel order", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Bundle args = new Bundle();
                            args.putString(ORDER_ID_KEY, orderId);
                            if (getSupportLoaderManager().getLoader(CANCEL_ORDER_LOADER_ID) == null) {
                                getSupportLoaderManager().initLoader(CANCEL_ORDER_LOADER_ID, args, DashboardActivity.this);
                            } else {
                                getSupportLoaderManager().restartLoader(CANCEL_ORDER_LOADER_ID, args, DashboardActivity.this);
                            }
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("Just checking", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
        }
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
    public Loader<String[]> onCreateLoader(int id, final Bundle args) {
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
        } else if (CANCEL_ORDER_LOADER_ID == id) {
            return new AsyncTaskLoader<String[]>(this) {

                @Override
                protected void onStartLoading() {
                    forceLoad();
                }

                @Override
                public String[] loadInBackground() {
                    JSONObject jsonRequest = new JSONObject();
                    try {
                        jsonRequest.put("order_id", args.getString(ORDER_ID_KEY));
                        return new String[] { NetworkUtils.getGeminiPrivateResponseFromUrl("/v1/order/cancel", jsonRequest) };
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
            Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (loader.getId()) {
            case MAIN_LOADER_ID:
                try {
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
                            listItems.add(new DashboardOrder(order.getString("order_id"), new Date(order.getLong("timestampms")), parseOpenOrderDescription(order), parseStatus(order), false));
                        }
                    }
                    listItems.add(new DashboardLabel("past orders/trades"));
                    listItems.add(new DashboardMessage("\u25bc Tap to load", true));
                    mDashboardAdapter.setListItems(listItems);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mLoadingIndicator.setVisibility(View.INVISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);
                break;

            case PAST_ORDERS_LOADER_ID:
                try {
                    List<DashboardListItem> listItems = mDashboardAdapter.getListItems();
                    int listSize = listItems.size() - 1;
                    listItems.remove(listSize);

                    for (int i = 0; i < TRADING_SYMBOLS.length; i++) {
                        JSONArray orders = new JSONArray(data[i]);
                        for (int j = 0; j < orders.length(); j++) {
                            JSONObject order = orders.getJSONObject(j);
                            listItems.add(new DashboardOrder(order.getString("order_id"), new Date(order.getLong("timestampms")), parsePastOrderDescription(TRADING_SYMBOLS[i], order), null, true));
                        }
                    }

                    if (listSize == listItems.size()) {
                        listItems.add(new DashboardMessage("No past orders"));
                    }

                    mDashboardAdapter.setListItems(listItems);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case CANCEL_ORDER_LOADER_ID:
                try {
                    if (JsonParser.isOrderCancelled(data[0])) {
                        Toast.makeText(this, "Order has been successfully cancelled!", Toast.LENGTH_LONG).show();
                        refreshPage();
                    } else {
                        Toast.makeText(this, "Failed to cancel order...", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<String[]> loader) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            refreshPage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshPage() {
        if (getSupportLoaderManager().getLoader(MAIN_LOADER_ID) == null) {
            getSupportLoaderManager().initLoader(MAIN_LOADER_ID, null, this);
        } else {
            getSupportLoaderManager().restartLoader(MAIN_LOADER_ID, null, this);
        }
    }

    private String parseStatus(JSONObject order) throws JSONException {
        String type = order.getString("type");
        if (type.equals("exchange limit")) {
            double filled = order.getDouble("executed_amount") / order.getDouble("original_amount");
            return String.format(Locale.US, "%.2f%% filled", filled);
        } else if (type.startsWith("auction-only")){
            return "Waiting for auction";
        }
        return "Unknown";
    }

    private String parseOpenOrderDescription(JSONObject order) throws JSONException {
        String tradingSymbol = order.getString("symbol").toUpperCase();
        String fromSymbol = tradingSymbol.substring(0, 3);
        String toSymbol = tradingSymbol.substring(3);

        String type = order.getString("type");
        if (type.equals("exchange limit") || type.equals("auction-only limit")) {
            JSONArray options = order.getJSONArray("options");
            String orderType = capitalize(options.length() == 0 ? "limit" : options.getString(0));
            String side = order.getString("side");
            String amount = order.getString("original_amount");
            String price = formatCurrencyAmount(toSymbol, order.getString("price"));
            return String.format("%s %s order for %s %s @ %s", orderType, side, amount, fromSymbol, price);
        } else if (type.startsWith("auction-only")) {
            type = capitalize(type);
            String totalSpend = formatCurrencyAmount(toSymbol, order.getString("total_spend"));
            return String.format("%s of %s for %s", type, fromSymbol, totalSpend);
        }
        return "Mysterious order";
    }

    private String parsePastOrderDescription(String tradingSymbol, JSONObject order) throws JSONException {
        tradingSymbol = tradingSymbol.toUpperCase();
        String fromSymbol = tradingSymbol.substring(0, 3);
        String toSymbol = tradingSymbol.substring(3);

        String type = capitalize(order.getString("type"));
        String amount = order.getString("amount");
        String price = formatCurrencyAmount(toSymbol, order.getString("price"));
        String fee = formatCurrencyAmount(order.getString("fee_currency"), order.getString("fee_amount"));
        return String.format("%s order filled for %s %s @ %s with fee %s", type, amount, fromSymbol, price, fee);
    }

    private static String capitalize(String word) {
        if (word.isEmpty()) return word;

        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    private static String formatCurrencyAmount(String currency, String number) {
        if (USD.equals(currency)) {
            return "$" + number;
        } else {
            return number + " " + currency;
        }
    }
}
