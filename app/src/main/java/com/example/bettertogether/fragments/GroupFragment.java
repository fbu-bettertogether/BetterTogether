package com.example.bettertogether.fragments;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bettertogether.AddUsersActivity;
import com.example.bettertogether.AlarmReceiver;
import com.example.bettertogether.CreatePostActivity;
import com.example.bettertogether.Formatter;
import com.example.bettertogether.FriendAdapter;
import com.example.bettertogether.HomeActivity;
import com.example.bettertogether.InvitationActivity;
import com.example.bettertogether.Messaging;
import com.example.bettertogether.PostsAdapter;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Award;
import com.example.bettertogether.models.CatMembership;
import com.example.bettertogether.models.Category;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Invitation;
import com.example.bettertogether.models.Membership;
import com.example.bettertogether.models.Post;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.parceler.Parcels;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

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
    public final int GROUP_INVITATION_REQUEST_CODE = 514;
    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    public PlacesClient placesClient;
    public final String APP_TAG = "BetterTogether";
    private static final String ARG_GROUP = "group";
    private Group group;
    private OnGroupFragmentInteractionListener mListener;
    private final int ADD_REQUEST_CODE = 20;
    public static final int REQUEST_CODE = 45;
    private ParseGeoPoint location;
    private ImageView ivBanner;
    private ImageView ivUserIcon;
    private ImageView ivSettings;
    private TextView tvGroupName;
    private Button btnCheckIn;
    private TextView tvDate;
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
    private PieChart chart;
    private ConstraintLayout constraintLayout;
    private FrameLayout chartFrame;
    private Context mcontext;
    private TextView tvCreatePost;
    private ImageView ivProfPic;
    private ScrollView scrollView;
    private KonfettiView viewKonfetti;
    private Date start;
    private ArrayList<ParseUser> addedMembers;
    private List<ParseUser> addedUsers;
    List<Membership> memberships;

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
        addedMembers = new ArrayList<>();
        memberships = new ArrayList<>();
        addedUsers = new ArrayList<>();
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
        tvGroupName = view.findViewById(R.id.tvGroupName);
        btnCheckIn = view.findViewById(R.id.btnCheckIn);
        tvDate = view.findViewById(R.id.tvDate);
        tvTimer = view.findViewById(R.id.tvTimer);
        chart = view.findViewById(R.id.chart);
        chart.setVisibility(View.INVISIBLE);
        constraintLayout = view.findViewById(R.id.constraintLayout);
        scrollView = view.findViewById(R.id.scrollView);
        tvCreatePost = view.findViewById(R.id.tvCreatePost);
        ivProfPic = view.findViewById(R.id.ivProfPic);
        ivSettings = view.findViewById(R.id.ivSettings);
        viewKonfetti = view.findViewById(R.id.viewKonfetti);
        // setting up recycler view of posts
        rvTimeline = view.findViewById(R.id.rvTimeline);
        mPosts = new ArrayList<>();
        postsAdapter = new PostsAdapter(getContext(), mPosts, getFragmentManager());
        rvTimeline.setAdapter(postsAdapter);
        rvTimeline.setLayoutManager(new LinearLayoutManager(getContext()));
        chartFrame = view.findViewById(R.id.chartFrame);
        users = new ArrayList<>();
        friendAdapter = new FriendAdapter(users, getFragmentManager());

        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        CollapsingToolbarLayout collapsingToolbar = view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(group.getName());
        setHasOptionsMenu(true);
        collapsingToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "title clicked", Toast.LENGTH_LONG).show();
                FragmentManager fm = ((AppCompatActivity) getContext()).getSupportFragmentManager();
                DialogGroupDetailFragment groupDetailFragment = DialogGroupDetailFragment.newInstance(group);
                groupDetailFragment.show(fm, "fragment_group_detail");

            }
        });

        final ImageView imageView = view.findViewById(R.id.backdrop);
        if (group.getIcon() != null) {
            Glide.with(view.getContext())
                    .load(group.getIcon().getUrl())
                    .apply(RequestOptions.centerCropTransform())
                    .into(imageView);
        }

        if (getCurrentUser().getParseFile("profileImage") != null) {
            Glide.with(view.getContext())
                    .load(((ParseFile) getCurrentUser().get("profileImage")).getUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfPic);
        }
        tvCreatePost.setText(String.format("Let %s know what you're up to!", group.getName()));
        tvCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkInPost();
            }
        });

        // getting date from string stored in group
        String startDateUgly = group.getStartDate();
        String endDateUgly = group.getEndDate();

        // translating string into Java Date
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");
        Date start = null;
        Date end = null;
        try {
            start = sdf.parse(startDateUgly);
            end = sdf.parse(endDateUgly);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        // turning dates into relative time from now
        Date now = Calendar.getInstance().getTime();
        final boolean nowBeforeStart = now.before(start);

        long diffInMillis = 0;
        if (group.getIsActive()) {
            diffInMillis = end.getTime() - now.getTime();
        } else if (nowBeforeStart) {
            diffInMillis = start.getTime() - now.getTime();
        } else {
            diffInMillis = end.getTime() - now.getTime();
        }

        long diff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        int weekDiff = (int) diff / 7;

        if(group.getIsActive()) {
            tvDate.setText(String.format("Active: %d weeks left!", weekDiff));
        } else if (nowBeforeStart){
            tvDate.setText(String.format("Inactive: starts in %d weeks!", weekDiff));
        } else {
            tvDate.setText(String.format("Inactive: completed %d weeks ago!", weekDiff));
        }
        tvDate.setTextColor(getResources().getColor(R.color.gray));

        ParseQuery<Membership> parseQuery = new ParseQuery<Membership>(Membership.class);
        parseQuery.whereEqualTo("user", getCurrentUser());
        parseQuery.whereEqualTo("group", group);
        parseQuery.include("numCheckIns");
        parseQuery.include("group");
        parseQuery.include("user");
        parseQuery.findInBackground(new FindCallback<Membership>() {
            @Override
            public void done(final List<Membership> objects, ParseException e) {
                if (objects.size() > 0) {
                    if (group.getIsActive()) {
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
                        btnCheckIn.setVisibility(View.INVISIBLE);
                        tvTimer.setVisibility(View.VISIBLE);
                        tvTimer.setText("Group has not started yet! Hang tight!");
                    }
                } else {
                    ivSettings.setVisibility(View.INVISIBLE);
                    if (!group.getPrivacy().equals("private") & nowBeforeStart) {
                        ParseQuery<Invitation> query = new ParseQuery<Invitation>("Invitation");
                        query.whereEqualTo("receiver", ParseUser.getCurrentUser());
                        query.whereEqualTo("group", group);
                        query.getFirstInBackground(new GetCallback<Invitation>() {
                            @Override
                            public void done(Invitation object, ParseException e) {
                                if (object != null) {
                                    btnCheckIn.setText("Request Pending");
                                } else {
                                    btnCheckIn.setText("Click to Join");
                                    btnCheckIn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Invitation invitation = new Invitation();
                                            invitation.setGroup(group);
                                            invitation.setReceiver(ParseUser.getCurrentUser());
                                            invitation.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    btnCheckIn.setText("Request Pending");
                                                }
                                            });

//                                Membership membership = new Membership();
//                                membership.setGroup(group);
//                                membership.setUser(ParseUser.getCurrentUser());
//                                membership.saveInBackground();
//                                btnCheckIn.setOnClickListener(null);
//                                currMem = membership;
//                                numCheckIns = currMem.getNumCheckIns();
//                                if (numCheckIns == null) {
//                                    numCheckIns = new ArrayList<>();
//                                    numCheckIns.add(0);
//                                }
//                                try {
//                                    checkPlace(category.getLocationTypesList());
//                                } catch (JSONException e1) {
//                                    e1.printStackTrace();
//                                }

                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        btnCheckIn.setText("Group is Unavailable");
                    }
                }
            }
        });
        queryMembers();
        queryPosts();
        configChart(false);
    }

    private void checkProximity() {
        ParseQuery<Membership> query = new ParseQuery<Membership>("Membership");
        query.whereWithinMiles("location", currMem.getParseGeoPoint("location"), 0.75);
        query.whereEqualTo("group", group);
        query.findInBackground(new FindCallback<Membership>() {
            @Override
            public void done(final List<Membership> objects, ParseException e) {
                if (objects.size() == 1) {
                    btnCheckIn.setVisibility(View.VISIBLE);
                    btnCheckIn.setText("Click to search for a group member");
                    btnCheckIn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            FragmentManager manager = getChildFragmentManager();
                            FragmentTransaction ft = manager.beginTransaction();
                            ft.replace(R.id.chartFrame, MapFragment.newInstance(objects), APP_TAG);
                            ft.commitAllowingStateLoss();
                            saveCurrentUserLocation();
                            checkProximity();
                            chartFrame.getLayoutParams().height = 500;
                            chartFrame.requestLayout();
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

    private void configChart(final boolean checkingIn) {

        if (!group.getIsActive()) {
            chart.requestLayout();
            chart.getLayoutParams().height = 0;
            chart.getLayoutParams().width = 0;
            return;
        }

        chart.requestLayout();
        chart.getLayoutParams().height = 700;
        chart.getLayoutParams().width = 0;

        // query for membership objects with this group
        ParseQuery<Membership> membershipQuery = ParseQuery.getQuery(Membership.class);
        membershipQuery.whereEqualTo("group", group);
        membershipQuery.include("numCheckIns");
        membershipQuery.include("user");
        membershipQuery.include("group");
        membershipQuery.findInBackground(new FindCallback<Membership>() {
            @Override
            public void done(List<Membership> objects, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                } else {
                    int weekNumber = 0;
                    List<PieEntry> entries = new ArrayList<>();
                    int totalCheckIns = 0;
                    // get last int of each numCheckIn, add to entries & keep track of sum
                    for (int i = 0; i < objects.size(); i++) {
                        Membership currMem = objects.get(i);
                        List<Integer> numCheckIns = currMem.getNumCheckIns();
                        if (numCheckIns != null) {
                            if (numCheckIns.isEmpty()) {
                                numCheckIns.add(0);
                            }
                            int currWeek = numCheckIns.get(numCheckIns.size() - 1);
                            weekNumber = numCheckIns.size();
                            if (currWeek > 0) {
                                totalCheckIns += currWeek;
                                String currUser = currMem.getUser().getUsername();
                                PieEntry newEntry = new PieEntry(currWeek, currUser);
                                entries.add(newEntry);
                            }
                        }
                        // numCheckIns should not have size 0 because will not draw chart if inactive

                    }
                    // calculate total weekly check ins using frequency and the number of members
                    int expectedCheckIns = group.getFrequency() * objects.size();
                    int remainingCheckIns = expectedCheckIns - totalCheckIns;
                    PieEntry remaining = new PieEntry(remainingCheckIns, "Remaining");
                    entries.add(remaining);

                    // add to pie chart
                    PieDataSet dataSet = new PieDataSet(entries, "group stats");
                    List<Integer> colors = new ArrayList<>();
                    colors.add(getResources().getColor(R.color.colorPrimary));
                    colors.add(getResources().getColor(R.color.o4));
                    colors.add(getResources().getColor(R.color.o8));
                    colors.add(getResources().getColor(R.color.colorPrimaryDark));
                    dataSet.setValueLineColor(R.color.colorPrimary);
                    dataSet.setColors(colors);
                    dataSet.setDrawValues(true);
                    dataSet.setSliceSpace(3);

                    PieData data = new PieData(dataSet);
                    data.setValueTextSize(20f);
                    data.setValueFormatter(new Formatter());
                    chart.setData(data);
                    chart.setCenterText("Week " + Integer.toString(weekNumber));
                    chart.getDescription().setEnabled(false);
                    chart.getLegend().setEnabled(false);

                    if (checkingIn) {
                        chart.spin(3000, 0, 360f, Easing.EaseInQuad);
                    } else {
                        chart.animateY(1500, Easing.EaseInOutQuad);
                    }

                    chart.invalidate();
                    chart.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if  (requestCode == ADD_REQUEST_CODE && resultCode == RESULT_OK) {
            addedMembers = data.getParcelableArrayListExtra("addedMembers");
            addedUsers.addAll(addedMembers);
            saveMemberships(addedMembers);
            if (addedMembers != null) {
                for (int i = 0; i < addedMembers.size(); i++) {
                    Messaging.sendNotification((String) addedMembers.get(i).get("deviceId"), ParseUser.getCurrentUser().getUsername() + " just added you to their group!");
                }
            }
        }
        mPosts.clear();
        queryPosts();
    }

    private void saveMemberships(final ArrayList<ParseUser> addedMembers) {
        List<Membership> memberships = new ArrayList<>();
        for (int i = 0; i < addedMembers.size(); i++) {
            memberships.add(new Membership());
            memberships.get(i).setGroup(group);
            memberships.get(i).setUser(addedMembers.get(i));
            ArrayList<Integer> numCheckIns = new ArrayList<Integer>();
            memberships.get(i).setNumCheckIns(numCheckIns);
            memberships.get(i).setPoints(0);
            final int finalI = i;
            memberships.get(i).saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        e.printStackTrace();
                    }
                    if (finalI == addedMembers.size() - 1) {
                        try {
                            scheduleAlarm(group);
                        } catch (java.text.ParseException ex) {
                            ex.printStackTrace();
                        }
                        Intent i = new Intent(getContext(), HomeActivity.class);
                        startActivityForResult(i, REQUEST_CODE);
                    }
                }
            });
        }
    }

    // set up recurring alarm so that numCheckIns gets moved to new entry each week
    // will also check if group is active
    public void scheduleAlarm(Group group) throws java.text.ParseException {
        // construct an intent to execute the AlarmReceiver
        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("group", (Serializable) group);
        intent.putExtra("bundle", bundle);
        // create a pending intent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(getContext(), AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // setup periodic alarm every week from the start day onwards
        AlarmManager alarm = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            cal.setTime(sdf.parse(group.getStartDate()));
            start = cal.getTime();
            alarm.setInexactRepeating(AlarmManager.RTC, start.getTime(), AlarmManager.INTERVAL_DAY * 7, pIntent);
        }
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

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> posts, ParseException e) {
                if (e != null) {
                    Log.e("Querying posts", "error with query");
                    e.printStackTrace();
                    return;
                }

                // add new posts to the list and notify postsAdapter
                mPosts.clear();
                mPosts.addAll((List<Post>) (Object) posts);
                postsAdapter.notifyDataSetChanged();
            }
        });
    }

    public void checkPlace(final List<String> types) {
        final List<PlaceLikelihood> validPlaces = new ArrayList();
        boolean inValidPlace = false;
        // Initialize the SDK
        Places.initialize(mcontext, getString(R.string.places_key));

        // Create a new Places client instance
        placesClient = Places.createClient(mcontext);
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

    private void drawButton() {
        if (!getChildFragmentManager().getFragments().isEmpty()) {
            getChildFragmentManager().beginTransaction().remove(getChildFragmentManager().getFragments().get(0));
        }
        boolean hasCheckInLeft = false;
        if (numCheckIns.isEmpty()) {
            hasCheckInLeft = true;
        } else if (numCheckIns.get(numCheckIns.size() - 1) < currMem.getGroup().getFrequency()) {
            hasCheckInLeft = true;
            group.setShowCheckInReminderBadge(true);
        } else if (numCheckIns.get(numCheckIns.size() - 1) == currMem.getGroup().getFrequency()) {
            group.setShowCheckInReminderBadge(false);
        }
        if (hasCheckInLeft) {
            final int currWeekCheckIns = numCheckIns.get(numCheckIns.size() - 1);
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
//                            CommonConfetti.rainingConfetti(constraintLayout, new int[] {R.color.colorPrimary}).oneShot();
                            viewKonfetti.build()
                                    .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                                    .setDirection(0.0, 359.0)
                                    .setSpeed(1f, 5f)
                                    .setFadeOutEnabled(true)
                                    .setTimeToLive(2000L)
                                    .addShapes(Shape.RECT, Shape.CIRCLE)
                                    .addSizes(new Size(12, 5))
                                    .setPosition(-50f, viewKonfetti.getWidth() + 50f, -50f, -50f)
                                    .streamFor(300, 5000L);

                            int currNum = numCheckIns.remove(numCheckIns.size() - 1);
                            numCheckIns.add(currNum + 1);
                            currMem.setNumCheckIns(numCheckIns);
                            final int addedPoints = currMem.getGroup().getMinTime();
                            currMem.setPoints(currMem.getPoints() + addedPoints);
                            ParseQuery<Group> groupQuery = ParseQuery.getQuery(Group.class);
                            groupQuery.whereEqualTo("objectId", currMem.getGroup().getObjectId());
                            groupQuery.include("category");
                            groupQuery.findInBackground(new FindCallback<Group>() {
                                @Override
                                public void done(List<Group> objects, ParseException e) {
                                    if (e != null) {
                                        e.printStackTrace();
                                        return;
                                    } else {
                                        Group g = objects.get(0);
                                        String cat = g.getCategory();
                                        String basePoints = "";
                                        // updating the appropriate points category of the user based on the category
                                        switch(cat) {
                                            case "Fitness":
                                                basePoints = "fitness";
                                                break;
                                            case "Get-Togethers":
                                                basePoints = "getTogether";
                                                break;
                                            case "Service":
                                                basePoints = "service";
                                                break;
                                        }

                                        String pointsKey = basePoints + "Points";
                                        ParseUser user = currMem.getUser();
                                        int currPoints = user.getInt(pointsKey);
                                        user.put(pointsKey, currPoints + addedPoints);
                                        user.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                Log.d("points", "user points updated");
                                            }
                                        });
                                    }
                                }
                            });

                            currMem.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    Log.d("checking in", "saved check in");
                                    configChart(true);
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
//                                            checkInPost();
                                            doAlerts(currWeekCheckIns);
                                        }
                                    }, 6000);

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

    private void doAlerts(final int currWeekCheckIns) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("You've checked in!");
        builder.setMessage("What do you want to do next?");
        builder.setPositiveButton("keep it simple", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int i) {

                final int nthCheckIn = currWeekCheckIns + 1;

                ParseQuery<ParseUser> checkInBotQuery = ParseQuery.getQuery(ParseUser.class);
                checkInBotQuery.whereEqualTo("username", "Check In Bot");
                checkInBotQuery.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> objects, ParseException e) {
                        ParseUser checkInBot = objects.get(0);
                        final Post post = new Post();
                        post.setUser(checkInBot);
                        post.setGroup(group);
                        String suffix = "th";
                        switch (nthCheckIn) {
                            case 1:
                                suffix = "st";
                                break;
                            case 2:
                                suffix = "nd";
                                break;
                            case 3:
                                suffix = "rd";
                                break;
                        }

                        post.setDescription(String.format("%s just completed their %d%s check in for the week with %s! Awesome!", getCurrentUser().getUsername(), nthCheckIn, suffix, group.getName()));

                        post.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                ParseRelation<Post> groupRelation = group.getRelation("posts");
                                groupRelation.add(post);

                                group.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        dialog.dismiss();
                                        queryPosts();

                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

        builder.setNegativeButton("get fancy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                // go to create post
                dialog.dismiss();
                checkInPost();
            }
        });

        builder.show();
    }

    private void checkInPost() {
        Intent i = new Intent(getContext(), CreatePostActivity.class);
        i.putExtra("group", Parcels.wrap(group));
        startActivityForResult(i, REQUEST_CODE);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        ((AppCompatActivity) getActivity()).getMenuInflater().inflate(R.menu.group_detail_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fragmentManager = ((AppCompatActivity) getContext()).getSupportFragmentManager();
                GroupsFragment fragment = new GroupsFragment();
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                break;
            case R.id.action_requests:
                Intent intent = new Intent(getContext(), InvitationActivity.class);
                intent.putExtra("group", (Parcelable) group);
                startActivityForResult(intent, GROUP_INVITATION_REQUEST_CODE);
                break;
            case R.id.action_leave:
                if (group.getIsActive()) {
                    Toast.makeText(getContext(), "Can't leave active group", Toast.LENGTH_LONG).show();
                } else {
                    ParseQuery<Membership> membershipParseQuery = new ParseQuery<Membership>("Membership");
                    membershipParseQuery.whereEqualTo("group", group);
                    membershipParseQuery.whereEqualTo("user", ParseUser.getCurrentUser());
                    membershipParseQuery.getFirstInBackground(new GetCallback<Membership>() {
                        @Override
                        public void done(Membership object, ParseException e) {
                            if (e != null) {
                                e.printStackTrace();
                            } else if (object != null) {
                                try {
                                    object.delete();
                                    Toast.makeText(getContext(), "You have left group.", Toast.LENGTH_LONG).show();
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    });
                }
                break;
            case R.id.action_invite_to_group:
                ParseQuery<Membership> parseQuery = new ParseQuery<Membership>(Membership.class);
                parseQuery.whereEqualTo("group", group);
                parseQuery.include("group");
                parseQuery.include("user");
                parseQuery.findInBackground(new FindCallback<Membership>() {
                    @Override
                    public void done(List<Membership> objects, ParseException e) {
                        if (e != null) {
                            Log.e("Querying memberships", "error with query");
                            e.printStackTrace();
                            return;
                        } else if (objects == null || objects.size() == 0) {
                            Log.e("membership querying", "no membership objects found with this group");
                        }
                        memberships.addAll(objects);
                        for (Membership mem : memberships) {
                            addedUsers.add(mem.getUser());
                        }
                        Intent intent = new Intent(getContext(), AddUsersActivity.class);
                        //Keeps track of which users have already been added, so that they cannot be added again.
                        intent.putParcelableArrayListExtra("alreadyAdded", (ArrayList<? extends Parcelable>) addedUsers);
                        intent.putExtra("situation", R.string.invite_friend_to_group);
                        startActivityForResult(intent, ADD_REQUEST_CODE);
                    }
                });
                break;
            default:
                break;
        }

        Log.d("itemId", item.toString());
        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGroupFragmentInteractionListener) {
            mListener = (OnGroupFragmentInteractionListener) context;
        }
        mcontext = context;
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
