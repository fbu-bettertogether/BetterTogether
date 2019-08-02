package com.example.bettertogether.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bettertogether.PostsAdapter;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.example.bettertogether.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.parse.ParseUser.getCurrentUser;

public class HomeFragment extends Fragment {

    private RecyclerView rvPosts;
    private SwipeRefreshLayout swipeContainerPosts;
    private PostsAdapter adapter;
    private List<Post> mPosts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rvPosts = view.findViewById(R.id.rvPosts);
        swipeContainerPosts = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainerPosts);

        // initializing list of posts, adapter, and attaching adapter to recyclerview
        mPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), mPosts, getFragmentManager());
        rvPosts.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvPosts.setLayoutManager(linearLayoutManager);

        // Lookup the swipe container view
        // Setup refresh listener which triggers new data loading
        swipeContainerPosts.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false) once the refresh completed successfully.
                fetchPostsAgain(0);
            }
        });
        // Configure the refreshing colors
        swipeContainerPosts.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        queryPosts();
    }

    public void fetchPostsAgain(int page) {
        adapter.clear();
        queryPosts();
        // Now we call setRefreshing(false) to signal refresh has finished
        swipeContainerPosts.setRefreshing(false);
    }

    private void queryPosts() {
        ParseQuery<Membership> parseQuery = new ParseQuery<Membership>(Membership.class);
        parseQuery.addDescendingOrder("createdAt");
        parseQuery.whereEqualTo("user", getCurrentUser());
        parseQuery.include("group");
        parseQuery.findInBackground(new FindCallback<Membership>() {
            @Override
            public void done(List<Membership> objects, ParseException e) {
                if (e != null) {
                    Log.e("Querying groups", "error with query");
                    e.printStackTrace();
                    return;
                }

                final List<Group> groups = Membership.getAllGroups(objects);

//                for (int i = 0; i < groups.size(); i++) {
//                    ParseQuery<Post> postQuery = new ParseQuery<Post>(Post.class);
//                    postQuery.whereEqualTo("group", groups.get(i));
//                    postQuery.setLimit(3);
//                    postQuery.addDescendingOrder("createdAt");
//                    postQuery.include("user");
//
//                    postQuery.findInBackground(new FindCallback<Post>() {
//                        @Override
//                        public void done(List<Post> posts, ParseException e) {
//                            if (e != null) {
//                                Log.e("querying posts", "error with query");
//                                e.printStackTrace();
//                                return;
//                            }
//
//                            // add new posts to the list and notify adapter
//                            mPosts.addAll(posts);
//                            Collections.sort(mPosts, new Comparator<Post>() {
//                                @Override
//                                public int compare(Post post1, Post post2) {
//                                    return post2.getCreatedAt().compareTo(post1.getCreatedAt());
//                                }
//                            });
//
//                            adapter.notifyDataSetChanged();
//                        }
//                    });
//                }
                ParseQuery friendQuery = null;
                try {
                    friendQuery = ((ParseRelation) ParseUser.getCurrentUser().fetchIfNeeded().get("friends")).getQuery();
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
                friendQuery.findInBackground(new FindCallback() {
                    @Override
                    public void done(List objects, ParseException e) {

                    }

                    @Override
                    public void done(Object o, Throwable throwable) {
                        if (throwable != null) {
                            throwable.printStackTrace();
                        } else {
                            queryFriendPosts(groups, (List<ParseUser>) o);
                        }
                    }
                });

            }
        });
    }

    private void queryFriendPosts(final List<Group> groups, final List<ParseUser> friends) {
        ParseQuery<Post> postQuery = new ParseQuery<Post>(Post.class);
        postQuery.include("user");
        postQuery.include("group");
        postQuery.setLimit(100);
        postQuery.addDescendingOrder("createdAt");
        postQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                postLoop:
                for (Post post : objects) {
                    for (Group group : groups) {
                        if (post.getGroup().hasSameId(group)) {
                            mPosts.add(post);
                            continue postLoop;
                        }
                    }
                    for (ParseUser friend : friends) {
                        if (post.getUser().hasSameId(friend)) {
                            mPosts.add(post);
                            continue postLoop;
                        }
                    }
                }
                Collections.sort(mPosts, new Comparator<Post>() {
                    @Override
                    public int compare(Post post1, Post post2) {
                        return post2.getCreatedAt().compareTo(post1.getCreatedAt());
                    }
                });

                adapter.notifyDataSetChanged();

            }
        });
    }
}
