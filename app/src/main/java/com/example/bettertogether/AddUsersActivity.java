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
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class AddUsersActivity extends AppCompatActivity {

    private RecyclerView rvMembers;
    private AddUsersAdapter adapter;
    private Toolbar toolbar;

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

        addedMembers = new ArrayList<>();
        addedMembers.add(ParseUser.getCurrentUser());
        users = new ArrayList<>();
        adapter = new AddUsersAdapter(users, addedMembers);
        rvMembers.setAdapter(adapter);
        rvMembers.setLayoutManager(new LinearLayoutManager(rvMembers.getContext()));

        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e != null) {
                    Log.e("Querying groups", "error with query");
                    e.printStackTrace();
                }
                users.addAll(objects);
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
