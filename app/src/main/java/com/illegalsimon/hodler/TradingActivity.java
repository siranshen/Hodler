package com.illegalsimon.hodler;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;

import com.illegalsimon.hodler.data.Symbol;
import com.illegalsimon.hodler.utils.NetworkUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TradingActivity extends AppCompatActivity implements TradingFragment.OnTradingFragmentInteractionListener, LoaderManager.LoaderCallbacks<String> {
    private static final String TAG = TradingActivity.class.getSimpleName();

    public static final String IS_BUY_KEY = "com.illegalsimon.hodler.IS_BUY";
    public static final String FROM_SYMBOL_KEY = "com.illegalsimon.hodler.FROM_SYMBOL";
    public static final String TO_SYMBOL_KEY = "com.illegalsimon.hodler.TO_SYMBOL";

    public static final String TRADING_PAIR_KEY = "TRADING_PAIR";

    private static long LOADING_INTERVAL = 5; // 5 seconds
    public static final int GEMINI_PUBLIC_LOADER_ID = 1;
    public static final int GEMINI_PRIVATE_LOADER_ID = 2;

    private ScheduledExecutorService mScheduler;
    private ScheduledFuture lastSchedule;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trading);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_trading));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Trading");
        }

        Intent intent = getIntent();
        Symbol fromSymbol = (Symbol) intent.getSerializableExtra(FROM_SYMBOL_KEY);
        Symbol toSymbol = (Symbol) intent.getSerializableExtra(TO_SYMBOL_KEY);
        mViewPager = (ViewPager) findViewById(R.id.vp_trading_pairs);
        setupViewPager(intent.getBooleanExtra(IS_BUY_KEY, false), fromSymbol, toSymbol);

        TabLayout mTabs = (TabLayout) findViewById(R.id.tabs_trading);
        mTabs.setupWithViewPager(mViewPager);
        mTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        if (mScheduler == null) mScheduler = Executors.newSingleThreadScheduledExecutor();
    }

    private void setupViewPager(boolean isBuy, Symbol fromSymbol, Symbol toSymbol) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), fromSymbol.getName().toLowerCase() + toSymbol.getName().toLowerCase());
        adapter.addFragment(TradingFragment.newInstance(true, fromSymbol, toSymbol), "Buy");
        adapter.addFragment(TradingFragment.newInstance(false, fromSymbol, toSymbol), "Sell");
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(getFragmentPosition(isBuy));
    }

    @Override
    protected void onResume() {
        super.onResume();
        scheduleLoader(((ViewPagerAdapter) mViewPager.getAdapter()).mTradingPair);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (lastSchedule != null) lastSchedule.cancel(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * LoaderCallbacks implementation
     */

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {
            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            public String loadInBackground() {
                if (args == null) return null;

                String pair = args.getString(TRADING_PAIR_KEY);
                try {
                    return NetworkUtils.getResponseFromUrl(NetworkUtils.buildGeminiPublicRequestUrl("/v1/pubticker/" + pair));
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if (data == null) return;

        try {
            String lastPrice = new JSONObject(data).getString("last");

            List<Fragment> fragments = ((ViewPagerAdapter) mViewPager.getAdapter()).mFragments;
            for (Fragment fragment : fragments) {
                if (fragment instanceof TradingFragment) ((TradingFragment) fragment).updateLastPrice(lastPrice);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {}

    /**
     * OnTradingFragmentInteractionListener implementation
     */

    @Override
    public void onTradingPairChanged(boolean isBuy, int position, String tradingPair) {
        ViewPagerAdapter adapter = (ViewPagerAdapter) mViewPager.getAdapter();
        Fragment otherFragment = adapter.mFragments.get(getFragmentPosition(!isBuy));
        if (otherFragment instanceof TradingFragment) ((TradingFragment) otherFragment).updateTradingPair(position);
        adapter.mTradingPair = tradingPair;

        scheduleLoader(tradingPair);
    }

    private void scheduleLoader(final String tradingPair) {
        if (lastSchedule != null) lastSchedule.cancel(true);

        lastSchedule = mScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Bundle args = new Bundle();
                args.putString(TRADING_PAIR_KEY, tradingPair);
                if (getSupportLoaderManager().getLoader(GEMINI_PUBLIC_LOADER_ID) == null) {
                    getSupportLoaderManager().initLoader(GEMINI_PUBLIC_LOADER_ID, args, TradingActivity.this);
                } else {
                    getSupportLoaderManager().restartLoader(GEMINI_PUBLIC_LOADER_ID, args, TradingActivity.this);
                }
            }
        }, 0, LOADING_INTERVAL, TimeUnit.SECONDS);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();
        private String mTradingPair;

        ViewPagerAdapter(FragmentManager manager, String tradingPair) {
            super(manager);
            mTradingPair = tradingPair;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }

        void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }
    }

    private static int getFragmentPosition(boolean isBuy) {
        return isBuy ? 0 : 1;
    }
}
