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
import com.example.bettertogether.models.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private Context context;
    private List<Post> posts;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View postView = inflater.inflate(R.layout.item_post, parent, false);
        ViewHolder viewHolder = new ViewHolder(postView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostsAdapter.ViewHolder holder, int position) {
        // get post at that position
        final Post post = posts.get(position);

        ParseUser user = (ParseUser) post.get("user");

        // populate the post views
        holder.tvUsername.setText(user.getString("username"));
        holder.tvBody.setText(post.getDescription());
        holder.tvTime.setText(post.getRelativeTimeAgo(post.getCreatedAt()));

        ParseFile profileImage = (ParseFile) user.get("profileImage");
        if (profileImage != null) {
            Glide.with(context)
                    .load(profileImage.getUrl())
                    .into(holder.ivProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivProfileImage;
        public TextView tvUsername;
        public TextView tvBody;
        public TextView tvTime;
        public ImageButton btnLike;
        public ImageView ivMedia;

        public ViewHolder(View itemView) {
            super(itemView);

            // lookup widgets
            ivProfileImage = (ImageView) itemView.findViewById(R.id.ivProfileImage);
            tvUsername = (TextView) itemView.findViewById(R.id.tvUsername);
            tvBody = (TextView) itemView.findViewById(R.id.tvBody);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            btnLike = (ImageButton) itemView.findViewById(R.id.ivReply);
            ivMedia = (ImageView) itemView.findViewById(R.id.ivMedia);
        }
    }
}
