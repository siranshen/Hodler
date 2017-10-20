package com.illegalsimon.hodler;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.illegalsimon.hodler.data.Symbol;

import java.util.ArrayList;
import java.util.List;

public class TradingActivity extends AppCompatActivity implements TradingFragment.OnFragmentInteractionListener {

    public static final String IS_BUY_KEY = "com.illegalsimon.hodler.IS_BUY";
    public static final String FROM_SYMBOL_KEY = "com.illegalsimon.hodler.FROM_SYMBOL";
    public static final String TO_SYMBOL_KEY = "com.illegalsimon.hodler.TO_SYMBOL";

    private TabLayout mTabs;
    private ViewPager mViewPager;
    private boolean isBuy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trading);

        Intent intent = getIntent();
        if (intent.hasExtra(IS_BUY_KEY)) {
            isBuy = intent.getBooleanExtra(IS_BUY_KEY, false);
        } else {
            throw new RuntimeException("Trade type not specified");
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_trading));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isBuy ? "Buy" : "Sell");
        }

        mViewPager = (ViewPager) findViewById(R.id.vp_trading_pairs);
        setupViewPager((Symbol) intent.getSerializableExtra(FROM_SYMBOL_KEY), (Symbol) intent.getSerializableExtra(TO_SYMBOL_KEY));

        mTabs = (TabLayout) findViewById(R.id.tabs_trading);
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
    }

    private void setupViewPager(Symbol fromSymbol, Symbol toSymbol) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        String targetPair = String.format("%s%s%s", fromSymbol.getName(), isBuy ? "<-" : "->", toSymbol.getName());
        String[] tradePairs = getTradePairs();
        int targetIndex = 0;
        for (int i = 0; i < tradePairs.length; i++) {
            String pair = tradePairs[i];
            adapter.addFragment(new TradingFragment(), pair);
            if (pair.equals(targetPair)) {
                targetIndex = i;
            }
        }
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(targetIndex);
    }

    private String[] getTradePairs() {
        return isBuy ? new String[] { "BTC<-USD", "ETH<-USD", "ETH<-BTC" } : new String[] { "BTC->USD", "ETH->USD", "ETH->BTC" };
    }

    @Override
    public void onFragmentInteraction(Uri uri) {}

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
    }
}
