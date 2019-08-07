package com.example.bettertogether.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bettertogether.DiscoveryAdapter;
import com.example.bettertogether.MakeNewGroupActivity;
import com.example.bettertogether.R;
import com.example.bettertogether.ResultsAdapter;
import com.example.bettertogether.models.CatMembership;
import com.example.bettertogether.models.Category;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryFragment extends Fragment {
    public static final String LOGTAG ="carousels";

    protected RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    boolean scrolling;
    protected DiscoveryAdapter mainRecyclerAdapter;
    private List<Category> mCategories = new ArrayList<>();
    private ProgressBar progressBar = null;
    private Button createGroupBtn;
    private Button btnLeaderboard;
    private Button btnShowMore;
    private List<List<Group>> listOfListOfItems = new ArrayList<List<Group>>();
    private final int REQUEST_CODE = 20;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_discovery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView)view.findViewById(R.id.recyclerview_rootview);
        createGroupBtn = (Button) view.findViewById(R.id.create_group_btn);
        btnLeaderboard = view.findViewById(R.id.btnLeaderboard);
        btnShowMore = (Button)view.findViewById(R.id.show_more_btn);
        mLayoutManager = new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        ParseQuery<Category> query = ParseQuery.getQuery(Category.class);
        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        queryCategories();
        btnLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.flContainer, new LeaderboardFragment()).commit();
            }
        });
    }

    private void queryCategories() {
        final Category.Query catQuery = new Category.Query();
        catQuery.addDescendingOrder("createdAt");
        catQuery.findInBackground(new FindCallback<Category>() {
            @Override
            public void done(List<Category> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); ++i) {
                        Log.d("DiscoveryActivity", "Category[" + i + "] = "
                                + objects.get(i).getName());
                    }
                    mCategories.addAll(objects);
                    //mainRecyclerAdapter.notifyDataSetChanged();
                    if (mCategories != null && mCategories.size() != 0) {
                        for (int i = 0; i < mCategories.size(); i++) {
                            queryGroups(mCategories.get(i));
                        }
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void queryGroups(Category cat) {
        ParseQuery<CatMembership> query = new ParseQuery<CatMembership>(CatMembership.class);
        query.include("group");
        query.addDescendingOrder("createdAt");
        query.whereEqualTo("category", cat);
        query.setLimit(10);
        query.findInBackground(new FindCallback<CatMembership>() {
            @Override
            public void done(List<CatMembership> objects, ParseException e) {
                if (e != null) {
                    Log.e("Querying groups", "error with query");
                    e.printStackTrace();
                    return;
                }
                Log.d("number of groups",Integer.toString(objects.size()));
                // add posts to list and update view with adapter
                List<Group> mGroups = new ArrayList<>();
                mGroups.addAll(CatMembership.getAllGroups(objects));
                //mainRecyclerAdapter.notifyDataSetChanged();
                listOfListOfItems.add(mGroups);
                display();

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Inflate the options menu from XML
        inflater.inflate(R.menu.search_view, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                // perform query here

                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                if (query.isEmpty()) {
                    mRecyclerView.setAdapter(mainRecyclerAdapter);
                    return false;
                }
                searchView.clearFocus();
                ParseQuery<ParseUser> userParseQuery = new ParseQuery<ParseUser>(ParseUser.class);
                userParseQuery.whereMatches("username", query, "i");
                userParseQuery.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(final List<ParseUser> users, ParseException e) {
                        ParseQuery<Group> userParseQuery = new ParseQuery<Group>("Group");
                        userParseQuery.whereMatches("name", query, "i");
                        userParseQuery.findInBackground(new FindCallback<Group>() {
                            @Override
                            public void done(List<Group> groups, ParseException e) {
                                mRecyclerView.setAdapter(new ResultsAdapter(getContext(), groups, users));
                            }
                        });

                    }
                });

                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                if (newText.isEmpty()) {
                    mRecyclerView.setAdapter(mainRecyclerAdapter);
                    return false;
                }
                ParseQuery<ParseUser> userParseQuery = new ParseQuery<ParseUser>(ParseUser.class);
                userParseQuery.whereMatches("username", newText, "i");
                userParseQuery.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(final List<ParseUser> users, ParseException e) {
                        ParseQuery<Group> userParseQuery = new ParseQuery<Group>("Group");
                        userParseQuery.whereMatches("name", newText, "i");
                        userParseQuery.findInBackground(new FindCallback<Group>() {
                            @Override
                            public void done(List<Group> groups, ParseException e) {
                                mRecyclerView.setAdapter(new ResultsAdapter(getContext(), groups, users));
                            }
                        });

                    }
                });

                return false;
            }
        });

    }



    private void display() {
        mainRecyclerAdapter = new DiscoveryAdapter(listOfListOfItems, getContext());
        mRecyclerView.setAdapter(mainRecyclerAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged (RecyclerView recyclerView, int newState){

                switch(newState){
                    case RecyclerView.SCROLL_STATE_IDLE:

                        Log.i(LOGTAG,"X = " + (mRecyclerView.getX() + mRecyclerView.getWidth() )+ " and Y = " + (mRecyclerView.getY()+ mRecyclerView.getHeight()));
                        float targetBottomPosition1 = mRecyclerView.getY();
                        float targetBottomPosition2 = mRecyclerView.getY() + mRecyclerView.getHeight();

                        Log.i(LOGTAG,"targetBottomPosition1 = " + targetBottomPosition1);
                        Log.i(LOGTAG,"targetBottomPosition2 = " + targetBottomPosition2);

                        View v1 = mRecyclerView.findChildViewUnder(500, targetBottomPosition1);
                        View v2 = mRecyclerView.findChildViewUnder(500, targetBottomPosition2);

                        float y1 = targetBottomPosition1;
                        if(v1!=null){
                            y1 =v1.getY();
                        }
                        float y2 = targetBottomPosition2;
                        if(v2!=null){
                            y2 =v2.getY();
                        }
                        Log.i(LOGTAG,"y1 = " + y1);
                        Log.i(LOGTAG,"y2 = " + y2);

                        float dy1 = Math.abs(y1-mRecyclerView.getY() );
                        float dy2 = Math.abs(y2-(mRecyclerView.getY()+ mRecyclerView.getHeight()));
                        Log.i(LOGTAG,"dy1 = " + dy1);
                        Log.i(LOGTAG,"dy2 = " + dy2);

                        float visiblePortionOfItem1 = 0;
                        float visiblePortionOfItem2 = 0;

                        if(y1<0 && v1 != null){
                            visiblePortionOfItem1 = v1.getHeight() - dy1;
                        }

                        if(v2 != null){
                            visiblePortionOfItem2 = v2.getHeight() - dy2;
                        }

                        int position = 0;
                        if(visiblePortionOfItem1<=visiblePortionOfItem2){
                            position = mRecyclerView.getChildAdapterPosition(mRecyclerView.findChildViewUnder(500, targetBottomPosition1));
                        }else{

                            position = mRecyclerView.getChildAdapterPosition(mRecyclerView.findChildViewUnder(500, targetBottomPosition2));
                        }
                        //mRecyclerView.scrollToPosition(position);
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        break;
                }
            }
            @Override
            public void onScrolled (RecyclerView recyclerView, int dx, int dy){
//				Log.i(LOGTAG,"X = " + dx + " and Y = " + dy);
            }
        });
        createGroupBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d("DiscoveryFragment", "Go to create new post page from discovery.");
                Intent i = new Intent(getContext(), MakeNewGroupActivity.class);
                startActivityForResult(i, REQUEST_CODE);
            }
        });
    }
}
