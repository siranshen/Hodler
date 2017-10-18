package com.illegalsimon.hodler.utils;

import android.net.Uri;
import android.util.Log;

import com.illegalsimon.hodler.data.Symbol;
import com.illegalsimon.hodler.data.TimeRange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Scanner;

/**
 * Created by shens on 10/14/2017.
 */

public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    public enum TimePrecision {
        MINUTE("histominute"),
        HOUR("histohour"),
        DAY("histoday");

        private final String path;

        TimePrecision(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    private static final String CRYTOCOMPARE_BASE_URL = "https://min-api.cryptocompare.com/data/";

    private static final String PRICE_URL = "price";
    private static final String HISTORICAL_PRICE_URL = "pricehistorical";

    private static final String FROM_SYMBOL_PARAM = "fsym";
    private static final String TO_SYMBOL_PARAM = "tsym";
    private static final String TO_SYMBOLS_PARAM = "tsyms";

    public static URL buildPriceHistoryQueryUrl(Symbol fromSymbol, Symbol toSymbol, TimeRange timeRange) {
        Uri uri = Uri.parse(CRYTOCOMPARE_BASE_URL + chooseTimePrecision(timeRange).getPath()).buildUpon()
                .appendQueryParameter(FROM_SYMBOL_PARAM, fromSymbol.getName())
                .appendQueryParameter(TO_SYMBOL_PARAM, toSymbol.getName())
                .appendQueryParameter("aggregate", chooseAggregateLevel(timeRange))
                .appendQueryParameter("limit", chooseLimit(timeRange))
                .build();

        return toUrl(uri);
    }

    public static URL buildCurrentPriceQueryUrl(Symbol fromSymbol, Symbol toSymbol) {
        Uri uri = Uri.parse(CRYTOCOMPARE_BASE_URL + PRICE_URL).buildUpon()
                .appendQueryParameter(FROM_SYMBOL_PARAM, fromSymbol.getName())
                .appendQueryParameter(TO_SYMBOLS_PARAM, toSymbol.getName())
                .build();

        return toUrl(uri);
    }

    // This API is only based on daily info: https://www.cryptocompare.com/api/#-api-data-pricehistorical-
    public static URL buildHistoricalPriceQueryUrl(Symbol fromSymbol, Symbol toSymbol, TimeRange timeRange) {
        Uri uri = Uri.parse(CRYTOCOMPARE_BASE_URL + HISTORICAL_PRICE_URL).buildUpon()
                .appendQueryParameter(FROM_SYMBOL_PARAM, fromSymbol.getName())
                .appendQueryParameter(TO_SYMBOLS_PARAM, toSymbol.getName())
                .appendQueryParameter("ts", chooseTimestamp(timeRange))
                .build();

        return toUrl(uri);
    }

    public static String getResponseFromUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            Scanner scanner = new Scanner(urlConnection.getInputStream());
            scanner.useDelimiter("\\A");

            return scanner.hasNext() ? scanner.next() : null;
        } finally {
            urlConnection.disconnect();
        }
    }

    private static URL toUrl(Uri uri) {
        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            Log.e(TAG, "Malformed URL: " + uri.toString());
            return null;
        }
    }

    private static TimePrecision chooseTimePrecision(TimeRange timeRange) {
        switch (timeRange) {
            case ONE_HOUR:
            case ONE_DAY:
                return TimePrecision.MINUTE;
            case ONE_WEEK:
            case ONE_MONTH:
                return TimePrecision.HOUR;
            default:
                return TimePrecision.DAY;
        }
    }

    private static String chooseAggregateLevel(TimeRange timeRange) {
        switch (timeRange) {
            case ONE_DAY:
                return "10"; // 10 minutes
            case ONE_MONTH:
                return "6"; // 6 hours
            default:
                return "1";
        }
    }

    private static String chooseLimit(TimeRange timeRange) {
        switch (timeRange) {
            case ONE_HOUR:
                return "60"; // 60 minutes
            case ONE_DAY:
                return "144"; // 144 * 10 minutes = 24 hours
            case ONE_WEEK:
                return "168"; // 168 hours = 7 days
            case ONE_MONTH:
                return "120"; // 120 * 6 hours = 30 days
            case THREE_MONTHS:
                return "90"; // 90 days
            case SIX_MONTHS:
                return "180"; // 180 days
            case ONE_YEAR:
                return "365"; // 365 days
            default:
                return "2000"; // Upper limit of the API
        }
    }

    private static String chooseTimestamp(TimeRange timeRange) {
        Calendar calendar = Calendar.getInstance();

        switch (timeRange) {
            case ONE_HOUR:
                calendar.add(Calendar.HOUR_OF_DAY, -1);
                break;
            case ONE_DAY:
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                break;
            case ONE_WEEK:
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                break;
            case ONE_MONTH:
                calendar.add(Calendar.DAY_OF_MONTH, -30);
                break;
            case THREE_MONTHS:
                calendar.add(Calendar.DAY_OF_MONTH, -90);
                break;
            case SIX_MONTHS:
                calendar.add(Calendar.DAY_OF_MONTH, -180);
                break;
            case ONE_YEAR:
                calendar.add(Calendar.YEAR, -1);
                break;
            default:
                calendar.add(Calendar.DAY_OF_MONTH, -2000);
        }

        return String.valueOf(calendar.getTimeInMillis() / 1000);
    }
}
