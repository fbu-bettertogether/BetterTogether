package com.example.bettertogether.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bettertogether.InvitationActivity;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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
    private OnFragmentInteractionListener mListener;
    private Button btnPendingRequests;
    private Button btnNotifications;
    private Button btnLeaveGroup;
    private Button btnProfilePicture;
    private Button btnGroupHistory;
    private Button btnDeleteGroup;

    private Group group;


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
        btnPendingRequests = view.findViewById(R.id.btnPendingRequests);
        btnNotifications = view.findViewById(R.id.btnNotifications);
        btnLeaveGroup = view.findViewById(R.id.btnLeaveGroup);
        btnProfilePicture = view.findViewById(R.id.btnProfilePicture);
        btnGroupHistory = view.findViewById(R.id.btnGroupHistory);
        btnDeleteGroup = view.findViewById(R.id.btnDeleteGroup);

        btnProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mListener.createProfilePicture(view);
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

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
