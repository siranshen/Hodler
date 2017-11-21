package com.illegalsimon.hodler;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.illegalsimon.hodler.data.Symbol;
import com.illegalsimon.hodler.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import static com.illegalsimon.hodler.DashboardActivity.formatCurrencyAmount;

public class TradingFragment extends Fragment implements TextWatcher, AdapterView.OnItemSelectedListener, LoaderManager.LoaderCallbacks<String> {
    private static final String TAG = TradingFragment.class.getSimpleName();

    private static final String IS_BUY_KEY = "IS_BUY"; // Trade type of the fragment
    private static final String FROM_SYMBOL_KEY = "FROM_SYMBOL";
    private static final String TO_SYMBOL_KEY = "TO_SYMBOL";
    private static final String URL_KEY = "URL";
    private static final String JSON_KEY = "JSON";

    private static final double FEE_RATE = 0.0025;
    private static final int CRYPTO_DECIMAL_PLACES = 6;
    private static final String LEFT_ARROW = "<-";
    private static final String RIGHT_ARROW = "->";
    public static final String[][] BASE_TRADING_PAIRS = new String[][] {
            { Symbol.BTC.getName(), Symbol.USD.getName() },
            { Symbol.ETH.getName(), Symbol.USD.getName() },
            { Symbol.ETH.getName(), Symbol.BTC.getName() }
    };

    public static final String[] ORDER_TYPES = new String[] { "Limit", "Maker-or-Cancel", "Immediate-or-Cancel", "Auction-Only" };

    private boolean isBuy;
    private Symbol toSymbol;
    private String[] tradingPairTitles;

    private TextView mLastPriceText;
    private Spinner mTradingPairSpinner;
    private Spinner mOrderTypeSpinner;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mTotalEditText;
    private Button mPlaceOrderBtn;
    private OnTradingFragmentInteractionListener mListener;

    public TradingFragment() {
        // Required empty public constructor
    }

