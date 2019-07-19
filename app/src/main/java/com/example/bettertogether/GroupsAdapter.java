package com.example.bettertogether;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bettertogether.fragments.GroupFragment;
import com.example.bettertogether.models.Group;

import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {

    private Context context;
    private List<Group> groups;

    public GroupsAdapter(Context context, List<Group> groups) {
        this.context = context;
        this.groups = groups;
    }

    @NonNull
    @Override
    public GroupsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view);
    }

    // bind the data into the viewholder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get group at the current position
        Group group = groups.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView ivGroupProf;
        private TextView tvGroupName;
        private TextView tvCategory;
        private TextView tvDescription;
        private TextView tvDates;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // lookup widgets by id
            ivGroupProf = itemView.findViewById(R.id.ivGroupProf);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDates = itemView.findViewById(R.id.tvDates);

            itemView.setOnClickListener(this);
        }

        public void bind(Group group) {

            tvGroupName.setText(group.getName());

            // loading in the rest of the group fields if they are available
//            if (group.getCategory() != null) {
//                tvCategory.setText(group.getCategory());
//            } else {
                tvCategory.setText("");
//            }

            if (group.getDescription() != null) {
                tvDescription.setText(group.getDescription());
            } else {
                tvDescription.setText(String.format("We set aside time to meet our goals %d times every week!", group.getFrequency()));
            }

            if(group.getIcon() != null) {
                Glide.with(context)
                        .load(group.getIcon().getUrl())
                        .into(ivGroupProf);
            }

            if(group.getIsActive()) {
                tvDates.setText("Active: " + group.getStartDate() + " - " + group.getEndDate());
                tvDates.setTextColor(ContextCompat.getColor(context, R.color.teal));
            } else {
                tvDates.setText("Inactive: " + group.getStartDate() + " - " + group.getEndDate());
                tvDates.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            }
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                // get the clicked-on group
                Group group = groups.get(position);
                // switch to group-detail view fragment
                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                GroupFragment fragment = GroupFragment.newInstance(group);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            }
        }
    }
}
