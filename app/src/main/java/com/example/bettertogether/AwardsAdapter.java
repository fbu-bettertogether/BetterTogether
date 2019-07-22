package com.example.bettertogether;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bettertogether.models.Award;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AwardsAdapter extends RecyclerView.Adapter<AwardsAdapter.ViewHolder> {

    private Context context;
    private List<Award> awards;

    public AwardsAdapter(Context context, List<Award> awards) {
        this.context = context;
        this.awards = awards;
    }

    @NonNull
    @Override
    public AwardsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View awardView = inflater.inflate(R.layout.item_award, parent, false);
        ViewHolder viewHolder = new ViewHolder(awardView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AwardsAdapter.ViewHolder holder, int position) {
        // get award at that position
        final Award award = awards.get(position);

        // populate the award views
        holder.tvAwardName.setText(award.getString("name"));
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        Date todayDate = new Date();
        String date = currentDate.format(todayDate);
        holder.tvDate.setText(date);

        ParseFile awardImage = (ParseFile) award.get("icon");
        if (awardImage != null) {
            Glide.with(context)
                    .load(awardImage.getUrl())
                    .into(holder.ivAwardImage);
        }
    }

    @Override
    public int getItemCount() {
        return awards.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivAwardImage;
        public TextView tvAwardName;
        public TextView tvDate;

        public ViewHolder(View itemView) {
            super(itemView);

            // lookup widgets
            ivAwardImage = (ImageView) itemView.findViewById(R.id.ivAwardImage);
            tvAwardName = (TextView) itemView.findViewById(R.id.tvAwardName);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
        }
    }
}
