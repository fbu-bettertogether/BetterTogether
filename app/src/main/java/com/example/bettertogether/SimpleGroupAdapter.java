package com.example.bettertogether;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.bettertogether.fragments.GroupFragment;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.parse.ParseUser.getCurrentUser;

public class SimpleGroupAdapter extends RecyclerView.Adapter<SimpleGroupAdapter.ViewHolder> {

    private Context context;
    private List<Group> groups;
    private List<Group> userGroups;

    public SimpleGroupAdapter(Context context, List<Group> groups) {
        this.context = context;
        this.groups = groups;
        this.userGroups = new ArrayList<>();
        queryGroups();
    }

    @NonNull
    @Override
    public SimpleGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_simple_group, parent, false);
        return new SimpleGroupAdapter.ViewHolder(view);
    }

    // bind the data into the viewholder
    @Override
    public void onBindViewHolder(@NonNull SimpleGroupAdapter.ViewHolder holder, int position) {
        // get group at the current position
        Group group = groups.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void queryGroups() {
        ParseQuery<Membership> parseQuery = new ParseQuery<Membership>(Membership.class);
        parseQuery.addDescendingOrder("createdAt");
        parseQuery.whereEqualTo("user", getCurrentUser());
        parseQuery.include("group");
        parseQuery.findInBackground(new FindCallback<Membership>() {
            @Override
            public void done(List<Membership> memberships, ParseException e) {
                if (e != null) {
                    Log.e("Querying groups", "error with query");
                    e.printStackTrace();
                    return;
                }

                Log.d("carmel",Integer.toString(memberships.size()));
                userGroups.addAll(Membership.getAllGroups(memberships));
            }
        });

    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView ivGroupProf;
        private TextView tvGroupName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // lookup widgets by id
            ivGroupProf = itemView.findViewById(R.id.ivGroupProf);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);

            itemView.setOnClickListener(this);
        }

        public void bind(Group group) {

            tvGroupName.setText(group.getName());

            // loading in the rest of the group fields if they are available

            if(group.getIcon() != null) {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));
                Glide.with(context)
                        .load(group.getIcon().getUrl())
                        .apply(requestOptions)
                        .into(ivGroupProf);
            }

            if(group.getIsActive()) {
                tvGroupName.setTextColor(context.getResources().getColor(R.color.orange));
            } else {
                tvGroupName.setTextColor(context.getResources().getColor(R.color.gray));
            }
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            boolean found = false;
                if (position != RecyclerView.NO_POSITION) {
                    for (int i = 0; i < userGroups.size() - 1; i++)
                        if (userGroups.get(i).getObjectId().equals(groups.get(position).getObjectId())) {
                            found = true;
                        }
                    }
                    if (found) {
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
