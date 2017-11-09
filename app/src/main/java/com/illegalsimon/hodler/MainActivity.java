package com.illegalsimon.hodler;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.IdRes;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.illegalsimon.hodler.data.ChartDateFormatter;
import com.illegalsimon.hodler.data.TimePrice;
import com.illegalsimon.hodler.data.TimeRange;
import com.illegalsimon.hodler.utils.JsonParser;
import com.illegalsimon.hodler.data.PriceOverview;
import com.illegalsimon.hodler.data.Symbol;
import com.illegalsimon.hodler.utils.NetworkUtils;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LoaderCallbacks<PriceOverview> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int CC_PRICE_LOADER_ID = 0;
    private static final int DEFAULT_SYMBOL_BTN_ID = R.id.btn_eth;
    private static final int DEFAULT_TIME_RANGE_BTN_ID = R.id.btn_one_hour;
    private static final String FROM_SYMBOL_KEY = "fromSymbol";
    private static final String TO_SYMBOL_KEY = "toSymbol";
    private static final String TIME_RANGE_KEY = "timeRange";
    private static final Map<Integer, Symbol> BUTTON_SYMBOL_MAP = new HashMap<Integer, Symbol>() {{
        put(R.id.btn_btc, Symbol.BTC);
        put(R.id.btn_eth, Symbol.ETH);
    }};
    private static final Map<Integer, TimeRange> BUTTON_TIME_RANGE_MAP = new HashMap<Integer, TimeRange>() {{
        put(R.id.btn_one_hour, TimeRange.ONE_HOUR);
        put(R.id.btn_one_day, TimeRange.ONE_DAY);
        put(R.id.btn_one_week, TimeRange.ONE_WEEK);
        put(R.id.btn_one_month, TimeRange.ONE_MONTH);
        put(R.id.btn_three_months, TimeRange.THREE_MONTHS);
        put(R.id.btn_six_months, TimeRange.SIX_MONTHS);
        put(R.id.btn_one_year, TimeRange.ONE_YEAR);
    }};

    private RadioGroup mSymbolsGroup;
    private RadioGroup mTimeRangesGroup;

    private LinearLayout mPricesLayout;
    private TextView mCurrentPriceTextView;
    private TextView mPriceChangeTextView;

    private ProgressBar mLoadingIndicator;

    private LineChart mPriceChart;
    private long base;
    private float currentPrice;
    private float priceChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_overview));

        mSymbolsGroup = (RadioGroup) findViewById(R.id.rg_symbols);
        ((RadioButton) findViewById(DEFAULT_SYMBOL_BTN_ID)).setChecked(true);
        mSymbolsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                loadPriceData(BUTTON_SYMBOL_MAP.get(checkedId), Symbol.USD, BUTTON_TIME_RANGE_MAP.get(mTimeRangesGroup.getCheckedRadioButtonId()), true);
            }
        });

        mTimeRangesGroup = (RadioGroup) findViewById(R.id.rg_time_ranges);
        ((RadioButton) findViewById(DEFAULT_TIME_RANGE_BTN_ID)).setChecked(true);
        mTimeRangesGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                loadPriceData(BUTTON_SYMBOL_MAP.get(mSymbolsGroup.getCheckedRadioButtonId()), Symbol.USD, BUTTON_TIME_RANGE_MAP.get(checkedId), true);
            }
        });

        mPricesLayout = (LinearLayout) findViewById(R.id.ll_prices);
        mCurrentPriceTextView = (TextView) findViewById(R.id.tv_current_price);
        mPriceChangeTextView = (TextView) findViewById(R.id.tv_price_change);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        mPriceChart = (LineChart) findViewById(R.id.lc_price_chart);
        setUpPriceChart();

        loadPriceData(BUTTON_SYMBOL_MAP.get(DEFAULT_SYMBOL_BTN_ID), Symbol.USD, BUTTON_TIME_RANGE_MAP.get(DEFAULT_TIME_RANGE_BTN_ID), false);
    }

    private void setUpPriceChart() {
        int colorWhite = ContextCompat.getColor(this, R.color.colorWhite);
        XAxis xAxis = mPriceChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(colorWhite);
        xAxis.setAxisLineColor(colorWhite);
        xAxis.setAxisLineWidth(1.2f);
        YAxis axisLeft = mPriceChart.getAxisLeft();
        axisLeft.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        axisLeft.setTextColor(colorWhite);
        mPriceChart.getAxisRight().setEnabled(false);
        mPriceChart.getLegend().setEnabled(false);
        mPriceChart.setScaleEnabled(false);
        mPriceChart.getDescription().setEnabled(false);
        mPriceChart.setNoDataTextColor(colorWhite);
        mPriceChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mPriceChart.highlightValues(null);
                    mPriceChangeTextView.setVisibility(View.VISIBLE);
                    setPriceViewToCurrent();
                }
                return false;
            }
        });
        mPriceChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            SimpleDateFormat mDateFormat = new SimpleDateFormat("M/d/YYYY HH:mm", Locale.US);

            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String priceStr = String.format(Locale.US, "$%.2f", e.getY());
                String dateStr = mDateFormat.format(new Date(((long) e.getX() + base) * 1000));
                String completeStr = priceStr + " " + dateStr;
                SpannableString text = new SpannableString(completeStr);
                text.setSpan(new RelativeSizeSpan(0.6f), priceStr.length(), completeStr.length(), 0);
                mCurrentPriceTextView.setText(text);
                mPriceChangeTextView.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected() {}
        });
    }

    private void loadPriceData(Symbol fromSymbol, Symbol toSymbol, TimeRange timeRange, boolean isReload) {
        Bundle args = new Bundle();
        args.putSerializable(FROM_SYMBOL_KEY, fromSymbol);
        args.putSerializable(TO_SYMBOL_KEY, toSymbol);
        args.putSerializable(TIME_RANGE_KEY, timeRange);
        if (isReload) {
            getSupportLoaderManager().restartLoader(CC_PRICE_LOADER_ID, args, this);
        } else {
            getSupportLoaderManager().initLoader(CC_PRICE_LOADER_ID, args, this);
        }
    }

    @Override
    public Loader<PriceOverview> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<PriceOverview>(this) {

            @Override
            protected void onStartLoading() {
                mPricesLayout.setVisibility(View.INVISIBLE);
                mPriceChart.setVisibility(View.INVISIBLE);
                mLoadingIndicator.setVisibility(View.VISIBLE);
                forceLoad();
            }

            @Override
            public PriceOverview loadInBackground() {
                Symbol fromSymbol = (Symbol) args.get(FROM_SYMBOL_KEY);
                Symbol toSymbol = (Symbol) args.get(TO_SYMBOL_KEY);
                TimeRange timeRange = (TimeRange) args.get(TIME_RANGE_KEY);
                URL priceHistoryRequestUrl = NetworkUtils.buildPriceHistoryQueryUrl(fromSymbol, toSymbol, timeRange);
                URL currentPriceRequestUrl = NetworkUtils.buildCurrentPriceQueryUrl(fromSymbol, toSymbol);

                try {
                    String priceHistoryResponse = NetworkUtils.getResponseFromUrl(priceHistoryRequestUrl);
                    String currentPriceResponse = NetworkUtils.getResponseFromUrl(currentPriceRequestUrl);

                    return new PriceOverview(JsonParser.parseCurrentPriceResponse(toSymbol, currentPriceResponse),
                            timeRange, JsonParser.parsePriceHistoryResponse(priceHistoryResponse));
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<PriceOverview> loader, PriceOverview data) {
        mLoadingIndicator.setVisibility(View.GONE);

        if (data == null || data.getPriceHistory() == null || data.getPriceHistory().length == 0 || data.getCurrentPrice() == -1f || data.getOpenPrice() == -1f) {
            // TODO: Show Error message
            return;
        }

        mPricesLayout.setVisibility(View.VISIBLE);
        mPriceChart.setVisibility(View.VISIBLE);

        currentPrice = data.getCurrentPrice();
        priceChange = ((currentPrice - data.getOpenPrice()) / data.getOpenPrice()) * 100;
        setPriceViewToCurrent();

        List<Entry> dataEntries = new ArrayList<>();
        base = data.getPriceHistory()[0].getTimestamp(); // To avoid precision loss when converted to float
        for (TimePrice timePrice : data.getPriceHistory()) {
            dataEntries.add(new Entry(timePrice.getTimestamp() - base, timePrice.getPrice()));
        }

        int colorTopaz = ContextCompat.getColor(this, R.color.colorTopaz);
        LineDataSet dataSet = new LineDataSet(dataEntries, "");
        dataSet.setCircleRadius(1f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setColor(colorTopaz);
        dataSet.setCircleColor(colorTopaz);
        dataSet.setFillColor(colorTopaz);
        dataSet.setDrawFilled(true);
        mPriceChart.setData(new LineData(dataSet));
        mPriceChart.getXAxis().setValueFormatter(new ChartDateFormatter(data.getTimeRange(), base));
        mPriceChart.invalidate();
    }

    @Override
    public void onLoaderReset(Loader<PriceOverview> loader) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectedId = item.getItemId();
        if (selectedId == R.id.menu_open_website) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    NetworkUtils.buildCryptoCompareWebsiteUri(BUTTON_SYMBOL_MAP.get(mSymbolsGroup.getCheckedRadioButtonId()), Symbol.USD));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } else if (selectedId == R.id.menu_dashboard) {
            startActivity(new Intent(this, DashboardActivity.class));
        }
        return true;
    }

    public void onClickBtnTrade(View view) {
        Intent intentTrade = new Intent(this, TradingActivity.class);
        intentTrade.putExtra(TradingActivity.FROM_SYMBOL_KEY, BUTTON_SYMBOL_MAP.get(mSymbolsGroup.getCheckedRadioButtonId()));
        intentTrade.putExtra(TradingActivity.TO_SYMBOL_KEY, Symbol.USD);

        int btnId = view.getId();
        if (btnId == R.id.btn_buy) {
            intentTrade.putExtra(TradingActivity.IS_BUY_KEY, true);
        } else if (btnId == R.id.btn_sell) {
            intentTrade.putExtra(TradingActivity.IS_BUY_KEY, false);
        }
        startActivity(intentTrade);
    }

    private void setPriceViewToCurrent() {
        mCurrentPriceTextView.setText(String.format(Locale.US, "$%.2f", currentPrice));
        int textColor = 0;
        String sign;
        if (priceChange > 0) {
            sign = "<font color='#00ff00'>\u25B2</font>";
            textColor = Color.GREEN;
        } else if (priceChange < 0) {
            sign = "<font color='#ff0000'>\u25BC</font>";
            textColor = Color.RED;
        } else {
            sign = "";
            textColor = Color.GRAY;
        }
        mPriceChangeTextView.setText(Html.fromHtml(sign + String.format(Locale.US, " %.2f%%", priceChange < 0 ? -priceChange : priceChange)));
    }
}
