package com.illegalsimon.hodler.dashboard;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.illegalsimon.hodler.R;

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by shens on 11/8/2017.
 */

public class DashboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DashboardListItem> mListItems;
    private OnClickHandler mOnClickHandler;

    public interface OnClickHandler {
        void handleOnClickOrder(Date orderDate, String description, String status, boolean isPast);
        void handleOnClickLoadPastOrders();
    }

    public DashboardAdapter(OnClickHandler onClickHandler) {
        mOnClickHandler = onClickHandler;
    }

    public List<DashboardListItem> getListItems() {
        return mListItems;
    }

    public void setListItems(List<DashboardListItem> listItems) {
        mListItems = listItems;
        notifyDataSetChanged();
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
                labelViewHolder.mLabelTextView.setText(((DashboardLabel) mListItems.get(position)).label);
                break;
            case DashboardListItem.BALANCE:
                BalanceViewHolder balanceViewHolder = (BalanceViewHolder) holder;
                DashboardBalance dashboardBalance = (DashboardBalance) mListItems.get(position);
                balanceViewHolder.mSymbolTextView.setText(dashboardBalance.symbol);
                balanceViewHolder.mBalanceTextView.setText(String.format(Locale.US, "%s / %s", dashboardBalance.available, dashboardBalance.balance));
                break;
            case DashboardListItem.ORDER:
                OrderViewHolder orderViewHolder = (OrderViewHolder) holder;
                final DashboardOrder order = (DashboardOrder) mListItems.get(position);
                orderViewHolder.mOrderTextView.setText(order.description);
                orderViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnClickHandler.handleOnClickOrder(order.placedDate, order.description, order.status, order.isPast);
                    }
                });
                break;
            case DashboardListItem.MESSAGE:
                final MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
                final DashboardMessage message = (DashboardMessage) mListItems.get(position);
                messageViewHolder.mMessageTextView.setText(message.message);
                if (message.isPastOrdersLoader) {
                    messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            messageViewHolder.mMessageTextView.setText("Loading...");
                            mOnClickHandler.handleOnClickLoadPastOrders();
                            view.setOnClickListener(null);
                        }
                    });
                }
                break;
            default:
                throw new IllegalArgumentException("View type not supported");
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mListItems.get(position).type;
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
            mOrderTextView = itemView.findViewById(R.id.tv_order);

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