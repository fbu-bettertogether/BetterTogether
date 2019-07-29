package com.example.bettertogether.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bettertogether.InvitationActivity;
import com.example.bettertogether.MainActivity;
import com.example.bettertogether.R;
import com.parse.ParseUser;


public class ProfileDetailFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String param_user = "param_user";
    public final String APP_TAG = "BetterTogether";
    public final int INVITATION_REQUEST_CODE = 232;
    private ProfileFragment.OnProfileFragmentInteraction mListener;
    private Button btnPendingRequests;
    private Button btnNotifications;
    private Button btnLogOut;
    private Button btnProfilePicture;
    private Button btnGroupHistory;
    private Button btnDeleteAccount;

    private ParseUser user;

    public ProfileDetailFragment() {
        // Required empty public constructor
    }

    public static ProfileDetailFragment newInstance(ParseUser varUser) {
        ProfileDetailFragment fragment = new ProfileDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(param_user, varUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable(param_user);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_profile_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnPendingRequests = view.findViewById(R.id.btnPendingRequests);
        btnNotifications = view.findViewById(R.id.btnNotifications);
        btnLogOut = view.findViewById(R.id.btnLogOut);
        btnProfilePicture = view.findViewById(R.id.btnProfilePicture);
        btnGroupHistory = view.findViewById(R.id.btnGroupHistory);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);

        btnProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.createProfilePicture(view);
            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOut();
                final Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        btnPendingRequests.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View view) {
                                                      Intent intent = new Intent(getContext(), InvitationActivity.class);
                                                      startActivityForResult(intent, INVITATION_REQUEST_CODE);
                                                  }
                                              }

        );
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProfileFragment.OnProfileFragmentInteraction) {
            mListener = (ProfileFragment.OnProfileFragmentInteraction) context;
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

}
