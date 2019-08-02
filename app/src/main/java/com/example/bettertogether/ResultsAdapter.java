package com.example.bettertogether;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.bettertogether.fragments.GroupFragment;
import com.example.bettertogether.fragments.ProfileFragment;
import com.example.bettertogether.models.Group;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {

    private Context context;
    private List<ParseObject> groupsAndUsers;

    public ResultsAdapter(Context context, List<Group> groups, List<ParseUser> users) {
        this.context = context;
        this.groupsAndUsers = new ArrayList<>();
        groupsAndUsers.addAll(users);
        groupsAndUsers.addAll(groups);
    }

    @NonNull
    @Override
    public ResultsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_result, parent, false);
        return new ResultsAdapter.ViewHolder(view);
    }

    // bind the data into the viewholder
    @Override
    public void onBindViewHolder(@NonNull ResultsAdapter.ViewHolder holder, int position) {
        // get group at the current position
        ParseObject parseObject = groupsAndUsers.get(position);
        holder.bind(parseObject);
    }

    @Override
    public int getItemCount() {
        return groupsAndUsers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView ivGroupProf;
        private TextView tvGroupName;
        private TextView tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // lookup widgets by id
            ivGroupProf = itemView.findViewById(R.id.ivGroupProf);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            itemView.setOnClickListener(this);
        }

        public void bind(ParseObject object) {
            if (object.getClassName().equals("Group")) {
                Group group = (Group) object;
                tvGroupName.setText(group.getName());

                if (group.getDescription() != null) {
                    tvDescription.setText(group.getDescription());
                } else {
                    tvDescription.setText(String.format("We set aside time to meet our goals %d times every week!", group.getFrequency()));
                }

                if (group.getIcon() != null) {
                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));
                    Glide.with(context)
                            .load(group.getIcon().getUrl())
                            .apply(requestOptions)
                            .into(ivGroupProf);
                } else {
                    Glide.with(context)
                            .load(Drawable.createFromPath("@tools:sample/backgrounds/scenic"))
                            .apply(RequestOptions.circleCropTransform())
                            .into(ivGroupProf);
                }
            } else if (object.getClassName().equals("_User")) {
                ParseUser user = (ParseUser) object;
                tvGroupName.setText(user.getUsername());
                if (user.get("profileImage") != null) {
                    Glide.with(context)
                            .load(((ParseFile) user.get("profileImage")).getUrl())
                            .apply(RequestOptions.circleCropTransform())
                            .into(ivGroupProf);
                } else {
                    Glide.with(context)
                            .load(Drawable.createFromPath("@tools:sample/backgrounds/scenic"))
                            .apply(RequestOptions.circleCropTransform())
                            .into(ivGroupProf);
                }

            }
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                if (groupsAndUsers.get(position).getClassName().equals("Group")) {
                    // get the clicked-on group
                    Group group = (Group) groupsAndUsers.get(position);
                    // switch to group-detail view fragment
                    FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                    GroupFragment fragment = GroupFragment.newInstance(group);
                    fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                } else {
                    ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, ProfileFragment.newInstance((ParseUser) groupsAndUsers.get(position))).commit();
                }
            }
        }
    }
}
