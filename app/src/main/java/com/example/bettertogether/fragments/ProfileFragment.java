package com.example.bettertogether.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bettertogether.AwardsAdapter;
import com.example.bettertogether.FriendAdapter;
import com.example.bettertogether.InvitationActivity;
import com.example.bettertogether.MainActivity;
import com.example.bettertogether.Messaging;
import com.example.bettertogether.MyFirebaseMessagingService;
import com.example.bettertogether.PostsAdapter;
import com.example.bettertogether.R;
import com.example.bettertogether.SimpleGroupAdapter;
import com.example.bettertogether.models.Award;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Invitation;
import com.example.bettertogether.models.Membership;
import com.example.bettertogether.models.Post;
import com.example.bettertogether.models.UserAward;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

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
    public final int INVITATION_REQUEST_CODE = 232;

    private ParseUser user;
    private Award friendshipGoals;
    private OnProfileFragmentInteraction mListener;

    private ImageView ivUserIcon;
    private TextView tvUsername;
    private RecyclerView rvPosts;
    private RecyclerView rvGroups;
    private RecyclerView rvFriends;
    private RecyclerView rvAwards;
    private ImageView ivFitnessPoints;
    private ImageView ivGetTogetherPoints;
    private ImageView ivServicePoints;
    private TextView tvFitnessPoints;
    private TextView tvGetTogetherPoints;
    private TextView tvServicePoints;
    private TextView tvNumGroups;
    private TextView tvNumFriends;
    private TextView tvNumAwards;
    private TextView tvNumPosts;

    private List<Post> posts;
    private List<Group> groups;
    private List<ParseUser> friends;
    private List<Award> awards;
    private List<Award> achievedAwards;
    private PostsAdapter postsAdapter;
    private FriendAdapter friendAdapter;
    private SimpleGroupAdapter simpleGroupAdapter;
    private AwardsAdapter awardsAdapter;
    private AwardFragment af;

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
        postsAdapter = new PostsAdapter(getContext(), posts, getFragmentManager());
        friendAdapter = new FriendAdapter(friends, getFragmentManager());
        simpleGroupAdapter = new SimpleGroupAdapter(getContext(), groups);
        awardsAdapter = new AwardsAdapter(getContext(), awards, achievedAwards);
        af = new AwardFragment();
        queryPosts();
        queryFriends();
        queryGroups();
        queryAwards();
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
        rvPosts = view.findViewById(R.id.rvPosts);
        rvGroups = view.findViewById(R.id.rvGroups);
        rvFriends = view.findViewById(R.id.rvFriends);
        rvAwards = view.findViewById(R.id.rvAwards);
        ivFitnessPoints = view.findViewById(R.id.ivFitnessPoints);
        ivGetTogetherPoints = view.findViewById(R.id.ivGetTogetherPoints);
        ivServicePoints = view.findViewById(R.id.ivServicePoints);
        tvFitnessPoints = view.findViewById(R.id.tvFitnessPoints);
        tvGetTogetherPoints = view.findViewById(R.id.tvGetTogetherPoints);
        tvServicePoints = view.findViewById(R.id.tvServicePoints);
        tvNumAwards = view.findViewById(R.id.tvNumAwards);
        tvNumFriends = view.findViewById(R.id.tvNumFriends);
        tvNumGroups = view.findViewById(R.id.tvNumGroups);
        tvNumPosts = view.findViewById(R.id.tvNumPosts);

        rvPosts.setAdapter(postsAdapter);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFriends.setAdapter(friendAdapter);
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvGroups.setAdapter(simpleGroupAdapter);
        rvGroups.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvAwards.setAdapter(awardsAdapter);
        rvAwards.setLayoutManager(new GridLayoutManager(getContext(), 4));

