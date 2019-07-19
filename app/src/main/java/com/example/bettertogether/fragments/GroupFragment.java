package com.example.bettertogether.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.bettertogether.CreatePostActivity;
import com.example.bettertogether.PostsAdapter;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.example.bettertogether.models.Post;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.parse.ParseUser.getCurrentUser;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnGroupFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupFragment extends Fragment {
    private static final String ARG_GROUP = "group";
    private Group group;
    private OnGroupFragmentInteractionListener mListener;

    private ImageView ivBanner;
    private ImageView ivUserIcon;
    private TextView tvGroupName;
    private TextView tvCreatePost;
    private Button btnCheckIn;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private TextView tvTimer;
    private int counter;
    private int numCheckIns;
    private Membership currMem;

    private RecyclerView rvTimeline;
    private PostsAdapter adapter;
    private List<Post> mPosts;


    public GroupFragment() {
        // Required empty public constructor
    }

    public static GroupFragment newInstance(Group group) {
        GroupFragment fragment = new GroupFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_GROUP, group);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.group = (Group) getArguments().getParcelable(ARG_GROUP);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_detail, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivBanner = view.findViewById(R.id.ivBanner);
        ivUserIcon = view.findViewById(R.id.ivUserIcon);
        tvGroupName = view.findViewById(R.id.tvGroupName);
        tvCreatePost = view.findViewById(R.id.tvCreatePost);
        btnCheckIn = view.findViewById(R.id.btnCheckIn);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        tvTimer = view.findViewById(R.id.tvTimer);

        // setting up recycler view of posts
        rvTimeline = view.findViewById(R.id.rvTimeline);
        mPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), mPosts);
        rvTimeline.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvTimeline.setLayoutManager(linearLayoutManager);

        if (group.getBanner() != null) {
            Glide.with(view.getContext()).load(group.getBanner().getUrl()).into(ivBanner);
        }
        if (ParseUser.getCurrentUser().getParseFile("profileImage") != null) {
            Glide.with(view.getContext()).load(ParseUser.getCurrentUser().getParseFile("profileImage").getUrl()).apply(RequestOptions.circleCropTransform()).into(ivUserIcon);
        }

        tvGroupName.setText(group.getName());
        tvStartDate.setText(group.getStartDate());
        tvEndDate.setText(group.getEndDate());

        if(group.getIsActive()) {
            tvStartDate.setTextColor(ContextCompat.getColor(getContext(), R.color.teal));
            tvEndDate.setTextColor(ContextCompat.getColor(getContext(), R.color.teal));
        } else {
            tvStartDate.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            tvEndDate.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        }

        ParseQuery<Membership> parseQuery = new ParseQuery<Membership>(Membership.class);
        parseQuery.whereEqualTo("user", getCurrentUser());
        parseQuery.whereEqualTo("group", group);
        parseQuery.include("numCheckIns");
        parseQuery.findInBackground(new FindCallback<Membership>() {
            @Override
            public void done(List<Membership> objects, ParseException e) {
                currMem = objects.get(0);
                numCheckIns = currMem.getNumCheckIns();

                if (numCheckIns < Integer.valueOf(group.getFrequency())) {

                    btnCheckIn.setVisibility(View.VISIBLE);
                    btnCheckIn.setText(String.format("%d check-ins left: check in now!", Integer.valueOf(group.getFrequency()) - numCheckIns));
                    btnCheckIn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d("Check in button", "Success");
                            btnCheckIn.setVisibility(View.INVISIBLE);
                            tvTimer.setVisibility(View.VISIBLE);
                            final long timeInMillis = TimeUnit.MINUTES.toMillis(group.getInt("minTime"));

                            new CountDownTimer(timeInMillis, 1000) {

                                @Override
                                public void onTick(long l) {
                                    long timeInS = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) - counter;
                                    tvTimer.setText(String.format("%02d : %02d", TimeUnit.SECONDS.toMinutes(timeInS), timeInS - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(timeInS))));
                                    counter++;
                                }

                                @Override
                                public void onFinish() {
                                    tvTimer.setText("Finished!");
                                    currMem.setNumCheckIns(numCheckIns + 1);
                                    currMem.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            Log.d("checking in", "saved check in");
                                        }
                                    });
                                }
                            }.start();
                        }

                    });

                } else {
                    btnCheckIn.setVisibility(View.INVISIBLE);
                    tvTimer.setVisibility(View.VISIBLE);
                    tvTimer.setText("You're done for the week!");
                }
            }
        });


        tvCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "text view clicked", Toast.LENGTH_LONG).show();
                Intent i = new Intent(getContext(), CreatePostActivity.class);
                i.putExtra("group", Parcels.wrap(group));
                startActivity(i);
            }
        });

        queryPosts();
    }

    private void queryPosts() {
        ParseQuery<ParseObject> parseQuery = group.getRelation("posts").getQuery();
        parseQuery.addDescendingOrder("createdAt");
        parseQuery.include("user");
        parseQuery.setLimit(25);
        parseQuery.addDescendingOrder("createdAt");

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> posts, ParseException e) {
                if (e != null) {
                    Log.e("Querying posts", "error with query");
                    e.printStackTrace();
                    return;
                }

                // add new posts to the list and notify adapter
                mPosts.addAll((List<Post>) (Object) posts);
                adapter.notifyDataSetChanged();
            }
        });
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGroupFragmentInteractionListener) {
            mListener = (OnGroupFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnGroupFragmentInteractionListener {
        void onGroupFragmentInteraction();
    }
}
