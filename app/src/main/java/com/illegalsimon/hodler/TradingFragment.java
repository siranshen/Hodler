package com.illegalsimon.hodler;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.illegalsimon.hodler.data.Symbol;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TradingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TradingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TradingFragment extends Fragment implements View.OnClickListener, TextWatcher {
    private static final String IS_BUY_KEY = "IS_BUY"; // Trade type of the fragment
    private static final String FROM_SYMBOL_KEY = "FROM_SYMBOL";
    private static final String TO_SYMBOL_KEY = "TO_SYMBOL";

    private static final int CRYPTO_DECIMAL_PLACES = 6;
    private static final String LEFT_ARROW = "<-";
    private static final String RIGHT_ARROW = "->";
    public static final String[][] BASE_TRADING_PAIRS = new String[][] {
            { Symbol.BTC.getName(), Symbol.USD.getName() },
            { Symbol.ETH.getName(), Symbol.USD.getName() },
            { Symbol.ETH.getName(), Symbol.BTC.getName() }
    };

    public static final String[] ORDER_TYPES = new String[] { "Limit", "Maker-or-cancel", "Immediate-or-cancel", "Auction-only" };

    private boolean isBuy;
    private Symbol toSymbol;
    private String[] tradingPairTitles;

    private Spinner mTradingPairSpinner;
    private Spinner mOrderTypeSpinner;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mTotalEditText;
    private OnFragmentInteractionListener mListener;

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

        ArrayAdapter<String> orderTypesAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner, ORDER_TYPES);
        mOrderTypeSpinner.setAdapter(orderTypesAdapter);

        view.findViewById(R.id.btn_gemini_order_book).setOnClickListener(this);
        view.findViewById(R.id.btn_open_orders).setOnClickListener(this);
        view.findViewById(R.id.btn_place_order).setOnClickListener(this);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_place_order:
                onClickPlaceOrderBtn();
                break;
            case R.id.btn_gemini_order_book:
                break;
            case R.id.btn_open_orders:
                break;
        }
    }

    private void onClickPlaceOrderBtn() {
        String price = mPriceEditText.getText().toString();
        String quantity = mQuantityEditText.getText().toString();
        if (price.isEmpty() || quantity.isEmpty()) {
            Toast.makeText(getContext().getApplicationContext(), "Input cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        Double priceDouble = Double.valueOf(price);
        Double quantityDouble = round(Double.valueOf(quantity), CRYPTO_DECIMAL_PLACES);
        if (priceDouble <= 0 || quantityDouble <= 0) {
            Toast.makeText(getContext().getApplicationContext(), "Improper order", Toast.LENGTH_SHORT).show();
            return;
        }

        mQuantityEditText.setText(String.valueOf(quantityDouble)); // This will trigger TextWatcher
        priceDouble = Double.valueOf(mPriceEditText.getText().toString());

        String[] tradingPair = BASE_TRADING_PAIRS[mTradingPairSpinner.getSelectedItemPosition()];
        String orderType = mOrderTypeSpinner.getSelectedItem().toString();

        mListener.onPlaceOrder(tradingPair[0].toLowerCase() + tradingPair[1].toLowerCase(), priceDouble, quantityDouble, orderType);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

        int decimalPlaces = toSymbol == Symbol.USD ? 2 : CRYPTO_DECIMAL_PLACES;

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

    private void setEditTextViewText(EditText editText, String text) {
        editText.removeTextChangedListener(this);
        editText.setText(text);
        editText.addTextChangedListener(this);
    }

    private double round(double number, int decimalPlaces) {
        double helperDouble = Math.pow(10.0, decimalPlaces);
        return Math.round(number * helperDouble) / helperDouble;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onPlaceOrder(String pairSymbol, double price, double amount, String orderType);
    }
}
