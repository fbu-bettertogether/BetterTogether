package com.example.bettertogether;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CheckAdapter extends RecyclerView.Adapter<CheckAdapter.ViewHolder> {
    int checks;
    int maxChecks;
    Context context;

    public CheckAdapter(int checks, int maxChecks) {
        this.checks = checks;
        this.maxChecks = maxChecks;
    }

    @NonNull
    @Override
    public CheckAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View checkView = inflater.inflate(R.layout.item_check, parent, false);
        CheckAdapter.ViewHolder viewHolder = new CheckAdapter.ViewHolder(checkView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CheckAdapter.ViewHolder holder, int position) {
        if (position < checks) {
            holder.ivCheck.setImageDrawable(context.getDrawable(R.drawable.check_circle));
        }

    }

    @Override
    public int getItemCount() {
        return maxChecks;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivCheck;

        public ViewHolder(View itemView) {
            super(itemView);
            // lookup widgets
            ivCheck = (ImageView) itemView.findViewById(R.id.ivCheck);
        }
    }
}
