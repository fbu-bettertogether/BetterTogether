package com.example.bettertogether.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bettertogether.AwardsAdapter;
import com.example.bettertogether.FriendAdapter;
import com.example.bettertogether.HomeActivity;
import com.example.bettertogether.MainActivity;
import com.example.bettertogether.PostsAdapter;
import com.example.bettertogether.R;
import com.example.bettertogether.SimpleGroupAdapter;
import com.example.bettertogether.models.Award;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.example.bettertogether.models.Post;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import static com.parse.ParseUser.getCurrentUser;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnProfileFragmentInteraction} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String param_user = "param_user";
    public final String APP_TAG = "BetterTogether";

    private ParseUser user;
    private OnProfileFragmentInteraction mListener;

    private ImageView ivUserIcon;
    private TextView tvUsername;
    private TextView tvDate;
    private RecyclerView rvPosts;
    private RecyclerView rvGroups;
    private RecyclerView rvFriends;
    private RecyclerView rvAwards;
    private List<Post> posts;
    private List<Group> groups;
    private List<ParseUser> friends;
    private List<Award> awards;
    private List<Award> achievedAwards;
    private PostsAdapter postsAdapter;
    private FriendAdapter friendAdapter;
    private SimpleGroupAdapter simpleGroupAdapter;
    private AwardsAdapter awardsAdapter;


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
        groups = new ArrayList<>();
        friends = new ArrayList<>();
        awards = new ArrayList<>();
        achievedAwards = new ArrayList<>();
        postsAdapter = new PostsAdapter(getContext(), posts);
        friendAdapter = new FriendAdapter(friends, getFragmentManager());
        simpleGroupAdapter = new SimpleGroupAdapter(getContext(), groups);
        awardsAdapter = new AwardsAdapter(getContext(), awards, achievedAwards);
        queryPosts();
        queryFriends();
        queryGroups();
        queryAwards();
        queryAchievedAwards();
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
        rvGroups = view.findViewById(R.id.rvGroups);
        rvFriends = view.findViewById(R.id.rvFriends);
        rvAwards = view.findViewById(R.id.rvAwards);

        rvPosts.setAdapter(postsAdapter);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFriends.setAdapter(friendAdapter);
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        rvGroups.setAdapter(simpleGroupAdapter);
        rvGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAwards.setAdapter(awardsAdapter);
        rvAwards.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        if (user.get("profileImage") != null) {
            Glide.with(view.getContext())
                    .load(((ParseFile) user.get("profileImage")).getUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivUserIcon);
        }
        ivUserIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.getObjectId() == ParseUser.getCurrentUser().getObjectId()){
                    Toast.makeText(view.getContext(), "Is current user", Toast.LENGTH_SHORT).show();
                    mListener.createProfilePicture(view);
                }
            }
            
        });
        tvUsername.setText(user.getUsername());
        tvDate.setText(String.format("Joined %s", Post.getRelativeTimeAgo(user.getCreatedAt())));
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnProfileFragmentInteraction) {
            mListener = (OnProfileFragmentInteraction) context;
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
    public void queryGroups() {
        ParseQuery<Membership> parseQuery = new ParseQuery<Membership>(Membership.class);
        parseQuery.addDescendingOrder("createdAt");
        parseQuery.whereEqualTo("user", user);
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

                groups.addAll(Membership.getAllGroups(memberships));
                simpleGroupAdapter.notifyDataSetChanged();
            }
        });

    }

    public void queryFriends() {
        ParseQuery<ParseObject> query = user.getRelation("friends").getQuery();
        query.include("friends");
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

    public void queryAwards() {
        ParseQuery<Post> parseQuery = new ParseQuery<>("Award");
        parseQuery.addAscendingOrder("createdAt");
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

                // add new awards to the list and notify adapter
                awards.addAll((List<Award>) (Object) objects);
                awardsAdapter.notifyDataSetChanged();
            }
        });
    }

    public void queryAchievedAwards() {
        ParseUser user = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> query = user.getRelation("awards").getQuery();
        query.include("awards");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) {
                    Log.e("Querying awards", "error with query");
                    e.printStackTrace();
                    return;
                }
                // keep track of a new list of awards that the user has achieved
                achievedAwards.addAll((List<Award>) (Object) objects);
                awardsAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnProfileFragmentInteraction {
        void createProfilePicture(View view);
    }
}
