package com.example.bettertogether.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import com.bumptech.glide.request.RequestOptions;
import com.example.bettertogether.CreatePostActivity;
import com.example.bettertogether.FriendAdapter;
import com.example.bettertogether.PostsAdapter;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Award;
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
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static androidx.core.content.ContextCompat.getSystemService;
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
    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    public PlacesClient placesClient;
    public final String APP_TAG = "BetterTogether";
    private static final String ARG_GROUP = "group";
    private Group group;
    private OnGroupFragmentInteractionListener mListener;
    public static final int REQUEST_CODE = 45;
    private ParseGeoPoint location;
    private ImageView ivBanner;
    private ImageView ivUserIcon;
    private TextView tvGroupName;
    private Button btnCheckIn;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private TextView tvTimer;
    private int counter;
    private List<Integer> numCheckIns;
    private Membership currMem;
    private Category category;
    private RecyclerView rvTimeline;
    private PostsAdapter postsAdapter;
    private FriendAdapter friendAdapter;
    private List<Post> mPosts;
    private List<ParseUser> users;
    private Award first = new Award();
    private Award oneWeek = new Award();
    private Award tenacity = new Award();
    private AwardFragment af = new AwardFragment();


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
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            category = group.getParseObject("category").fetch();
        } catch (ParseException e) {
            e.printStackTrace();
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
        tvGroupName = view.findViewById(R.id.tvGroupName);
        btnCheckIn = view.findViewById(R.id.btnCheckIn);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        tvTimer = view.findViewById(R.id.tvTimer);
        // setting up recycler view of posts
        rvTimeline = view.findViewById(R.id.rvTimeline);
        mPosts = new ArrayList<>();
        postsAdapter = new PostsAdapter(getContext(), mPosts, getFragmentManager());
        rvTimeline.setAdapter(postsAdapter);
        rvTimeline.setLayoutManager(new LinearLayoutManager(getContext()));

        users = new ArrayList<>();
        friendAdapter = new FriendAdapter(users, getFragmentManager());

        if (group.getIcon() != null) {
            Glide.with(view.getContext()).load(group.getIcon().getUrl()).into(ivBanner);
        }


        String startDateUgly = group.getStartDate();
        String endDateUgly = group.getEndDate();
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
        final boolean nowBeforeStart = now.before(start);

        if (now.after(end)) {
            Toast.makeText(getContext(), "Group is no longer active!", Toast.LENGTH_LONG).show();
        } else if (nowBeforeStart) {
            btnCheckIn.setVisibility(View.INVISIBLE);
            tvTimer.setVisibility(View.VISIBLE);
            tvTimer.setText("Group has not started yet! Hang tight!");
        }

        if (group.getIsActive()) {
            tvStartDate.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
            tvEndDate.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        } else {
            tvStartDate.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            tvEndDate.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        }

        ParseQuery<Membership> parseQuery = new ParseQuery<Membership>(Membership.class);
        parseQuery.whereEqualTo("user", getCurrentUser());
        parseQuery.whereEqualTo("group", group);
        parseQuery.include("numCheckIns");
        parseQuery.include("group");
        parseQuery.findInBackground(new FindCallback<Membership>() {
            @Override
            public void done(final List<Membership> objects, ParseException e) {
                if (objects.size() > 0 & group.getIsActive()) {
                    currMem = objects.get(0);
                    numCheckIns = currMem.getNumCheckIns();
                    if (numCheckIns == null) {
                        numCheckIns = new ArrayList<>();
                        numCheckIns.add(0);
                    } else if (numCheckIns.size() == 0) {
                        numCheckIns.add(0);
                    }
                    if (category.getName().equals("Get-Togethers")) {
                        saveCurrentUserLocation();
                        checkProximity();
                     } else {
                        try {
                            checkPlace(category.getLocationTypesList());
                            drawButton();
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                } else {
                    if (!group.getPrivacy().equals("private") & nowBeforeStart) {
                        btnCheckIn.setText("Click to Join");
                        btnCheckIn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Membership membership = new Membership();
                                membership.setGroup(group);
                                membership.setUser(ParseUser.getCurrentUser());
                                membership.saveInBackground();
                                btnCheckIn.setOnClickListener(null);
                                currMem = membership;
                                numCheckIns = currMem.getNumCheckIns();
                                if (numCheckIns == null) {
                                    numCheckIns = new ArrayList<>();
                                    numCheckIns.add(0);
                                }
                                try {
                                    checkPlace(category.getLocationTypesList());
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }

                            }
                        });
                    } else {
                        btnCheckIn.setText("Group is Private");
                    }
                }
            }
        });
        queryMembers();
        queryPosts();
    }
    private void checkProximity() {
        ParseQuery<Membership> query = new ParseQuery<Membership>("Membership");
        query.whereWithinMiles("location", currMem.getParseGeoPoint("location"), 0.75);
        query.whereEqualTo("group", group);
        query.whereNotEqualTo("user", getCurrentUser());
        query.findInBackground(new FindCallback<Membership>() {
            @Override
            public void done(List<Membership> objects, ParseException e) {
                if (objects.isEmpty()) {
                    btnCheckIn.setVisibility(View.VISIBLE);
                    btnCheckIn.setText("Click to search for a group member");
                    btnCheckIn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            saveCurrentUserLocation();
                            checkProximity();
                        }
                    });
                } else {
                    drawButton();
                }
            }
        });
    }
    private void saveCurrentUserLocation() {
        // requesting permission to get user's location
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        else {
            // getting last know user's location
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            // checking if the location is null
            if(location != null){
                // if it isn't, save it to Back4App Dashboard
                ParseGeoPoint currentUserLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

                if (currMem != null) {
                    currMem.put("location", currentUserLocation);
                    currMem.saveInBackground();
                }
            }
            else {
                // if it is null, do something like displaying error and coming back to the menu activity
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPosts.clear();
        queryPosts();
    }
    private void queryMembers() {
            ParseQuery<Membership> parseQuery = new ParseQuery<Membership>(Membership.class);
            parseQuery.addDescendingOrder("updatedAt");
            parseQuery.whereEqualTo("group", group);
            parseQuery.include("user");
            parseQuery.findInBackground(new FindCallback<Membership>() {
                @Override
                public void done(List<Membership> objects, ParseException e) {
                    if (e != null) {
                        Log.e("Querying groups", "error with query");
                        e.printStackTrace();
                        return;
                    }
                    users.addAll(Membership.getAllUsers(objects));
                    friendAdapter.notifyDataSetChanged();
                }
            });
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

                // add new posts to the list and notify postsAdapter
                mPosts.addAll((List<Post>) (Object) posts);
                postsAdapter.notifyDataSetChanged();
            }
        });
    }

    public void checkPlace(final List<String> types) {
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
                            for (Place.Type type : Objects.requireNonNull(placeLikelihood.getPlace().getTypes())) {
                                if (types.contains(type.toString())) {
                                    drawButton();
                                    return;
                                }
                            }
                        }
                    } else {
                        Exception exception = task.getException();
                            ApiException apiException = (ApiException) exception;
                            Log.e(APP_TAG, "Place not found: " + apiException.getStatusCode());
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
        boolean hasCheckInLeft = false;

        if (numCheckIns.isEmpty()) {
            hasCheckInLeft = true;
        } else if (numCheckIns.get(numCheckIns.size() - 1) < currMem.getGroup().getFrequency()) {
            hasCheckInLeft = true;
        }
        if (hasCheckInLeft) {
            int currWeekCheckIns = numCheckIns.get(numCheckIns.size() - 1);
            btnCheckIn.setVisibility(View.VISIBLE);
            btnCheckIn.setText(String.format("%d check-ins left: check in now!", group.getFrequency() - currWeekCheckIns));
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
                            int currNum = numCheckIns.remove(numCheckIns.size() - 1);
                            numCheckIns.add(currNum + 1);
                            currMem.setNumCheckIns(numCheckIns);
                            int addedPoints = currMem.getGroup().getFrequency() / 10;
                            currMem.setPoints(currMem.getPoints() + addedPoints);
                            // TODO -- get category & switch case which category to add points to for the user
                            currMem.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    Log.d("checking in", "saved check in");
                                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Award");
                                    query.getInBackground(getString(R.string.first_complete_award), new GetCallback<ParseObject>() {
                                        public void done(ParseObject object, ParseException e) {
                                            if (e == null) {
                                                first = (Award) object;
                                                af.queryAward(first, false, true, getContext());
                                            } else {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    query.getInBackground(getString(R.string.one_week_streak_award), new GetCallback<ParseObject>() {
                                        public void done(ParseObject object, ParseException e) {
                                            if (e == null) {
                                                oneWeek = (Award) object;
                                                af.queryAward(oneWeek, false, true, getContext());
                                            } else {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    query.getInBackground(getString(R.string.tenacity_guru_award), new GetCallback<ParseObject>() {
                                        public void done(ParseObject object, ParseException e) {
                                            if (e == null) {
                                                tenacity = (Award) object;
                                                af.queryAward(tenacity, false, true, getContext());
                                            } else {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
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
                    // locations-related task you need to do.
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
