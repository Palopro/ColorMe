package com.palopro.colorme.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.palopro.colorme.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainListRecyclerViewAdapter extends RecyclerView.Adapter<MainListRecyclerViewAdapter.ViewHolder> {
    private final List<ListItem> listItems;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout linearLayout;
        public TextView titleText;
        public TextView descText;

        public ViewHolder(LinearLayout linearLayout) {
            super(linearLayout);
            this.linearLayout = linearLayout;
            titleText = linearLayout.findViewById(R.id.main_list_title);
            descText = linearLayout.findViewById(R.id.main_list_description);
        }
    }

    public MainListRecyclerViewAdapter(List<ListItem> listItems) {
        this.listItems = listItems;
    }

    @NonNull
    @NotNull
    @Override
    public MainListRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup viewGroup, int i) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.main_list_item, viewGroup, false);
        return new ViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MainListRecyclerViewAdapter.ViewHolder viewHolder, int i) {
        ListItem listItem = listItems.get(i);
        viewHolder.titleText.setText(listItem.getTitle());
        viewHolder.descText.setText(listItem.getDescription());
        viewHolder.linearLayout.setOnClickListener(listItem.getOnClickListener());
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }
}
