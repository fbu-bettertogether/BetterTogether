package com.example.bettertogether;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.example.bettertogether.models.Post;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class TagActivity extends AppCompatActivity {
    private RecyclerView rvMembers;
    private MemberAdapter adapter;
    private Toolbar toolbar;

    private Group group;
    private ArrayList<ParseUser> taggedUsers;
    private ArrayList<ParseUser> groupMembers;
    private ArrayList<String> objIds;
    private ArrayList<ParseUser> alreadyTagged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);
        toolbar = findViewById(R.id.toolbar);
        rvMembers = findViewById(R.id.rvMembers);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        group = getIntent().getParcelableExtra("group");
        taggedUsers = new ArrayList<>();
        groupMembers = new ArrayList<>();
        alreadyTagged = getIntent().getParcelableArrayListExtra("alreadyTagged");
        objIds = new ArrayList<>();
        for (ParseUser user : alreadyTagged) {
            objIds.add(user.getObjectId());
        }
        adapter = new MemberAdapter(groupMembers, taggedUsers);
        rvMembers.setLayoutManager(new LinearLayoutManager(rvMembers.getContext()));
        rvMembers.setAdapter(adapter);

        ParseQuery<Membership> membershipParseQuery = new ParseQuery<>(Membership.class);
        membershipParseQuery.whereEqualTo("group", group);
        membershipParseQuery.include("user");
        membershipParseQuery.findInBackground(new FindCallback<Membership>() {
            @Override
            public void done(List<Membership> objects, ParseException e) {
                if (e != null) {
                    Log.e("Querying groups", "error with query");
                    e.printStackTrace();
                }
                ArrayList<ParseUser> untaggedFriends = new ArrayList<>();
                for (ParseUser curr: Membership.getAllUsers(objects)) {
                    if (alreadyTagged == null || alreadyTagged.size() == 0 || !objIds.contains(curr.getObjectId())) {
                        untaggedFriends.add(curr);
                    }
                }
                groupMembers.addAll(untaggedFriends);
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
                intent.putParcelableArrayListExtra("taggedUsers", taggedUsers);
                setResult(RESULT_OK, intent); // set result code and bundle data for response
                finish(); // closes the activity, pass data to parent
                return true;
        }
    }

}
