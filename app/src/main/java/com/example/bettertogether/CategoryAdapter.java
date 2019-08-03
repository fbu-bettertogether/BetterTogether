package com.example.bettertogether;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bettertogether.models.Like;
import com.example.bettertogether.models.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{
    private List<Post> mPosts;
    Context context;
    //pass in the posts array in the constructor
    public CategoryAdapter(List<Post> posts) {
        mPosts = posts;
    }

    //for each row, inflate the layout and cache references into ViewHolder

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View postView = inflater.inflate(R.layout.item_post, parent, false);
        ViewHolder viewHolder = new ViewHolder(postView);
        return viewHolder;
    }

    //bind the values based on the position of the element

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //get the data according to position
        Post post = mPosts.get(position);

        //populate the views according to this data
        holder.tvUsername.setText(post.getUser().getUsername());
        holder.tvBody.setText(post.getDescription());

        holder.tvUser.setText(post.getUser().getUsername());

        String relativeDate = post.getRelativeTimeAgo(post.getCreatedAt());
        holder.tvTimestamp.setText(relativeDate);


        int placeholderId = R.drawable.handshake;

        ParseFile proPic = post.getMedia();
        if (proPic != null) {
            Glide.with(context)
                    .load(group.getIcon.getUrl())
                    .apply(new RequestOptions().placeholder(placeholderId)
                            .error(placeholderId))
                    .into(holder.ivProfileImage);
        }

        ParseFile media = post.getMedia();
        if (media != null) {
            Glide.with(context)
                    .load(media.getUrl())
                    .apply(new RequestOptions().placeholder(placeholderId)
                            .error(placeholderId))
                    .into(holder.ivImage);
        }
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    //create ViewHolder class

    class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivProfileImage;
        public TextView tvUsername;
        public TextView tvBody;
        public TextView tvTimestamp;
        public ImageView ivImage;
        public TextView tvUser;
        public ImageView ivLike;

        public ViewHolder(View itemView) {
            super(itemView);

            //perform findViewById lookups
            ivProfileImage = (ImageView) itemView.findViewById(R.id.ivProfileImage);
            tvUsername = (TextView) itemView.findViewById(R.id.tvUserName);
            tvBody = (TextView) itemView.findViewById(R.id.tvBody);
            tvTimestamp = (TextView) itemView.findViewById(R.id.tvTimestamp);
            ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            tvUser = (TextView) itemView.findViewById(R.id.tvUser);
            ivLike = (ImageView) itemView.findViewById(R.id.ivLike);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // gets item position
                    int position = getAdapterPosition();
                    // make sure the position is valid, i.e. actually exists in the view
                    if (position != RecyclerView.NO_POSITION) {
                        // get the post at the position, this won't work if the class is static
                        Post post = mPosts.get(position);
                        // create intent for the new activity
                        Intent intent = new Intent(context, PostDetailsActivity.class);
                        // serialize the post using parceler, use its short name as a key
                        intent.putExtra(Post.class.getSimpleName(), Parcels.wrap(post));
                        // show the activity
                        context.startActivity(intent);
                    }
                }
            });
            ivLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    // make sure the position is valid, i.e. actually exists in the view
                    if (position != RecyclerView.NO_POSITION) {
                        // get the post at the position, this won't work if the class is static
                        final Post post = mPosts.get(position);
                        final ParseUser user = ParseUser.getCurrentUser();
                        ParseRelation<Like> relation = post.getRelation("likes");
                        final Like like = new Like();
                        like.setUser(ParseUser.getCurrentUser());
                        like.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Log.d("PostAdapter", "Change like status success!");
                                } else {
                                    e.printStackTrace();
                                }
                            }
                        });

                        boolean liked = false;
                        try {
                            liked = !relation.getQuery().whereEqualTo("user", user).find().isEmpty();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (liked) {
                            relation.remove(like);
                            like.saveInBackground();
                            post.saveInBackground();
                        } else {
                            relation.add(like);
                            post.saveInBackground();
                        }
                        post.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
//                                    String text = "Liked by " + user.getUsername();
//                                    tvLikedBy.setText(text);
                                    Log.d("PostAdapter", "Change like status success!");
                                } else {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    notifyItemChanged(position);
                }
            });
        }
    }

    public void clear() {
        mPosts.clear();
        notifyDataSetChanged();
    }
}
