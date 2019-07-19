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

import com.example.bettertogether.GroupsAdapter;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import static com.parse.ParseUser.getCurrentUser;

public class GroupsFragment extends Fragment {

    private RecyclerView rvGroups;
    private GroupsAdapter adapter;
    private List<Group> mGroups;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_groups, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rvGroups = view.findViewById(R.id.rvGroups);

        // initialize the data source
        mGroups = new ArrayList<>();
        //mGroups.addAll((List<Group>) (Object) getCurrentUser().get("groups"));
        // initialize the adapter
        adapter = new GroupsAdapter(getContext(), mGroups);
        // set the adapter on the recycler view
        rvGroups.setAdapter(adapter);
        // set the layout manager on the recyler view
        rvGroups.setLayoutManager(new LinearLayoutManager(getContext()));

        queryGroups();
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

                Log.d("carmel",Integer.toString(memberships.size()));

                mGroups.addAll(Membership.getAllGroups(memberships));
                adapter.notifyDataSetChanged();
            }
        });
    }
}
