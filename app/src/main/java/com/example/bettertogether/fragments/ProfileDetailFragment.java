package com.example.bettertogether.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.bettertogether.MainActivity;
import com.example.bettertogether.R;
import com.example.bettertogether.fragments.AwardFragment;
import com.example.bettertogether.fragments.ProfileFragment;
import com.example.bettertogether.models.Award;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.example.bettertogether.models.Post;
import com.example.bettertogether.models.UserAward;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class ProfileDetailFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String param_user = "param_user";
    public final String APP_TAG = "BetterTogether";

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
