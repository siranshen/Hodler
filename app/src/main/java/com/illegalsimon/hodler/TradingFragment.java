package com.illegalsimon.hodler;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.illegalsimon.hodler.data.Symbol;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TradingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TradingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TradingFragment extends Fragment {
    private static final String IS_BUY_KEY = "IS_BUY"; // Trade type of the fragment
    private static final String FROM_SYMBOL_KEY = "FROM_SYMBOL";
    private static final String TO_SYMBOL_KEY = "TO_SYMBOL";

    public static final String LEFT_ARROW = "<-";
    public static final String RIGHT_ARROW = "->";
    public static final String[][] BASE_TRADING_PAIRS = new String[][] {
            { Symbol.BTC.getName(), Symbol.USD.getName() },
            { Symbol.ETH.getName(), Symbol.USD.getName() },
            { Symbol.ETH.getName(), Symbol.BTC.getName() }
    };

    public static final String[] ORDER_TYPES = new String[] { "Limit", "Maker-or-cancel", "Immediate-or-cancel", "auction-only" };

    private boolean isBuy;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trading, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Symbol fromSymbol = (Symbol) getArguments().get(FROM_SYMBOL_KEY);
        if (fromSymbol == null) fromSymbol = Symbol.BTC;
        Symbol toSymbol = (Symbol) getArguments().get(TO_SYMBOL_KEY);
        if (toSymbol == null) toSymbol = Symbol.USD;

        mTradingPairSpinner = view.findViewById(R.id.spinner_trade_pair);
        mOrderTypeSpinner = view.findViewById(R.id.spinner_order_type);
        mPriceEditText = view.findViewById(R.id.et_price);
        mQuantityEditText = view.findViewById(R.id.et_quantity);
        mTotalEditText = view.findViewById(R.id.et_total);

        String[] tradingPairTitles = getTradingPairTitles();
        ArrayAdapter<String> tradingPairAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner, tradingPairTitles);
        mTradingPairSpinner.setAdapter(tradingPairAdapter);
        for (int i = 0; i < tradingPairTitles.length; i++) {
            if (tradingPairTitles[i].equals(String.format("%s %s %s", fromSymbol.getName(), isBuy ? LEFT_ARROW : RIGHT_ARROW, toSymbol.getName()))) {
                mTradingPairSpinner.setSelection(i);
            }
        }

        ArrayAdapter<String> orderTypesAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner, ORDER_TYPES);
        mOrderTypeSpinner.setAdapter(orderTypesAdapter);

        super.onViewCreated(view, savedInstanceState);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    private String[] getTradingPairTitles() {
        String[] pairs = new String[BASE_TRADING_PAIRS.length];
        String arrow = isBuy ? LEFT_ARROW : RIGHT_ARROW;
        for (int i = 0; i < BASE_TRADING_PAIRS.length; i++) {
            pairs[i] = String.format("%s %s %s", BASE_TRADING_PAIRS[i][0], arrow, BASE_TRADING_PAIRS[i][1]);
        }
        return pairs;
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
