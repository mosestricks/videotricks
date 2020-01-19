package com.lightricks.videotricks.util;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.lightricks.videotricks.R;
import com.lightricks.videotricks.databinding.ClickableListItemBinding;

import java.util.Collections;
import java.util.List;

public class ClickableListAdapter extends RecyclerView.Adapter<ClickableListAdapter.ItemHolder> {
    private List<String> labels = Collections.emptyList();
    private ClickListener clickListener;

    public void setLabels(List<String> labels) {
        this.labels = labels;

        notifyDataSetChanged();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.clickable_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemHolder holder, int position) {
        String label = labels.get(position);
        holder.getTextView().setText(label);
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClick(holder.getAdapterPosition(), label);
            }
        });
    }

    @Override
    public int getItemCount() {
        return labels.size();
    }

    public interface ClickListener {
        void onClick(int position, String label);
    }

    static class ItemHolder extends RecyclerView.ViewHolder {
        private ClickableListItemBinding binding;

        ItemHolder(@NonNull ClickableListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        TextView getTextView() {
            return binding.textView;
        }
    }
}
