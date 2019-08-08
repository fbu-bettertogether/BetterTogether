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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.example.bettertogether.CheckAdapter;
import com.example.bettertogether.CreatePostActivity;
import com.example.bettertogether.Formatter;
import com.example.bettertogether.HomeActivity;
import com.example.bettertogether.InvitationActivity;
import com.example.bettertogether.Messaging;
import com.example.bettertogether.MyFirebaseMessagingService;
import com.example.bettertogether.PostsAdapter;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Award;
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
    // key and location params
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

    // widget params
    private Button btnCheckIn;
    private TextView tvDate;
    private TextView tvTimer;
    private TextView tvCreatePost;
    private ImageView ivProfPic;

    // check-in check marks
    private CheckAdapter checkAdapter;
    private RecyclerView rvChecks;

    // membership, group, and check-ins info
    private List<Integer> numCheckIns;
    private Membership currMem;
    private Category category;
    private ArrayList<ParseUser> addedMembers;
    private List<ParseUser> addedUsers;
    List<Membership> memberships;
    private boolean inGroup = false;
    int correctNumCheckIns = 0;
    boolean hasCheckInLeft = false;

    // posts fields
    private RecyclerView rvTimeline;
    private PostsAdapter postsAdapter;
    private List<Post> mPosts;

    // awards fields
    private Award first = new Award();
    private Award oneWeek = new Award();
    private Award tenacity = new Award();
    private AwardFragment af = new AwardFragment();

    // chart widgets
    private PieChart chart;
    private FrameLayout chartFrame;
    private Context mcontext;
    private KonfettiView viewKonfetti;

    // time fields
    private Date start;
    boolean nowBeforeStart;
    Calendar now;

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
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnCheckIn = view.findViewById(R.id.btnCheckIn);
        btnCheckIn.setVisibility(View.INVISIBLE);
        tvDate = view.findViewById(R.id.tvDate);
        tvTimer = view.findViewById(R.id.tvTimer);
        chart = view.findViewById(R.id.chart);
        chart.setVisibility(View.INVISIBLE);
        tvCreatePost = view.findViewById(R.id.tvCreatePost);
        ivProfPic = view.findViewById(R.id.ivProfPic);
        viewKonfetti = view.findViewById(R.id.viewKonfetti);
        // setting up recycler view of posts
        rvTimeline = view.findViewById(R.id.rvTimeline);
        mPosts = new ArrayList<>();
        postsAdapter = new PostsAdapter(getContext(), mPosts, getFragmentManager());
        rvTimeline.setAdapter(postsAdapter);
        rvTimeline.setLayoutManager(new LinearLayoutManager(getContext()));
        chartFrame = view.findViewById(R.id.chartFrame);
        rvChecks = view.findViewById(R.id.rvChecks);

        setUpToolbar(view);
        setUpRelativeDates();
        doubleCheckNumCheckIns();

        ParseQuery<Membership> parseQuery = new ParseQuery<Membership>(Membership.class);
        parseQuery.whereEqualTo("user", getCurrentUser());
        parseQuery.whereEqualTo("group", group);
        parseQuery.include("numCheckIns");
        parseQuery.include("group");
        parseQuery.include("user");
        parseQuery.findInBackground(new FindCallback<Membership>() {
            @Override
            public void done(final List<Membership> objects, ParseException e) {
                if (objects.size() == 0) {
                    // user is not in group
                    notInGroupOptions();
                } else {
                    setUpCreatePost(view);
                    inGroup = true;
                    if (group.getIsActive()) {
                        currMem = objects.get(0);
                        numCheckIns = currMem.getNumCheckIns();
                        configChart(false);
                        setUpCheckMarks();

                        if (numCheckIns.isEmpty()) {
                            hasCheckInLeft = false;
                        } else if (numCheckIns.get(numCheckIns.size() - 1) < currMem.getGroup().getFrequency()) {
                            hasCheckInLeft = true;
                        } else if (numCheckIns.get(numCheckIns.size() - 1) == currMem.getGroup().getFrequency()) {
                            group.setShowCheckInReminderBadge(false);
                        }

                        if (category.getName().equals("Get-Togethers")) {
                            saveCurrentUserLocation();
                        }

                        if (hasCheckInLeft) {
                            if (hasCheckedInToday()) {
                                textInsteadOfBtn("Checked-in today!");
                                setHelpMessage("already checked-in");
                            } else if (category.getName().equals("Get-Togethers")) {
                                checkProximity();
                            } else {
                                try {
                                    checkPlace(category.getLocationTypesList());
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        } else {
                            textInsteadOfBtn("You're done for the week!");
                            setHelpMessage("done for week");
                        }
                    } else {
                        textInsteadOfBtn("Group has not started yet! Hang tight!");
                        shrinkChart(chart);
                    }
                }
            }
        });
        queryPosts();
    }

    private void textInsteadOfBtn(String str) {
        btnCheckIn.setVisibility(View.INVISIBLE);
        tvTimer.setVisibility(View.VISIBLE);
        tvTimer.setText(str);
    }

    private void setUpCheckMarks() {
        checkAdapter = new CheckAdapter(numCheckIns.get(numCheckIns.size() - 1), group.getFrequency());
        rvChecks.setAdapter(checkAdapter);
        rvChecks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private boolean hasCheckedInToday() {
        now = Calendar.getInstance();
        Calendar prevCheckIn = Calendar.getInstance();
        Date lastCheckIn = currMem.getLastCheckIn();
        prevCheckIn.setTime(lastCheckIn);
        boolean checkedInToday = (now.get(Calendar.DAY_OF_YEAR) == prevCheckIn.get(Calendar.DAY_OF_YEAR))
                && (now.get(Calendar.YEAR) == prevCheckIn.get(Calendar.YEAR));
        return checkedInToday;
    }

    private void notInGroupOptions() {
        shrinkChart(chart);
        tvCreatePost.setVisibility(View.INVISIBLE);
        ivProfPic.setVisibility(View.INVISIBLE);
        if (group.getPrivacy().equals("public") && nowBeforeStart) {
            ParseQuery<Invitation> query = new ParseQuery<Invitation>("Invitation");
            query.whereEqualTo("receiver", ParseUser.getCurrentUser());
            query.whereEqualTo("group", group);
            query.getFirstInBackground(new GetCallback<Invitation>() {
                @Override
                public void done(Invitation object, ParseException e) {
                    if (object != null) {
                        textInsteadOfBtn("Request Pending");
                        setHelpMessage("request pending");
                    } else {
                        textInsteadOfBtn("You are not in this group");
                        setHelpMessage("join group");
                        btnCheckIn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Invitation invitation = new Invitation();
                                invitation.setGroup(group);
                                invitation.setReceiver(ParseUser.getCurrentUser());
                                invitation.setAccepted("sent");
                                invitation.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        textInsteadOfBtn("Request Pending");
                                    }
                                });

                            }
                        });
                    }
                }
            });
        } else {
            textInsteadOfBtn("Group is Unavailable");
        }
    }

    private void doubleCheckNumCheckIns() {
        ParseQuery<Membership> membershipQuery = ParseQuery.getQuery(Membership.class);
        membershipQuery.whereEqualTo("group", group);
        membershipQuery.include("numCheckIns");
        membershipQuery.include("user");
        membershipQuery.include("group");
        try {
            List<Membership> objects = membershipQuery.find();
            // check that numCheckIns is correct size for all members
            for (int i = 0; i < objects.size(); i++) {
                Membership currMem = objects.get(i);
                List<Integer> numCheckIns = currMem.getNumCheckIns();
                if (numCheckIns != null) {

                    while (numCheckIns.size() < correctNumCheckIns) {
                        numCheckIns.add(0);
                    }

                    currMem.setNumCheckIns(numCheckIns);
                    try {
                        currMem.save();
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    currMem.setNumCheckIns(new ArrayList<Integer>()); // should not happen
                    currMem.saveInBackground();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

    }

    private void setUpRelativeDates() {
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
        nowBeforeStart = now.before(start);

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
        String unit = "weeks";
        int time = weekDiff;

        if (weekDiff == 0) {
            unit = "days";
            time = (int) diff;
        }

        if (group.getIsActive()) {
            tvDate.setText(String.format("Active: %d %s left!", time, unit));
            correctNumCheckIns = group.getNumWeeks() - weekDiff;
        } else if (nowBeforeStart) {
            tvDate.setText(String.format("Inactive: starts in %d %s!", time, unit));
        } else {
            tvDate.setText(String.format("Inactive: completed %d %s ago!", time, unit));
        }
        tvDate.setTextColor(getResources().getColor(R.color.gray));
    }

    private void setUpCreatePost(View view) {
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
    }

    private void setUpToolbar(View view) {
        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        CollapsingToolbarLayout collapsingToolbar = view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(group.getName());
        setHasOptionsMenu(true);

        final ImageView imageView = view.findViewById(R.id.backdrop);
        if (group.getIcon() != null) {
            Glide.with(view.getContext())
                    .load(group.getIcon().getUrl())
                    .apply(RequestOptions.centerCropTransform())
                    .into(imageView);
        }
    }

    private void checkProximity() {
        ParseQuery<Membership> query = new ParseQuery<Membership>("Membership");
        query.whereWithinMiles("location", currMem.getParseGeoPoint("location"), 5);
        query.whereEqualTo("group", group);
        query.include("user");
        query.findInBackground(new FindCallback<Membership>() {
            @Override
            public void done(final List<Membership> objects, ParseException e) {
                if (objects.size() == 1) {
                    setHelpMessage("proximity");
                    drawButton(false);
                } else {
                    drawButton(true);
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
                    try {
                        currMem.save();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                Log.e("saving location", "location is null");
            }
        }
    }

    private void shrinkChart(PieChart chart) {
        chart.requestLayout();
        chart.getLayoutParams().height = 0;
        chart.getLayoutParams().width = 0;
        return;
    }

    private void expandChart(PieChart chart) {
        chart.requestLayout();
        chart.getLayoutParams().height = 700;
        chart.getLayoutParams().width = 0;
    }

    private void configChart(final boolean checkingIn) {

        if (!group.getIsActive()) {
            shrinkChart(chart);
            return;
        }

        expandChart(chart);

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
                    List<PieEntry> entries = getChartEntries(objects);
                    // add to pie chart
                    PieDataSet dataSet = new PieDataSet(entries, "group stats");
                    List<Integer> colors = new ArrayList<>();
                    colors.add(getResources().getColor(R.color.o1));
                    colors.add(getResources().getColor(R.color.o4));
                    colors.add(getResources().getColor(R.color.o8));
                    colors.add(getResources().getColor(R.color.originalOrange));
                    dataSet.setValueLineColor(R.color.orange);
                    dataSet.setColors(colors);
                    dataSet.setDrawValues(true);
                    dataSet.setSliceSpace(3);

                    PieData data = new PieData(dataSet);
                    data.setValueTextSize(20f);
                    data.setValueFormatter(new Formatter());
                    chart.setData(data);
                    chart.setCenterText("Week " + Integer.toString(correctNumCheckIns));
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

    private List<PieEntry> getChartEntries(List<Membership> objects) {
        List<PieEntry> entries = new ArrayList<>();
        int totalCheckIns = 0;
        // get last int of each numCheckIn, add to entries & keep track of sum
        for (int i = 0; i < objects.size(); i++) {
            Membership currMem = objects.get(i);
            List<Integer> numCheckIns = currMem.getNumCheckIns();

            int currWeek = numCheckIns.get(numCheckIns.size() - 1);
            if (currWeek > 0) {
                totalCheckIns += currWeek;
                String currUser = currMem.getUser().getUsername();
                PieEntry newEntry = new PieEntry(currWeek, currUser);
                entries.add(newEntry);
            }
        }

        // calculate total weekly check ins using frequency and the number of members
        int expectedCheckIns = group.getFrequency() * objects.size();
        int remainingCheckIns = expectedCheckIns - totalCheckIns;
        PieEntry remaining = new PieEntry(remainingCheckIns, "Remaining");
        entries.add(remaining);
        return entries;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if  (requestCode == ADD_REQUEST_CODE && resultCode == RESULT_OK) {
            addedMembers = data.getParcelableArrayListExtra("addedMembers");
            addedUsers.addAll(addedMembers);
            saveMemberships(addedMembers);
            MyFirebaseMessagingService mfms = new MyFirebaseMessagingService();
            mfms.logToken(getContext());
            if (addedMembers != null) {
                for (int i = 0; i < addedMembers.size(); i++) {
                    Messaging.sendNotification((String) addedMembers.get(i).get("deviceId"), ParseUser.getCurrentUser().getUsername() + " just added you to their group!");
                }
            }
        }
        addedMembers.clear();
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
                            double likelihood = placeLikelihood.getLikelihood();
                            for (Place.Type type : Objects.requireNonNull(placeLikelihood.getPlace().getTypes())) {
                                if (types.contains(type.toString()) & likelihood > 0.05) {
                                    drawButton(true);
                                    return;
                                } else {
                                    drawButton(false);
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
            setHelpMessage("place");
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_REQUEST_READ_CONTACTS);
        }
    }

    // sets help message if button is not shown or disabled, type specifies reason
    private void setHelpMessage(final String type) {
        btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (type) {

                    case "place":
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Get to a valid location");
                        builder.setMessage("This group requires you to be in a specific type of location to check in!");
                        try {
                            checkPlace(category.getLocationTypesList());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        builder.show();
                        break;

                    case "proximity":
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                        builder1.setTitle("Search for a nearby group member");
                        builder1.setMessage("This group requires you to be nearby to another member to check in!");
                        builder1.setPositiveButton("Find a member!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ParseQuery<Membership> query = new ParseQuery<Membership>("Membership");
                                query.whereWithinMiles("location", currMem.getParseGeoPoint("location"), 0.75);
                                query.whereEqualTo("group", group);
                                query.findInBackground(new FindCallback<Membership>() {
                                    @Override
                                    public void done(final List<Membership> objects, ParseException e) {
                                        if (objects.size() == 1) {
                                            FragmentManager manager = getChildFragmentManager();
                                            FragmentTransaction ft = manager.beginTransaction();
                                            ft.replace(R.id.chartFrame, MapFragment.newInstance(objects), APP_TAG);
                                            ft.commitAllowingStateLoss();
                                            saveCurrentUserLocation();
                                            checkProximity();
                                            chartFrame.getLayoutParams().height = 500;
                                            chartFrame.requestLayout();
                                        } else {
                                            drawButton(true);
                                        }
                                    }
                                });
                            }
                        });
                        builder1.show();
                        break;

                        case "already checked-in":
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                            builder2.setTitle("You already checked-in today");
                            builder2.setMessage("You can only check in once per day, see you tomorrow!");
                            builder2.show();
                            break;

                        case "done for week":
                            AlertDialog.Builder builder3 = new AlertDialog.Builder(getActivity());
                            builder3.setTitle("You are done for the week");
                            builder3.setMessage("You completed all your check-ins for this week, congrats!");
                            builder3.show();
                            break;

                        case "join group":
                            AlertDialog.Builder builder4 = new AlertDialog.Builder(getActivity());
                            builder4.setTitle("You are not currently in this group");
                            builder4.setMessage("It is a public group, and it hasn't started yet, so you can join!");
                            builder4.setPositiveButton("join group", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Invitation invitation = new Invitation();
                                    invitation.setGroup(group);
                                    invitation.setReceiver(ParseUser.getCurrentUser());
                                    invitation.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            textInsteadOfBtn("Request Pending");
                                            setHelpMessage("request pending");
                                        }
                                    });
                                }
                            });

                            builder4.show();
                            break;

                        case "request pending":
                            AlertDialog.Builder builder5 = new AlertDialog.Builder(getActivity());
                            builder5.setTitle("You're request to join this group is still pending");
                            builder5.setMessage("Check back later!");
                            builder5.show();
                            break;

                        default:
                            break;
                }
            }
        });
    }

    private void drawButton(boolean enabled) {

        btnCheckIn.setVisibility(View.VISIBLE);

        if (!enabled) {
            btnCheckIn.setBackgroundColor(getResources().getColor(R.color.gray));
            return;
        }

        btnCheckIn.setEnabled(true);
        btnCheckIn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        if (!getChildFragmentManager().getFragments().isEmpty()) {
            getChildFragmentManager().beginTransaction().remove(getChildFragmentManager().getFragments().get(0));
        }

        final int currWeekCheckIns = numCheckIns.get(numCheckIns.size() - 1);
        group.setShowCheckInReminderBadge(true);
        tvTimer.setVisibility(View.INVISIBLE);
        btnCheckIn.setVisibility(View.VISIBLE);
        btnCheckIn.setEnabled(true);

        btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAdapter.setChecks(checkAdapter.getChecks() + 1);
                checkAdapter.notifyDataSetChanged();
                currMem.setLastCheckIn(now.getTime());
                btnCheckIn.setVisibility(View.INVISIBLE);

                checkInCelebration(currWeekCheckIns);
                updatePoints();
                updateCurrMem(currWeekCheckIns);
            }

        });

    }

    private void updateCurrMem(final int currWeekCheckIns) {
        currMem.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.d("checking in", "saved check in");
                configChart(true);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkInPostAlert(currWeekCheckIns);
                    }
                }, 6000);
                updateAwards();
            }
        });
    }

    private void updateAwards() {
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

    private void updatePoints() {
        int currNum = numCheckIns.remove(numCheckIns.size() - 1);
        numCheckIns.add(currNum + 1);
        currMem.setNumCheckIns(numCheckIns);
        currMem.setPoints(currMem.getPoints() + 10);
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
                    user.put(pointsKey, currPoints + 10);
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            Log.d("points", "user points updated");
                        }
                    });
                }
            }
        });
    }

    private void checkInCelebration(int currWeekCheckIns) {
        group.setShowCheckInReminderBadge(false);
        if (currWeekCheckIns == group.getFrequency() - 1) {
            // final check-in for the week
            textInsteadOfBtn("Done for the week!");
            setHelpMessage("done for week");
            viewKonfetti.build()
                    .addColors(Color.RED, getResources().getColor(R.color.white), getResources().getColor(R.color.gold), getResources().getColor(R.color.colorPrimary))
                    .setDirection(0.0, 359.0)
                    .setSpeed(1f, 5f)
                    .setFadeOutEnabled(true)
                    .setTimeToLive(2000L)
                    .addShapes(Shape.RECT, Shape.CIRCLE)
                    .addSizes(new Size(12, 5))
                    .setPosition(-50f, viewKonfetti.getWidth() + 50f, -50f, -50f)
                    .streamFor(300, 5000L);

        } else {
            setHelpMessage("already checked-in");
            textInsteadOfBtn("Checked-in for the day!");
            viewKonfetti.build()
                    .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                    .setDirection(0.0, 359.0)
                    .setSpeed(1f, 5f)
                    .setFadeOutEnabled(true)
                    .setTimeToLive(2000L)
                    .addShapes(Shape.RECT, Shape.CIRCLE)
                    .addSizes(new Size(12, 5))
                    .setPosition(-50f, viewKonfetti.getWidth() + 50f, -50f, -50f)
                    .streamFor(300, 2000L);
        }
    }


    private void checkInPostAlert(final int currWeekCheckIns) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("You've checked in!");
        builder.setMessage("What do you want to do next?");
        builder.setPositiveButton("keep it simple", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int i) {

                final int nthCheckIn = currWeekCheckIns + 1;
                group.setShowCheckInReminderBadge(false);

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        ((AppCompatActivity) getActivity()).getMenuInflater().inflate(R.menu.group_detail_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (inGroup) {
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

                case R.id.action_more:
                    FragmentManager fm = ((AppCompatActivity) getContext()).getSupportFragmentManager();
                    DialogGroupDetailFragment groupDetailFragment = DialogGroupDetailFragment.newInstance(group);
                    groupDetailFragment.show(fm, "fragment_group_detail");
                    break;

                default:
                    break;
                }

        } else {
            if (item.getItemId() == android.R.id.home) {
                FragmentManager fragmentManager = ((AppCompatActivity) getContext()).getSupportFragmentManager();
                GroupsFragment fragment = new GroupsFragment();
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            } else {
                Toast.makeText(getContext(), "You are not a member of the group.", Toast.LENGTH_SHORT).show();
            }
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
