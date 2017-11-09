package com.illegalsimon.hodler;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

/**
 * Created by shens on 11/8/2017.
 */

class DashboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<DashboardListItem> mListItems;

    public DashboardAdapter(List<DashboardListItem> listItems) {
        mListItems = listItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == 0) {
            return new LabelViewHolder(inflater.inflate(R.layout.label, parent, false));
        } else if (viewType == 1) {
            return new BalanceViewHolder(inflater.inflate(R.layout.balance_list_item, parent, false));
        } else if (viewType == 2) {
            return new OrderViewHolder(inflater.inflate(R.layout.order_list_item, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = holder.getItemViewType();
        if (viewType == 0) {
            ((LabelViewHolder) holder).mLabelTextView.setText(((DashboardLabel) mListItems.get(position)).mLabel);
        } else if (viewType == 1) {
            BalanceViewHolder balanceViewHolder = (BalanceViewHolder) holder;
            DashboardBalance dashboardBalance = (DashboardBalance) mListItems.get(position);
            balanceViewHolder.mSymbolTextView.setText(dashboardBalance.mSymbol);
            balanceViewHolder.mBalanceTextView.setText(String.format(Locale.US, "%s / %s", dashboardBalance.mAvailable, dashboardBalance.mBalance));
        } else if (viewType == 2) {
            OrderViewHolder orderViewHolder = (OrderViewHolder) holder;
            orderViewHolder.mOrderTextView.setText(((DashboardOrder) mListItems.get(position)).mDescription);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mListItems.get(position).mType.getValue();
    }

    @Override
    public int getItemCount() {
        return mListItems.size();
    }

    private class LabelViewHolder extends RecyclerView.ViewHolder {
        TextView mLabelTextView;

        public LabelViewHolder(View itemView) {
            super(itemView);
            mLabelTextView = itemView.findViewById(R.id.tv_label);
        }
    }

    private class BalanceViewHolder extends RecyclerView.ViewHolder {
        TextView mSymbolTextView;
        TextView mBalanceTextView;

        public BalanceViewHolder(View itemView) {
            super(itemView);
            mSymbolTextView = itemView.findViewById(R.id.tv_symbol);
            mBalanceTextView = itemView.findViewById(R.id.tv_balance);
        }
    }

    private class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView mOrderTextView;

        public OrderViewHolder(View itemView) {
            super(itemView);
            mOrderTextView = itemView.findViewById(R.id.tv_order); // TODO: onClickListener
        }
    }
}