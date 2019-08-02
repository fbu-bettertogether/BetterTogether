package com.example.bettertogether;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bettertogether.fragments.ProfileFragment;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Like;
import com.example.bettertogether.models.Post;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private Context context;
    private List<Post> posts;
    private FragmentManager fragmentManager;

    public PostsAdapter(Context context, List<Post> posts, FragmentManager fragmentManager) {
        this.context = context;
        this.posts = posts;
        this.fragmentManager = fragmentManager;
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
    public void onBindViewHolder(@NonNull final PostsAdapter.ViewHolder holder, int position) {
        // get post at that position
        final Post post = posts.get(position);

        final ParseUser user = (ParseUser) post.get("user");

        // populate the post views
        holder.tvUsername.setText(user.getString("username"));
        holder.tvBody.setText(post.getDescription());
        holder.tvTime.setText(post.getRelativeTimeAgo(post.getCreatedAt()));

        if (user.getUsername().equals("Check In Bot")) {
            holder.itemView.setBackground(context.getResources().getDrawable(R.drawable.fade_orange));
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        ParseFile profileImage = (ParseFile) user.get("profileImage");
        if (profileImage != null) {
            Glide.with(context)
                    .load(profileImage.getUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.ivProfileImage);
        }
        holder.ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager.beginTransaction().replace(R.id.flContainer, ProfileFragment.newInstance(user)).commit();
            }
        });
        holder.tvUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager.beginTransaction().replace(R.id.flContainer, ProfileFragment.newInstance(user)).commit();
            }
        });

        // setting size of image depending on whether or not media was attached to post
        if (post.getMedia() != null) {
            holder.ivMedia.requestLayout();
            holder.ivMedia.getLayoutParams().height = 350;
            holder.ivMedia.getLayoutParams().width = 350;
            Glide.with(context)
                    .load(post.getMedia().getUrl())
                    .into(holder.ivMedia);
        } else {
            holder.ivMedia.requestLayout();
            holder.ivMedia.getLayoutParams().height = 0;
            holder.ivMedia.getLayoutParams().width = 0;
        }

        ParseQuery<Like> query = new ParseQuery<Like>(Like.class);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.whereEqualTo("post", post);
        query.getFirstInBackground(new GetCallback<Like>() {
                                       @Override
                                       public void done(Like object, ParseException e) {
                                           if (object == null) {
                                                       holder.btnLike.setImageDrawable(context.getDrawable(R.drawable.thumb_up_outline));
                                           } else {
                                               holder.btnLike.setImageDrawable(context.getDrawable(R.drawable.thumb_up));
                                                   }
                                               }

                                           }
                                   );
        holder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseQuery<Like> query = new ParseQuery<Like>(Like.class);
                query.whereEqualTo("user", ParseUser.getCurrentUser());
                query.whereEqualTo("post", post);
                query.getFirstInBackground(new GetCallback<Like>() {
                    @Override
                    public void done(Like object, ParseException e) {
                        if (object != null) {
                            object.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    holder.btnLike.setImageDrawable(context.getDrawable(R.drawable.thumb_up_outline));
                                }
                            });
                        } else {
                            Like like = new Like();
                            like.setPost(post);
                            like.setUser(ParseUser.getCurrentUser());
                            like.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    holder.btnLike.setImageDrawable(context.getDrawable(R.drawable.thumb_up));
                                }
                            });

                        }

                    }
                });
            }
        });
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
            btnLike = (ImageButton) itemView.findViewById(R.id.ivLike);
            ivMedia = (ImageView) itemView.findViewById(R.id.ivMedia);
        }
    }
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }
}
