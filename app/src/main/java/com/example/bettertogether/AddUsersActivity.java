package com.example.bettertogether;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.bettertogether.models.Group;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class AddUsersActivity extends AppCompatActivity {

    private RecyclerView rvMembers;
    private AddUsersAdapter adapter;
    private Toolbar toolbar;
    private ArrayList<ParseUser> alreadyAdded;
    private ArrayList<String> objIds;
    private ArrayList<ParseUser> addedMembers;
    private ArrayList<ParseUser> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);
        toolbar = findViewById(R.id.toolbar);
        rvMembers = findViewById(R.id.rvMembers);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        alreadyAdded = getIntent().getParcelableArrayListExtra("alreadyAdded");
        objIds = new ArrayList<>();
        for (ParseUser user: alreadyAdded) {
            objIds.add(user.getObjectId());
        }
        addedMembers = new ArrayList<>();
        //Current user should only be added to the "addedMembers" list once (in case the person goes back to add other users).
        if (!objIds.contains(ParseUser.getCurrentUser().getObjectId())) {
            addedMembers.add(ParseUser.getCurrentUser());
        }
        users = new ArrayList<>();
        adapter = new AddUsersAdapter(users, addedMembers);
        rvMembers.setAdapter(adapter);
        rvMembers.setLayoutManager(new LinearLayoutManager(rvMembers.getContext()));

        //Only the current user's friends on the app are added to the list of people they can add/invite to the group.
        final ParseRelation<ParseUser> relation = ParseUser.getCurrentUser().getRelation("friends");
        ParseQuery<ParseUser> query = relation.getQuery();
        query.include("friends");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                if (e != null) {
                    Log.e("Querying friends", "error with query");
                    e.printStackTrace();
                    return;
                }
                //Only friends that have not been added to the current group would be available to be added (avoids repeatedly adding the same friend(s) to the group).
                ArrayList<ParseUser> unaddedFriends = new ArrayList<>();
                for (ParseUser curr: friends) {
                    if (alreadyAdded == null || alreadyAdded.size() == 0 || !objIds.contains(curr.getObjectId())) {
                        unaddedFriends.add(curr);
                    }
                }
                users.addAll(unaddedFriends);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.compose_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuCompose:
            default:
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra("addedMembers", addedMembers);
                setResult(RESULT_OK, intent); // set result code and bundle data for response
                finish(); // closes the activity, pass data to parent
                return true;
        }
    }
}
