package com.illegalsimon.hodler.dashboard;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.illegalsimon.hodler.R;

import java.util.List;
import java.util.Locale;

/**
 * Created by shens on 11/8/2017.
 */

public class DashboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DashboardListItem> mListItems;

    public DashboardAdapter(List<DashboardListItem> listItems) {
        mListItems = listItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType){
            case DashboardListItem.LABEL:
                return new LabelViewHolder(inflater.inflate(R.layout.label, parent, false));
            case DashboardListItem.BALANCE:
                return new BalanceViewHolder(inflater.inflate(R.layout.balance_list_item, parent, false));
            case DashboardListItem.ORDER:
                return new OrderViewHolder(inflater.inflate(R.layout.order_list_item, parent, false));
            case DashboardListItem.MESSAGE:
                return new MessageViewHolder(inflater.inflate(R.layout.message_list_item, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = holder.getItemViewType();
        switch (viewType) {
            case DashboardListItem.LABEL:
                LabelViewHolder labelViewHolder = (LabelViewHolder) holder;
                labelViewHolder.mLabelTextView.setText(((DashboardLabel) mListItems.get(position)).mLabel);
                break;
            case DashboardListItem.BALANCE:
                BalanceViewHolder balanceViewHolder = (BalanceViewHolder) holder;
                DashboardBalance dashboardBalance = (DashboardBalance) mListItems.get(position);
                balanceViewHolder.mSymbolTextView.setText(dashboardBalance.mSymbol);
                balanceViewHolder.mBalanceTextView.setText(String.format(Locale.US, "%s / %s", dashboardBalance.mAvailable, dashboardBalance.mBalance));
                break;
            case DashboardListItem.ORDER:
                OrderViewHolder orderViewHolder = (OrderViewHolder) holder;
                orderViewHolder.mOrderTextView.setText(((DashboardOrder) mListItems.get(position)).mDescription);
                break;
            case DashboardListItem.MESSAGE:
                MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
                messageViewHolder.mMessageTextView.setText(((DashboardMessage) mListItems.get(position)).mMessage);
                break;
            default:
                throw new IllegalArgumentException("View type not supported");
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mListItems.get(position).mType;
    }

    @Override
    public int getItemCount() {
        return mListItems.size();
    }

    private class LabelViewHolder extends RecyclerView.ViewHolder {
        TextView mLabelTextView;

        LabelViewHolder(View itemView) {
            super(itemView);
            mLabelTextView = itemView.findViewById(R.id.tv_label);
        }
    }

    private class BalanceViewHolder extends RecyclerView.ViewHolder {
        TextView mSymbolTextView;
        TextView mBalanceTextView;

        BalanceViewHolder(View itemView) {
            super(itemView);
            mSymbolTextView = itemView.findViewById(R.id.tv_symbol);
            mBalanceTextView = itemView.findViewById(R.id.tv_balance);
        }
    }

    private class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView mOrderTextView;

        OrderViewHolder(View itemView) {
            super(itemView);
            mOrderTextView = itemView.findViewById(R.id.tv_order); // TODO: onClickListener
        }
    }

    private class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView mMessageTextView;

        MessageViewHolder(View itemView) {
            super(itemView);
            mMessageTextView = itemView.findViewById(R.id.tv_message);
        }
    }
}