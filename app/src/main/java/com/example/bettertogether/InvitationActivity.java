package com.example.bettertogether;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Invitation;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class InvitationActivity extends AppCompatActivity {
    private RecyclerView rvMembers;
    private InvitationAdapter adapter;
    private Toolbar toolbar;

    private Group group;
    private ArrayList<Invitation> taggedInvitiations;
    private ArrayList<Invitation> invitiations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);
        toolbar = findViewById(R.id.toolbar);
        rvMembers = findViewById(R.id.rvMembers);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        taggedInvitiations = new ArrayList<>();
        invitiations = new ArrayList<>();
        adapter = new InvitationAdapter(invitiations, taggedInvitiations);
        rvMembers.setLayoutManager(new LinearLayoutManager(rvMembers.getContext()));
        rvMembers.setAdapter(adapter);

        ParseQuery<Invitation> invitationParseQuery = new ParseQuery<>(Invitation.class);
        invitationParseQuery.whereEqualTo("receiver", ParseUser.getCurrentUser());
        if (getIntent().getParcelableExtra("group") != null) {
            group = getIntent().getParcelableExtra("group");
            invitationParseQuery.whereEqualTo("group", group);
            invitationParseQuery.include("group");
        } else {
            invitationParseQuery.include("inviter");
        }
        invitationParseQuery.whereNotEqualTo("accepted", "accepted");
        invitationParseQuery.findInBackground(new FindCallback<Invitation>() {
            @Override
            public void done(List<Invitation> objects, ParseException e) {
                if (e != null) {
                    Log.e("Querying invitation", "error with query");
                    e.printStackTrace();
                }
                invitiations.addAll(objects);
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
                intent.putParcelableArrayListExtra("taggedInvitations", taggedInvitiations);
                setResult(RESULT_OK, intent); // set result code and bundle data for response
                finish(); // closes the activity, pass data to parent
                return true;
        }
    }

}
