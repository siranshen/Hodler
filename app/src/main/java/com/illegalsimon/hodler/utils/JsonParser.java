package com.illegalsimon.hodler.utils;

import android.util.Log;

import com.illegalsimon.hodler.data.Symbol;
import com.illegalsimon.hodler.data.TimePrice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Sample url: https://min-api.cryptocompare.com/data/histohour?fsym=BTC&tsym=USD&aggregate=3
 * Created by shens on 10/14/2017.
 */

public final class JsonParser {

    private static final String TAG = JsonParser.class.getSimpleName();

    private static final String CC_RESPONSE_CODE_KEY = "Response";
    private static final String CC_RESPONSE_ERROR = "Error";
    private static final String CC_RESPONSE_DATA_KEY = "Data";
    private static final String CC_RESPONSE_OPEN_KEY = "open";
    private static final String CC_RESPONSE_CLOSE_KEY = "close";
    private static final String CC_RESPONSE_TIME_KEY = "time";
    private static final String CC_RESPONSE_TIMETO_KEY = "TimeTo";

    public static TimePrice[] parsePriceHistoryResponse(String response) throws JSONException {
        JSONObject responseJson = new JSONObject(response);

        if (isResponseError(responseJson) || !responseJson.has(CC_RESPONSE_DATA_KEY)) {
            return null;
        }

        JSONArray timePriceArray = responseJson.getJSONArray(CC_RESPONSE_DATA_KEY);
        int arrayLen = timePriceArray.length();

        TimePrice[] parsedData = new TimePrice[arrayLen + 1];

        JSONObject timePrice;
        for (int i = 0; i < arrayLen; i++) {
            timePrice = timePriceArray.getJSONObject(i);

            parsedData[i] = new TimePrice(timePrice.getLong(CC_RESPONSE_TIME_KEY), (float) timePrice.getDouble(CC_RESPONSE_OPEN_KEY));
        }
        timePrice = timePriceArray.getJSONObject(arrayLen - 1);
        parsedData[arrayLen] = new TimePrice(responseJson.getLong(CC_RESPONSE_TIMETO_KEY), (float) timePrice.getDouble(CC_RESPONSE_CLOSE_KEY));

        return parsedData;
    }

    public static float parseCurrentPriceResponse(Symbol toSymbol, String response) throws JSONException {
        JSONObject responseJson = new JSONObject(response);

        if (isResponseError(responseJson) || !responseJson.has(toSymbol.getName())) {
            return -1f;
        }

        return (float) responseJson.getDouble(toSymbol.getName());
    }

    public static float parseHistoricalPriceResponse(Symbol fromSymbol, Symbol toSymbol, String response) throws JSONException {
        JSONObject responseJson = new JSONObject(response);

        if (isResponseError(responseJson) || !responseJson.has(fromSymbol.getName())) {
            return -1f;
        }

        JSONObject priceObject = responseJson.getJSONObject(fromSymbol.getName());

        if (!priceObject.has(toSymbol.getName())) {
            return -1f;
        }

        return (float) priceObject.getDouble(toSymbol.getName());
    }

    public static boolean isOrderCancelled(String response) throws JSONException {
        JSONObject responseJson = new JSONObject(response);

        return responseJson.getBoolean("is_cancelled");
    }

    private static boolean isResponseError(JSONObject response) throws JSONException {
        if (response.has(CC_RESPONSE_CODE_KEY)) {
            if (response.getString(CC_RESPONSE_CODE_KEY).equals(CC_RESPONSE_ERROR)) {
                Log.e(TAG, "CC response is \"Error\"");
                return true;
            }
        }
        return false;
    }
}
