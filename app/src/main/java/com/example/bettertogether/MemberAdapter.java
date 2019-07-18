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
import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

    MemberAdapter.ViewHolder viewHolder;
    private List<ParseUser> users;
    private List<ParseUser> taggedUsers;

    private Context context;

    public MemberAdapter(List<ParseUser> users, List<ParseUser> taggedUsers) {
        this.users = users;
        this.taggedUsers = taggedUsers;

    }

    @NonNull
    @Override
    public MemberAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.member_layout, parent, false);
        viewHolder = new MemberAdapter.ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MemberAdapter.ViewHolder holder, int position) {
        // get the data according to position

        final ParseUser user = users.get(position);

        holder.tvUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (taggedUsers.contains(user)) {
                    taggedUsers.remove(user);
                    holder.ivTag.setVisibility(View.INVISIBLE);
                } else {
                    taggedUsers.add(user);
                    holder.ivTag.setVisibility(View.VISIBLE);
                }
            }
        });


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
        public ImageView ivUserIcon;
        public ImageView ivTag;
        public TextView tvUsername;

        public ViewHolder(View itemView) {
            super(itemView);
            ivUserIcon = itemView.findViewById(R.id.ivUserIcon);
            ivTag = itemView.findViewById(R.id.ivAdd);
            tvUsername = itemView.findViewById(R.id.tvUsername);

        }

    }

}
