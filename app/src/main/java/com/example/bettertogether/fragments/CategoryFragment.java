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
import com.example.bettertogether.models.Membership;
import com.example.bettertogether.models.Post;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.parse.ParseUser.getCurrentUser;

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
        rvGroups = view.findViewById(R.id.rvGroupsInCategory);

        user = ParseUser.getCurrentUser();

        groups = new ArrayList<>();
        //construct the adapter from this data source
        categoryAdapter = new CategoryAdapter(getContext(), groups);
        //RecyclerView setup (layout manager, use adapter)
        //set the adapter
        rvGroups.setAdapter(categoryAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        rvGroups.setLayoutManager(gridLayoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore ( int page, int totalItemsCount, RecyclerView view){
                fetchTimelineAsync(page, false);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvGroups.addOnScrollListener(scrollListener);

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

    public void queryGroups() {
        ParseQuery<Membership> parseQuery = new ParseQuery<Membership>(Membership.class);
        parseQuery.addDescendingOrder("createdAt");
        parseQuery.whereEqualTo("user", getCurrentUser());
        parseQuery.include("group");
        parseQuery.findInBackground(new FindCallback<Membership>() {
            @Override
            public void done(List<Membership> memberships, ParseException e) {
                if (e != null) {
                    Log.e("Querying groups", "error with query");
                    e.printStackTrace();
                    return;
                }
                List<Group> activeGroups = new ArrayList<>();
                List<Group> inactiveGroups = new ArrayList<>();

                for (Group group : Membership.getAllGroups(memberships)) {
                    if (group.getIsActive()) {
                        activeGroups.add(group);
                    } else {
                        inactiveGroups.add(group);
                    }
                }
                groups.addAll(activeGroups);
                groups.addAll(inactiveGroups);
                categoryAdapter.notifyDataSetChanged();
            }
        });
    }

    protected void loadTopPosts() {
        final Group.Query groupQuery = new Group.Query();
        groupQuery.whereLessThan(Group.KEY_CREATED_AT, maxDate);
        postQuery.getTop().withUser();
        postQuery.addDescendingOrder(Group.KEY_CREATED_AT);
        postQuery.findInBackground(new FindCallback<Group>() {
            @Override
            public void done(List<Group> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); ++i) {
                        Log.d("CategoryFragment", "Group[" + i + "] = "
                                + objects.get(i).getDescription()
                                + "\nusername = " + objects.get(i).getOwner().getUsername());
                    }
                    groups.addAll(objects);
                    categoryAdapter.notifyItemInserted(0);
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
            maxDate = groups.get(groups.size() - 1).getCreatedAt();
        } else {
            categoryAdapter.clear();
            maxDate = new Date();
        }
        // ...the data has come back, add new items to your adapter...
        populateTimeline();
        // Now we call setRefreshing(false) to signal refresh has finished
        swipeContainer.setRefreshing(false);
    }
}