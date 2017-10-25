package com.illegalsimon.hodler;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.illegalsimon.hodler.data.Symbol;
import com.illegalsimon.hodler.utils.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TradingActivity extends AppCompatActivity implements TradingFragment.OnFragmentInteractionListener {

    public static final String IS_BUY_KEY = "com.illegalsimon.hodler.IS_BUY";
    public static final String FROM_SYMBOL_KEY = "com.illegalsimon.hodler.FROM_SYMBOL";
    public static final String TO_SYMBOL_KEY = "com.illegalsimon.hodler.TO_SYMBOL";

    private TabLayout mTabs;
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
        mViewPager = (ViewPager) findViewById(R.id.vp_trading_pairs);
        setupViewPager(intent.getBooleanExtra(IS_BUY_KEY, false), (Symbol) intent.getSerializableExtra(FROM_SYMBOL_KEY), (Symbol) intent.getSerializableExtra(TO_SYMBOL_KEY));

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

    private void setupViewPager(boolean isBuy, Symbol fromSymbol, Symbol toSymbol) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(TradingFragment.newInstance(true, fromSymbol, toSymbol), "Buy");
        adapter.addFragment(TradingFragment.newInstance(false, fromSymbol, toSymbol), "Sell");
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(isBuy ? 0 : 1);
    }

    @Override
    public void onPlaceOrder(String pairSymbol, double price, double amount, String orderType) {
    }

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
