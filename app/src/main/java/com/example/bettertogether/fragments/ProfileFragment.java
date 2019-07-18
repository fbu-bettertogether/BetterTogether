package com.example.bettertogether.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bettertogether.FriendAdapter;
import com.example.bettertogether.MemberAdapter;
import com.example.bettertogether.PostsAdapter;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Award;
import com.example.bettertogether.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String param_user = "param_user";
    public final String APP_TAG = "BetterTogether";

    private ParseUser user;
    private OnFragmentInteractionListener mListener;

    private ImageView ivUserIcon;
    private TextView tvUsername;
    private TextView tvDate;
    private RecyclerView rvPosts;
    private RecyclerView rvAwards;
    private RecyclerView rvFriends;
    private List<Post> posts;
    private List<Award> awards;
    private List<ParseUser> friends;
    private PostsAdapter postsAdapter;
    private FriendAdapter friendAdapter;


    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(ParseUser varUser) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable(param_user, varUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable(param_user);
        }
        posts = new ArrayList<>();
        awards = new ArrayList<>();
        friends = new ArrayList<>();
        postsAdapter = new PostsAdapter(getContext(), posts);
        friendAdapter = new FriendAdapter(friends, getFragmentManager());
        queryPosts();
        queryFriends();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivUserIcon = view.findViewById(R.id.ivUserIcon);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvDate = view.findViewById(R.id.tvDate);
        rvPosts = view.findViewById(R.id.rvPosts);
        rvAwards = view.findViewById(R.id.rvAwards);
        rvFriends = view.findViewById(R.id.rvFriends);

        rvPosts.setAdapter(postsAdapter);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFriends.setAdapter(friendAdapter);
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));

        if (user.get("profileImage") != null) {
            Glide.with(view.getContext())
                    .load(((ParseFile) user.get("profileImage")).getUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivUserIcon);
        }
        ivUserIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        tvUsername.setText(user.getUsername());
        tvDate.setText(Post.getRelativeTimeAgo(user.getCreatedAt()));


    }
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
        }
    }

    private void queryPosts() {
        ParseQuery<Post> parseQuery = new ParseQuery<>("Post");
        parseQuery.addDescendingOrder("createdAt");
        parseQuery.whereEqualTo("user", user);
        parseQuery.setLimit(25);
        parseQuery.include("user");

        parseQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e != null) {
                    Log.e("Querying posts", "error with query");
                    e.printStackTrace();
                    return;
                }

                // add new posts to the list and notify adapter
                posts.addAll((List<Post>) (Object) objects);
                postsAdapter.notifyDataSetChanged();
            }
        });
    }

    public void queryFriends() {
        ParseQuery<ParseObject> query = user.getRelation("friends").getQuery();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) {
                    Log.e("Querying friends", "error with query");
                    e.printStackTrace();
                    return;
                }

                // add new posts to the list and notify adapter
                friends.addAll((List<ParseUser>) (Object) objects);
                friendAdapter.notifyDataSetChanged();

            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
