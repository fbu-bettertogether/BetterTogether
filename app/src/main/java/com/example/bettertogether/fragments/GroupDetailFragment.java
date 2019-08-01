package com.example.bettertogether.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bettertogether.AddUsersActivity;
import com.example.bettertogether.AddUsersAdapter;
import com.example.bettertogether.AlarmReceiver;
import com.example.bettertogether.HomeActivity;
import com.example.bettertogether.InvitationActivity;
import com.example.bettertogether.MakeNewGroupActivity;
import com.example.bettertogether.Messaging;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.Serializable;
import java.lang.reflect.Member;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupDetailFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String param_group = "param_group";
    public final String APP_TAG = "BetterTogether";
    public final int GROUP_INVITATION_REQUEST_CODE = 514;
    private final int REQUEST_CODE = 10;
    private final int ADD_REQUEST_CODE = 20;

    private InvitationFragment.OnFragmentInteractionListener mListener;
    private Button btnPendingRequests;
    private Button btnInviteToGroup;
    private Button btnLeaveGroup;
    private Button btnProfilePicture;
    private Button btnGroupHistory;
    private Button btnDeleteGroup;

    private Group group;
    private ArrayList<ParseUser> addedMembers;
    private List<ParseUser> addedUsers = new ArrayList<ParseUser>();
    private Date start;
    List<Membership> memberships = new ArrayList<Membership>();

    public GroupDetailFragment() {
        // Required empty public constructor
    }

    public static GroupDetailFragment newInstance(Group group) {
        GroupDetailFragment fragment = new GroupDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(param_group, group);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.group = getArguments().getParcelable(param_group);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_detail_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addedMembers = new ArrayList<>();
        btnPendingRequests = view.findViewById(R.id.btnPendingRequests);
        btnInviteToGroup = view.findViewById(R.id.btnInviteToGroup);
        btnLeaveGroup = view.findViewById(R.id.btnLeaveGroup);
        btnProfilePicture = view.findViewById(R.id.btnProfilePicture);
        btnGroupHistory = view.findViewById(R.id.btnGroupHistory);
        btnDeleteGroup = view.findViewById(R.id.btnDeleteGroup);

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

                btnProfilePicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //                mListener.createProfilePicture(view);
                    }
                });

                btnInviteToGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), AddUsersActivity.class);
                        //Keeps track of which users have already been added, so that they cannot be added again.
                        intent.putParcelableArrayListExtra("alreadyAdded", (ArrayList<? extends Parcelable>) addedUsers);
                        startActivityForResult(intent, ADD_REQUEST_CODE);
                    }
                });

                btnLeaveGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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
                    }
                });

                btnPendingRequests.setOnClickListener(new View.OnClickListener() {
                                                          @Override
                                                          public void onClick(View view) {
                                                              Intent intent = new Intent(getContext(), InvitationActivity.class);
                                                              intent.putExtra("group", (Parcelable) group);
                                                              startActivityForResult(intent, GROUP_INVITATION_REQUEST_CODE);
                                                          }
                                                      }

                );
            }
        });
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

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener extends InvitationFragment.OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