//        drawSettings();

        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitle(user.getUsername());
        setHasOptionsMenu(true);

        if (user.get("profileImage") != null) {
            Glide.with(view.getContext())
                    .load(((ParseFile) user.get("profileImage")).getUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivUserIcon);
        }
        onFriendUpdate();
        ivUserIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.hasSameId(ParseUser.getCurrentUser())) {
                    Toast.makeText(view.getContext(), "Is current user", Toast.LENGTH_SHORT).show();
                    mListener.createProfilePicture(view);
                } else {
                    final ParseUser currentUser = ParseUser.getCurrentUser();
                    final ParseRelation<ParseUser> relation = currentUser.getRelation("friends");
                    ParseQuery<ParseUser> query = relation.getQuery();
                    query.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> objects, ParseException e) {
                            boolean found = false;
                            for (int i = 0; i < objects.size(); i++) {
                                if (objects.get(i).hasSameId(user)) {
                                    found = true;
                                }
                            }
                            if (found) {
                                relation.remove(user);
                                ParseQuery<Invitation> invitationParseQuery = new ParseQuery<Invitation>("Invitation");
                                invitationParseQuery.whereEqualTo("inviter", ParseUser.getCurrentUser());
                                invitationParseQuery.whereEqualTo("receiver", user);
                                invitationParseQuery.getFirstInBackground(new GetCallback<Invitation>() {
                                    @Override
                                    public void done(Invitation object, ParseException e) {
                                        if (object != null) {
                                            object.setAccepted("rejected");
                                            object.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e != null) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });

                                        } else {
                                            ParseQuery<Invitation> invitationParseQuery = new ParseQuery<Invitation>("Invitation");
                                            invitationParseQuery.whereEqualTo("inviter", user);
                                            invitationParseQuery.whereEqualTo("receiver", ParseUser.getCurrentUser());
                                            invitationParseQuery.getFirstInBackground(new GetCallback<Invitation>() {
                                                @Override
                                                public void done(Invitation object, ParseException e) {
                                                    if (object != null) {
                                                        object.setAccepted("rejected");
                                                        object.saveInBackground(new SaveCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                if (e != null) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            } else {
                                ParseQuery<Invitation> invitationParseQuery = new ParseQuery<Invitation>("Invitation");
                                invitationParseQuery.whereEqualTo("inviter", ParseUser.getCurrentUser());
                                invitationParseQuery.whereEqualTo("receiver", user);
                                invitationParseQuery.getFirstInBackground(new GetCallback<Invitation>() {
                                    @Override
                                    public void done(Invitation object, ParseException e) {
                                        if (object == null) {
                                            Invitation invitation = new Invitation();
                                            invitation.setInviter(ParseUser.getCurrentUser());
                                            invitation.setReceiver(user);
                                            invitation.setAccepted("sent");
                                            invitation.saveInBackground();
                                            MyFirebaseMessagingService mfms = new MyFirebaseMessagingService();
                                            mfms.logToken(getContext());
                                            Messaging.sendNotification((String) user.get("deviceId"), ParseUser.getCurrentUser().getUsername() + " just sent you a friend request!");
                                            ParseQuery<ParseObject> query = ParseQuery.getQuery("Award");
                                            query.getInBackground(getString(R.string.friendship_goals_award), new GetCallback<ParseObject>() {
                                                public void done(ParseObject object, ParseException e) {
                                                    if (e == null) {
                                                        friendshipGoals = (Award) object;
                                                        af.queryAward(friendshipGoals, false, true, getContext());
                                                    } else {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });

                            }
                            currentUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                onFriendUpdate();
                                }
                            });
                        }
                    });
                }
            }
        });

        ivServicePoints.setColorFilter(getResources().getColor(R.color.o9));
        ivGetTogetherPoints.setColorFilter(getResources().getColor(R.color.o4));
        ivFitnessPoints.setColorFilter(getResources().getColor(R.color.orange));
        tvServicePoints.setText(Integer.toString(user.getInt("servicePoints")));
        tvGetTogetherPoints.setText(Integer.toString(user.getInt("getTogetherPoints")));
        tvFitnessPoints.setText(Integer.toString(user.getInt("fitnessPoints")));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        ((AppCompatActivity) getActivity()).getMenuInflater().inflate(R.menu.profile_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_prof_logout:
                ParseUser.logOut();
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
                break;
            case R.id.action_prof_pending:
                Intent i = new Intent(getContext(), InvitationActivity.class);
                startActivityForResult(i, INVITATION_REQUEST_CODE);
        }

        Toast.makeText(getContext(), "itemId: " + item.toString(), Toast.LENGTH_LONG).show();
        Log.d("itemId", item.toString());
        return true;
    }

    public void onFriendUpdate() {
        final ParseRelation relation = ParseUser.getCurrentUser().getRelation("friends");
        ParseQuery<Invitation> invitationParseQuery = new ParseQuery<Invitation>("Invitation");
        invitationParseQuery.findInBackground(new FindCallback<Invitation>() {
            @Override
            public void done(List<Invitation> objects, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                } else {
                    for (int i = 0; i < objects.size(); i++) {
                        if (objects.get(i) != null) {
                            if (objects.get(i).getGroup() == null) {
                                if (objects.get(i).getInviter().hasSameId(ParseUser.getCurrentUser())) {
                                    if (objects.get(i).getAccepted() != null && objects.get(i).getAccepted().equals("rejected")) {
                                        relation.remove(objects.get(i).getReceiver());
                                    } else {
                                        relation.add(objects.get(i).getReceiver());
                                    }
                                } else if (objects.get(i).getReceiver() != null && objects.get(i).getReceiver().hasSameId(ParseUser.getCurrentUser())) {
                                    if (objects.get(i).getAccepted().equals("rejected")) {
                                        relation.remove(objects.get(i).getInviter());
                                    } else {
                                        relation.add(objects.get(i).getInviter());
                                    }
                                }
                            }
                        }
                    }
                    ParseUser.getCurrentUser().saveInBackground();
                }
            }
        });
        ParseQuery<ParseUser> query = relation.getQuery();
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (objects == null) return;
                for (int i = 0; i < objects.size(); i++) {
                    if (objects.get(i).getObjectId().equals(user.getObjectId())) {
                        tvUsername.setText(String.format("☺️%s", user.getUsername()));
                        return;
                    }
                }
                tvUsername.setText(user.getUsername());
                ivUserIcon.setBackground(null);
            }
        });
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
                tvNumPosts.setText(Integer.toString(posts.size()));
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
                tvNumGroups.setText(Integer.toString(groups.size()));
                simpleGroupAdapter.notifyDataSetChanged();
            }
        });

    }

    public void queryFriends() {
        final ParseRelation relation = user.getRelation("friends");
        ParseQuery<ParseObject> query = relation.getQuery();
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
                tvNumFriends.setText(Integer.toString(friends.size()));
                friendAdapter.notifyDataSetChanged();
            }
        });
    }

    public void queryAwards() {
        ParseQuery<Award> parseQuery = new ParseQuery<>("Award");
        parseQuery.include("awards");
        parseQuery.addAscendingOrder("createdAt");
        parseQuery.setLimit(25);
        parseQuery.include("user");

        parseQuery.findInBackground(new FindCallback<Award>() {
            @Override
            public void done(final List<Award> objects, ParseException e) {
                if (e != null) {
                    Log.e("Querying awards", "error with query");
                    e.printStackTrace();
                    return;
                }
                // add new awards to the list and notify adapter
                awards.addAll((List<Award>) (Object) objects);

                ParseQuery<UserAward> query = new ParseQuery<>(UserAward.class);
                query.include("award");
                query.whereEqualTo("user", user);
                query.findInBackground(new FindCallback<UserAward>() {
                    @Override
                    public void done(List<UserAward> objects, ParseException e) {
                        if (e != null) {
                            Log.e("Querying groups", "error with query");
                            e.printStackTrace();
                            return;
                        }
                        if (objects != null) {
                            for (int i = 0; i < objects.size(); i++) {
                                if (objects.get(i).getIfAchieved()) {
                                    achievedAwards.add(objects.get(i).getAward());
                                }
                            }
                            awardsAdapter.notifyDataSetChanged();
                            tvNumAwards.setText(Integer.toString(achievedAwards.size()));
                        }
                    }
                });
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
