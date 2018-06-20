package io.github.pulakdp.flickrsearch.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.pulakdp.flickrsearch.R;

/**
 * Author: PulakDebasish
 */

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.SearchHistoryItemViewHolder> {

    ArrayList<String> searchItems;
    OnHistoryItemClickListener onHistoryItemClickListener;

    public interface OnHistoryItemClickListener {
        void onHistoryItemClick(String query);
    }

    public SearchHistoryAdapter(ArrayList<String> searchItems, OnHistoryItemClickListener onHistoryItemClickListener) {
        this.searchItems = searchItems;
        this.onHistoryItemClickListener = onHistoryItemClickListener;
    }

    @Override
    public SearchHistoryItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_history_item, parent, false);
        return new SearchHistoryItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SearchHistoryItemViewHolder holder, int position) {
        holder.historyText.setText(searchItems.get(position));
    }

    @Override
    public int getItemCount() {
        return searchItems.size();
    }

    public class SearchHistoryItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.search_history_item)
        TextView historyText;

        public SearchHistoryItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onHistoryItemClickListener.onHistoryItemClick(searchItems.get(getAdapterPosition()));
        }
    }

}
