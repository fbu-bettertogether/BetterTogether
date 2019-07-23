package com.example.bettertogether.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
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
import com.example.bettertogether.HomeActivity;
import com.example.bettertogether.PostsAdapter;
import com.example.bettertogether.R;
import com.example.bettertogether.models.CatMembership;
import com.example.bettertogether.models.Category;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.example.bettertogether.models.Post;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;
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
    private static final int ACCESS_FINE_LOCATION_REQUEST_READ_CONTACTS = 36;
    public PlacesClient placesClient;
    public final String APP_TAG = "BetterTogether";
    private static final String ARG_GROUP = "group";
    private Group group;
    private OnGroupFragmentInteractionListener mListener;
    public static final int REQUEST_CODE = 45;

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
    private Category category;
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
        findCategory();
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
        adapter = new PostsAdapter(getContext(), mPosts, getFragmentManager());
        rvTimeline.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvTimeline.setLayoutManager(linearLayoutManager);

        if (group.getIcon() != null) {
            Glide.with(view.getContext()).load(group.getIcon().getUrl()).into(ivBanner);
        }
        if (ParseUser.getCurrentUser().getParseFile("profileImage") != null) {
            Glide.with(view.getContext()).load(ParseUser.getCurrentUser().getParseFile("profileImage").getUrl()).apply(RequestOptions.circleCropTransform()).into(ivUserIcon);
        }

        String startDateUgly = group.getStartDate();
        String endDateUgly = group.getEndDate();
        tvGroupName.setText(group.getName());
        tvStartDate.setText(startDateUgly.substring(0, 10).concat(", " + startDateUgly.substring(24)));
        tvEndDate.setText(endDateUgly.substring(0, 10).concat(", " + endDateUgly.substring(24)));

        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");
        Date start = null;
        Date end = null;
        try {
            start = sdf.parse(startDateUgly);
            end = sdf.parse(endDateUgly);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        Date now = Calendar.getInstance().getTime();
        if (now.after(end)) {
            group.setIsActive(false);
            Toast.makeText(getContext(), "Group is no longer active!", Toast.LENGTH_LONG).show();
        }

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
                try {
                    checkPlace(category.getLocationTypesList());
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });

        tvCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "text view clicked", Toast.LENGTH_LONG).show();
                Intent i = new Intent(getContext(), CreatePostActivity.class);
                i.putExtra("group", Parcels.wrap(group));
                startActivityForResult(i, REQUEST_CODE);
            }
        });

        queryPosts();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPosts.clear();
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
    public void checkPlace(final List<String> types)
    {
        final List<PlaceLikelihood> validPlaces = new ArrayList();
        boolean inValidPlace = false;
        // Initialize the SDK
        Places.initialize(getContext(), getString(R.string.places_key));

        // Create a new Places client instance
        placesClient = Places.createClient(getContext());
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_REQUEST_READ_CONTACTS);
        }
        // Use fields to define the data types to return.
        List<Place.Field> placeFields = Collections.singletonList(Place.Field.TYPES);

        // Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.newInstance(placeFields);

        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    // This task finds the places and their probabilities
                    if (task.isSuccessful()) {
                        FindCurrentPlaceResponse response = task.getResult();
                        for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                            for( Place.Type type : Objects.requireNonNull(placeLikelihood.getPlace().getTypes())) {
                                if (types.contains(type.toString())) {
                                    drawButton();
                                    return;
                                }
                            }
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            Log.e(APP_TAG, "Place not found: " + apiException.getStatusCode());
                        }
                    }
                }
            });
            btnCheckIn.setText("Get to a valid location");
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_REQUEST_READ_CONTACTS);
        }
    }

    public void drawButton() {
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
                            int addedPoints = currMem.getGroup().getFrequency() / 10;
                            currMem.setPoints(currMem.getPoints() + addedPoints);
                            // TODO -- get category & switch case which category to add points to for the user
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
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // locatinos-related task you need to do.
                } else {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            ACCESS_FINE_LOCATION_REQUEST_READ_CONTACTS);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void findCategory() {
        ParseQuery<CatMembership> query = new ParseQuery<CatMembership>(CatMembership.class);
        query.include("category");
        query.whereEqualTo("group", group);
        query.findInBackground(new FindCallback<CatMembership>() {
            @Override
            public void done(List<CatMembership> objects, ParseException e) {
                if (e != null) {
                    Log.e("Querying groups", "error with query");
                    e.printStackTrace();
                    return;
                }
                category = objects.get(0).getCategory();
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
