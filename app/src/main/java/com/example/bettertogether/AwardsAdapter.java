package com.example.bettertogether;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bettertogether.fragments.AwardFragment;
import com.example.bettertogether.models.Award;
import com.parse.ParseFile;

import java.util.List;

public class AwardsAdapter extends RecyclerView.Adapter<AwardsAdapter.ViewHolder> {

    private Context context;
    private List<Award> awards;
    private List<Award> achievedAwards;

    public AwardsAdapter(Context context, List<Award> awards, List<Award> achievedAwards) {
        this.context = context;
        this.awards = awards;
        this.achievedAwards = achievedAwards;
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

        int placeholderId = R.drawable.medal;

        ParseFile awardImage = (ParseFile) award.get("icon");
        if (awardImage != null) {
            Glide.with(context)
                    .load(awardImage.getUrl())
                    .apply(new RequestOptions().placeholder(placeholderId)
                            .error(placeholderId))
                    .into(holder.ivAwardImage);
        }

        // decide to tint the medal to gold or not depending on whether the user has gotten the award
        for (int i = 0; i < achievedAwards.size(); i++) {
            if (achievedAwards.get(i).get("name").toString().equalsIgnoreCase(award.get("name").toString())) {
                Resources res = context.getResources();
                final int goldTint = res.getColor(R.color.gold);
                holder.ivAwardImage.setColorFilter(goldTint);
            }
        }
    }

    @Override
    public int getItemCount() {
        return awards.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivAwardImage;
        public TextView tvAwardName;

        public ViewHolder(View itemView) {
            super(itemView);

            // lookup widgets
            tvAwardName = (TextView) itemView.findViewById(R.id.tvAwardName);
            ivAwardImage = (ImageView) itemView.findViewById(R.id.ivAwardImage);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Award award = awards.get(position);
                // switch to award-detail view fragment
                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                AwardFragment fragment = AwardFragment.newInstance(award);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            }
        }
    }
}
