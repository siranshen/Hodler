package com.illegalsimon.hodler.utils;

import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.illegalsimon.hodler.data.Symbol;
import com.illegalsimon.hodler.data.TimeRange;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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

    private static final int TIMEOUT_VALUE = 6000; // 6 seconds

    private static final String CRYTOCOMPARE_BASE_URL = "https://www.cryptocompare.com/coins/";
    private static final String CRYTOCOMPARE_API_BASE_URL = "https://min-api.cryptocompare.com/data/";

    private static final String PRICE_URL = "price";
    private static final String HISTORICAL_PRICE_URL = "pricehistorical";

    private static final String FROM_SYMBOL_PARAM = "fsym";
    private static final String TO_SYMBOL_PARAM = "tsym";
    private static final String TO_SYMBOLS_PARAM = "tsyms";

    private static final String GEMINI_API_BASE_URL = "https://api.sandbox.gemini.com";

    // TODO: Customize API Key
    private static final String GEMINI_API_KEY = "C9Mj2u7NdEAFCKbwBrOh";
    private static final String GEMINI_API_SECRET = "NXCceQpZCVXBBKH9ZzZKR2CWBJn";

    public static Uri buildCryptoCompareWebsiteUri(Symbol fromSymbol, Symbol toSymbol) {
        return Uri.parse(CRYTOCOMPARE_BASE_URL + fromSymbol.getName().toLowerCase() + "/overview/" + toSymbol.getName().toLowerCase());
    }

    public static URL buildPriceHistoryQueryUrl(Symbol fromSymbol, Symbol toSymbol, TimeRange timeRange) {
        Uri uri = Uri.parse(CRYTOCOMPARE_API_BASE_URL + chooseTimePrecision(timeRange).getPath()).buildUpon()
                .appendQueryParameter(FROM_SYMBOL_PARAM, fromSymbol.getName())
                .appendQueryParameter(TO_SYMBOL_PARAM, toSymbol.getName())
                .appendQueryParameter("aggregate", chooseAggregateLevel(timeRange))
                .appendQueryParameter("limit", chooseLimit(timeRange))
                .build();

        return toUrl(uri);
    }

    public static URL buildCurrentPriceQueryUrl(Symbol fromSymbol, Symbol toSymbol) {
        Uri uri = Uri.parse(CRYTOCOMPARE_API_BASE_URL + PRICE_URL).buildUpon()
                .appendQueryParameter(FROM_SYMBOL_PARAM, fromSymbol.getName())
                .appendQueryParameter(TO_SYMBOLS_PARAM, toSymbol.getName())
                .build();

        return toUrl(uri);
    }

    // This API is only based on daily info: https://www.cryptocompare.com/api/#-api-data-pricehistorical-
    public static URL buildHistoricalPriceQueryUrl(Symbol fromSymbol, Symbol toSymbol, TimeRange timeRange) {
        Uri uri = Uri.parse(CRYTOCOMPARE_API_BASE_URL + HISTORICAL_PRICE_URL).buildUpon()
                .appendQueryParameter(FROM_SYMBOL_PARAM, fromSymbol.getName())
                .appendQueryParameter(TO_SYMBOLS_PARAM, toSymbol.getName())
                .appendQueryParameter("ts", chooseTimestamp(timeRange))
                .build();

        return toUrl(uri);
    }

    public static String getResponseFromUrl(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(TIMEOUT_VALUE);
        connection.setReadTimeout(TIMEOUT_VALUE);
        try {
            InputStream in = new BufferedInputStream(connection.getInputStream());
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            return scanner.hasNext() ? scanner.next() : null;
        } finally {
            connection.disconnect();
        }
    }

    public static URL buildGeminiPublicRequestUrl(String urlPath) {
        try {
            return new URL(GEMINI_API_BASE_URL + urlPath);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Malformed URL: " + GEMINI_API_BASE_URL + urlPath);
            return null;
        }
    }

    private static final String HMAC_SHA384 = "HmacSHA384";

    public static String getGeminiPrivateResponseFromUrl(String urlPath, String jsonData) throws IOException, JSONException {
        JSONObject request = new JSONObject(jsonData);
        request.put("request", urlPath);
        request.put("nonce", getNonce());
        Log.d(TAG, request.toString());

        URL url = new URL(GEMINI_API_BASE_URL + urlPath);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setConnectTimeout(TIMEOUT_VALUE);
        connection.setReadTimeout(TIMEOUT_VALUE);

        try {
            final byte[] encodedJson = Base64.encode(request.toString().getBytes("US-ASCII"), Base64.NO_WRAP);
            final Mac sha_hmac = Mac.getInstance(HMAC_SHA384);
            sha_hmac.init(new SecretKeySpec(GEMINI_API_SECRET.getBytes("UTF-8"), HMAC_SHA384));
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Length", "0");
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setRequestProperty("X-GEMINI-APIKEY", GEMINI_API_KEY);
            connection.setRequestProperty("X-GEMINI-PAYLOAD", new String(encodedJson));
            connection.setRequestProperty("X-GEMINI-SIGNATURE", bytesToHex(sha_hmac.doFinal(encodedJson)));

            InputStream in;
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                in = new BufferedInputStream(connection.getErrorStream());
            } else {
                in = new BufferedInputStream(connection.getInputStream());
            }
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            return scanner.hasNext() ? scanner.next() : null;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        } finally {
            connection.disconnect();
        }
    }

    private static String getNonce() {
        Random rand = new Random();
        return String.format(Locale.US, "%d%02d", System.currentTimeMillis(), rand.nextInt(100));
    }

    private final static char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
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
