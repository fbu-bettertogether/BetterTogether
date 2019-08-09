package com.example.bettertogether;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseFile;

import java.util.List;

public class GraphAdapter extends RecyclerView.Adapter<GraphAdapter.ViewHolder> {
    GraphAdapter.ViewHolder viewHolder;
    Context context;
    private List<Integer> entries;
    private List<ParseFile> imageEntries;

    public GraphAdapter(List<Integer> entries, List<ParseFile> imageEntries) {
        this.entries = entries;
        this.imageEntries = imageEntries;
    }

    @NonNull
    @Override
    public GraphAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_graph, parent, false);
        viewHolder = new GraphAdapter.ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GraphAdapter.ViewHolder holder, int position) {
        if (imageEntries.get(position) != null) {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));
            Glide.with(context)
                    .load(imageEntries.get(position).getUrl())
                    .apply(requestOptions)
                    .into(holder.ivGroupIcon);
        }
        int color = position % 4;
        switch (color) {
            case 0:
                holder.ivBar.setColorFilter(context.getResources().getColor(R.color.o1));
                break;
            case 1:
                holder.ivBar.setColorFilter(context.getResources().getColor(R.color.o4));
                break;
            case 2:
                holder.ivBar.setColorFilter(context.getResources().getColor(R.color.o8));
                break;
            case 3:
                holder.ivBar.setColorFilter(context.getResources().getColor(R.color.originalOrange));
                break;

        }
        holder.ivBar.setMinimumHeight(entries.get(position) * 64);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivGroupIcon;
        public ImageView ivBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGroupIcon = itemView.findViewById(R.id.ivGroupIcon);
            ivBar = itemView.findViewById(R.id.ivBar);
        }
    }
}
