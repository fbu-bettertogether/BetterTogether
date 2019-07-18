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

import com.example.bettertogether.PostsAdapter;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import static com.parse.ParseUser.getCurrentUser;

public class HomeFragment extends Fragment {

    private RecyclerView rvPosts;
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

        // initializing list of posts, adapter, and attaching adapter to recyclerview
        mPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), mPosts);
        rvPosts.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvPosts.setLayoutManager(linearLayoutManager);

        queryPosts();
    }

    private void queryPosts() {
        getCurrentUser().getRelation("groups").getQuery().findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> groups, ParseException e) {
                if (e != null) {
                    Log.e("Querying groups", "error with query");
                    e.printStackTrace();
                    return;
                }

                for (int i = 0; i < groups.size() && i < 3; i++) {
                    ParseQuery<Post> postQuery = new ParseQuery<Post>(Post.class);
                    postQuery.whereEqualTo("group", groups.get(i));
                    postQuery.setLimit(3);
                    postQuery.addDescendingOrder("createdAt");
                    postQuery.include("user");

                    postQuery.findInBackground(new FindCallback<Post>() {
                        @Override
                        public void done(List<Post> posts, ParseException e) {
                            if (e != null) {
                                Log.e("querying posts", "error with query");
                                e.printStackTrace();
                                return;
                            }

                            // add new posts to the list and notify adapter
                            mPosts.addAll(posts);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }
}
