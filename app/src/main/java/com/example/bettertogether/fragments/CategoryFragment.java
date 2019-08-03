package com.example.bettertogether.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bettertogether.CategoryAdapter;
import com.example.bettertogether.DiscoveryAdapter;
import com.example.bettertogether.EndlessRecyclerViewScrollListener;
import com.example.bettertogether.MainActivity;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Post;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CategoryFragment extends Fragment {

    protected RecyclerView rvGroups;
    protected CategoryAdapter categoryAdapter;
    ParseUser user;
    protected List<Group> groups = new ArrayList<>();
    protected SwipeRefreshLayout swipeContainer;
    MenuItem miActionProgressItem;
    private final int REQUEST_CODE = 20;
    protected EndlessRecyclerViewScrollListener scrollListener;
    //keeps track of the maxDate (used to keep track of post order) for the infinite scrolling
    public Date maxDate = new Date();
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rvPosts = view.findViewById(R.id.rvPosts);

        user = ParseUser.getCurrentUser();

        //find the RecyclerView
        rvPosts = (RecyclerView) view.findViewById(R.id.rvPosts);
        //init the arraylist (data source)
        posts = new ArrayList<>();
        //construct the adapter from this data source
        postAdapter = new PostAdapter(posts);
        //RecyclerView setup (layout manager, use adapter)
        //set the adapter
        rvPosts.setAdapter(postAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        rvPosts.setLayoutManager(gridLayoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore ( int page, int totalItemsCount, RecyclerView view){
                fetchTimelineAsync(page, false);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvPosts.addOnScrollListener(scrollListener);

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                showProgressBar();
                fetchTimelineAsync(0, true);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        populateTimeline();
    }

    protected void loadTopPosts() {
        final Post.Query postQuery = new Post.Query();
        postQuery.whereLessThan(Post.KEY_CREATED_AT, maxDate);
        postQuery.getTop().withUser();
        postQuery.addDescendingOrder(Post.KEY_CREATED_AT);
        postQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); ++i) {
                        Log.d("HomeActivity", "Post[" + i + "] = "
                                + objects.get(i).getDescription()
                                + "\nusername = " + objects.get(i).getUser().getUsername());
                    }
                    posts.addAll(objects);
                    postAdapter.notifyItemInserted(0);
                    //rvPosts.scrollToPosition(0);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
    }

    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        if (miActionProgressItem == null) {
            return;
        }
        miActionProgressItem.setVisible(false);
    }

    protected void populateTimeline() {
        loadTopPosts();
        categoryAdapter.notifyItemInserted(groups.size() - 1);
        hideProgressBar();
    }

    public void fetchTimelineAsync(int page, boolean isRefreshed) {
        if (!isRefreshed) {
            maxDate = posts.get(posts.size() - 1).getCreatedAt();
        } else {
            postAdapter.clear();
            maxDate = new Date();
        }
        // ...the data has come back, add new items to your adapter...
        populateTimeline();
        // Now we call setRefreshing(false) to signal refresh has finished
        swipeContainer.setRefreshing(false);
    }
}