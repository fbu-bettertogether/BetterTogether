package com.example.bettertogether;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bettertogether.fragments.ProfileFragment;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder>{
    FriendAdapter.ViewHolder viewHolder;
    private List<ParseUser> users;
    private FragmentManager fragmentManager;
    private Context context;

    public FriendAdapter(List<ParseUser> users, FragmentManager fragmentManager) {
        this.users = users;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public FriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.friend_layout, parent, false);
        viewHolder = new FriendAdapter.ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendAdapter.ViewHolder holder, int position) {
        // get the data according to position

        final ParseUser user = users.get(position);

        holder.ivUserIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager.beginTransaction().replace(R.id.flContainer, ProfileFragment.newInstance(user)).commit();
            }
        }
        );
        if ((user.get("profileImage") != null)) {
            Glide.with(context).load(((ParseFile) user.get("profileImage")).getUrl()).apply(RequestOptions.circleCropTransform()).into(holder.ivUserIcon);
        }


    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView ivUserIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            ivUserIcon = itemView.findViewById(R.id.ivUserIcon);
        }

    }

}