    public static TradingFragment newInstance(boolean isBuy, Symbol fromSymbol, Symbol toSymbol) {
        TradingFragment fragment = new TradingFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_BUY_KEY, isBuy);
        args.putSerializable(FROM_SYMBOL_KEY, fromSymbol);
        args.putSerializable(TO_SYMBOL_KEY, toSymbol);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null || !getArguments().containsKey(IS_BUY_KEY)) {
            throw new RuntimeException("TradingFragment must have a trade type");
        }
        isBuy = getArguments().getBoolean(IS_BUY_KEY);
        tradingPairTitles = new String[BASE_TRADING_PAIRS.length];
        String arrow = isBuy ? LEFT_ARROW : RIGHT_ARROW;
        for (int i = 0; i < BASE_TRADING_PAIRS.length; i++) {
            tradingPairTitles[i] = String.format("%s %s %s", BASE_TRADING_PAIRS[i][0], arrow, BASE_TRADING_PAIRS[i][1]);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trading, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Symbol fromSymbol = (Symbol) getArguments().get(FROM_SYMBOL_KEY);
        if (fromSymbol == null) fromSymbol = Symbol.BTC;
        toSymbol = (Symbol) getArguments().get(TO_SYMBOL_KEY);
        if (toSymbol == null) toSymbol = Symbol.USD;

        mLastPriceText = view.findViewById(R.id.tv_last_price);
        mTradingPairSpinner = view.findViewById(R.id.spinner_trade_pair);
        mOrderTypeSpinner = view.findViewById(R.id.spinner_order_type);
        mPriceEditText = view.findViewById(R.id.et_price);
        mPriceEditText.addTextChangedListener(this);
        mQuantityEditText = view.findViewById(R.id.et_quantity);
        mQuantityEditText.setText("0");
        mQuantityEditText.addTextChangedListener(this);
        mTotalEditText = view.findViewById(R.id.et_total);
        mTotalEditText.addTextChangedListener(this);

        ArrayAdapter<String> tradingPairAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner, tradingPairTitles);
        mTradingPairSpinner.setAdapter(tradingPairAdapter);
        for (int i = 0; i < tradingPairTitles.length; i++) {
            if (tradingPairTitles[i].equals(String.format("%s %s %s", fromSymbol.getName(), isBuy ? LEFT_ARROW : RIGHT_ARROW, toSymbol.getName()))) {
                mTradingPairSpinner.setSelection(i);
            }
        }
        isProgrammatic = true;
        mTradingPairSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<String> orderTypesAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner, ORDER_TYPES);
        mOrderTypeSpinner.setAdapter(orderTypesAdapter);

        mPlaceOrderBtn = view.findViewById(R.id.btn_place_order);
        mPlaceOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickPlaceOrderBtn();
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTradingFragmentInteractionListener) {
            mListener = (OnTradingFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnTradingFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void onClickPlaceOrderBtn() {
        String price = mPriceEditText.getText().toString();
        String quantity = mQuantityEditText.getText().toString();
        if (price.isEmpty() || quantity.isEmpty()) {
            Toast.makeText(getContext().getApplicationContext(), "Input cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        double priceDouble = Double.valueOf(price);
        double quantityDouble = round(Double.valueOf(quantity), CRYPTO_DECIMAL_PLACES);
        if (priceDouble <= 0 || quantityDouble <= 0) {
            Toast.makeText(getContext().getApplicationContext(), "Improper order", Toast.LENGTH_SHORT).show();
            return;
        }
        mQuantityEditText.setText(String.valueOf(quantityDouble)); // This will trigger TextWatcher

        price = mPriceEditText.getText().toString();
        String[] tradingPair = BASE_TRADING_PAIRS[mTradingPairSpinner.getSelectedItemPosition()];
        String orderType = mOrderTypeSpinner.getSelectedItem().toString();
        String fee = formatCurrencyAmount(tradingPair[1], String.valueOf(round(Double.valueOf(price) * quantityDouble * FEE_RATE, getDecimalPlaces())));

        String confirmationMessage = String.format(Locale.US, "You are about to place %s %s %s order for %s %s at a price of %s per %s with fee %s.",
                getArticle(orderType), orderType, isBuy ? "buy" : "sell", mQuantityEditText.getText().toString(), tradingPair[0],
                formatCurrencyAmount(tradingPair[1], price), tradingPair[0], fee);

        new AlertDialog.Builder(getContext(), R.style.CustomAlertDialogStyle).setTitle("Placing order").setMessage(confirmationMessage)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onPlaceOrder();
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    // A very crude article decider
    private String getArticle(String str) {
        char firstChar = str.toLowerCase().charAt(0);
        if (firstChar == 'a' || firstChar == 'e' || firstChar == 'i' || firstChar == 'o' || firstChar == 'u') return "an";
        return "a";
    }

    private void onPlaceOrder() {
        String[] tradingPair = BASE_TRADING_PAIRS[mTradingPairSpinner.getSelectedItemPosition()];
        String orderType = mOrderTypeSpinner.getSelectedItem().toString();

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("symbol", tradingPair[0].toLowerCase() + tradingPair[1].toLowerCase());
            jsonRequest.put("amount", mQuantityEditText.getText().toString());
            jsonRequest.put("price", mPriceEditText.getText().toString());
            jsonRequest.put("side", isBuy ? "buy" : "sell");
            jsonRequest.put("type", "exchange limit");
            if (!orderType.equals(TradingFragment.ORDER_TYPES[0])) {
                // https://docs.gemini.com/rest-api/#new-order
                jsonRequest.put("options", new JSONArray(Collections.singletonList(orderType.toLowerCase())));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Bundle args = new Bundle();
        args.putString(URL_KEY, "/v1/order/new");
        args.putString(JSON_KEY, jsonRequest.toString());

        int loaderId = TradingActivity.GEMINI_PRIVATE_LOADER_ID;
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();

        if (loaderManager.getLoader(loaderId) == null) {
            loaderManager.initLoader(loaderId, args, this);
        } else {
            loaderManager.restartLoader(loaderId, args, this);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable editable) {
        String price = mPriceEditText.getText().toString();
        String quantity = mQuantityEditText.getText().toString();
        String total = mTotalEditText.getText().toString();

        if (price.isEmpty() || quantity.isEmpty())
            return;

        int decimalPlaces = getDecimalPlaces();

        if (editable == mPriceEditText.getText()) {
            Double totalDouble = Double.valueOf(price) * Double.valueOf(quantity);
            setEditTextViewText(mTotalEditText, String.valueOf(round(totalDouble, decimalPlaces)));
        } else if (editable == mQuantityEditText.getText()) {
            Double priceDouble = round(Double.valueOf(price), decimalPlaces);
            setEditTextViewText(mPriceEditText, String.valueOf(priceDouble));
            setEditTextViewText(mTotalEditText, String.valueOf(round(priceDouble * Double.valueOf(quantity), decimalPlaces)));
        } else if (!total.isEmpty() && editable == mTotalEditText.getText()) {
            Double priceDouble = round(Double.valueOf(price), decimalPlaces);
            setEditTextViewText(mPriceEditText, String.valueOf(priceDouble));
            setEditTextViewText(mQuantityEditText, String.valueOf(round(Double.valueOf(total) / priceDouble, CRYPTO_DECIMAL_PLACES)));
        }
    }

    private int getDecimalPlaces() {
        return mTradingPairSpinner.getSelectedItemPosition() == 2 ? CRYPTO_DECIMAL_PLACES : 2;
    }

    private void setEditTextViewText(EditText editText, String text) {
        editText.removeTextChangedListener(this);
        editText.setText(text);
        editText.addTextChangedListener(this);
    }

    private double round(double number, int decimalPlaces) {
        double helperDouble = Math.pow(10.0, decimalPlaces);
        return Math.round(number * helperDouble) / helperDouble;
    }

    // onItemSelected fires off when setOnItemSelectedListener and setSelection
    private boolean isProgrammatic = true;

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (isProgrammatic) {
            isProgrammatic = false;
            return;
        }
        mLastPriceText.setText(getString(R.string.loading_text));
        String[] tradingPair = BASE_TRADING_PAIRS[i];
        if (mListener != null) mListener.onTradingPairChanged(isBuy, i, tradingPair[0].toLowerCase() + tradingPair[1].toLowerCase());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(getContext()) {

            @Override
            protected void onStartLoading() {
                mPlaceOrderBtn.setEnabled(false);
                forceLoad();
            }

            @Override
            public String loadInBackground() {
                String url = args.getString(URL_KEY);
                String jsonRequest = args.getString(JSON_KEY);
                try {
                    return NetworkUtils.getGeminiPrivateResponseFromUrl(url, jsonRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        Log.d(TAG, data);
        int loaderId = TradingActivity.GEMINI_PRIVATE_LOADER_ID;
        // Need to destroy the loader as Android calls it when reopening app
        getActivity().getSupportLoaderManager().destroyLoader(loaderId);

        Toast errorMessage = Toast.makeText(getContext().getApplicationContext(), "Sorry, something went wrong...", Toast.LENGTH_LONG);
        if (data == null) {
            errorMessage.show();
        } else {
            try {
                JSONObject jsonResponse = new JSONObject(data);
                if (jsonResponse.has("order_id")) {
                    setEditTextViewText(mPriceEditText, "");
                    setEditTextViewText(mQuantityEditText, "0");
                    setEditTextViewText(mTotalEditText, "");
                    Toast.makeText(getContext().getApplicationContext(), "Your order has been successfully placed!", Toast.LENGTH_LONG).show();
                } else if (jsonResponse.has("result") && jsonResponse.getString("result").equals("error")) {
                    if (jsonResponse.has("message")) {
                        Toast.makeText(getContext().getApplicationContext(), jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                    } else {
                        errorMessage.show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.show();
            }
        }
        mPlaceOrderBtn.setEnabled(true);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {}

    public void updateTradingPair(int position) {
        isProgrammatic = true;
        mTradingPairSpinner.setSelection(position, false);
    }

    public void updateLastPrice(String price) {
        String completeStr = price + " " + new SimpleDateFormat("M/d/YYYY HH:mm:ss", Locale.US).format(new Date());
        SpannableString text = new SpannableString(completeStr);
        text.setSpan(new RelativeSizeSpan(0.8f), price.length(), completeStr.length(), 0);
        mLastPriceText.setText(text);
    }

    interface OnTradingFragmentInteractionListener {
        void onTradingPairChanged(boolean isBuy, int position, String tradingPair);
    }
}
