package com.example.bettertogether;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class AddUsersAdapter extends RecyclerView.Adapter<AddUsersAdapter.ViewHolder> {

    AddUsersAdapter.ViewHolder viewHolder;
    private List<ParseUser> users;
    private List<ParseUser> addedMembers;

    private Context context;

    public AddUsersAdapter(List<ParseUser> users, List<ParseUser> taggedUsers) {
        this.users = users;
        this.addedMembers = taggedUsers;

    }

    @NonNull
    @Override
    public AddUsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_add_user, parent, false);
        viewHolder = new AddUsersAdapter.ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final AddUsersAdapter.ViewHolder holder, int position) {
        // get the data according to position

        final ParseUser user = users.get(position);
        if (user.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
            holder.ivAdd.setVisibility(View.INVISIBLE);
            holder.ivOwner.setVisibility(View.VISIBLE);
        } else {
            holder.tvUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (addedMembers.contains(user)) {
                        addedMembers.remove(user);
                        holder.ivAdd.setVisibility(View.INVISIBLE);
                    } else {
                        addedMembers.add(user);
                        holder.ivAdd.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        holder.tvUsername.setText( user.getString("username"));
        if (user.get("profileImage") != null) {
            Glide.with(context).load(((ParseFile) user.get("profileImage")).getUrl()).apply(RequestOptions.circleCropTransform()).into(holder.ivUserIcon);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView ivUserIcon;
        private ImageView ivAdd;
        private TextView tvUsername;
        private ImageView ivOwner;

        private ViewHolder(View itemView) {
            super(itemView);
            ivUserIcon = itemView.findViewById(R.id.ivUserIcon);
            ivAdd = itemView.findViewById(R.id.ivAdd);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivOwner = itemView.findViewById(R.id.ivOwner);
        }
    }
}