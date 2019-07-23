package com.example.bettertogether.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Award;
import com.example.bettertogether.models.CatMembership;
import com.example.bettertogether.models.UserAward;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


import static com.parse.ParseUser.getCurrentUser;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnAwardFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AwardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class AwardFragment extends Fragment {
    public final String APP_TAG = "BetterTogether";
    private static final String ARG_AWARD = "award";
    private Award award;
    private ParseUser user = getCurrentUser();
    private OnAwardFragmentInteractionListener mListener;

    private ImageView ivAwardImage;
    private TextView tvAwardName;
    private TextView tvAwardDescription;
    private List<Award> achievedAwards = new ArrayList<>();


    public AwardFragment() {
        // Required empty public constructor
    }

    public static AwardFragment newInstance(Award award) {
        AwardFragment fragment = new AwardFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_AWARD, award);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.award = (Award) getArguments().getParcelable(ARG_AWARD);
        }
        findUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout. award_details, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivAwardImage = view.findViewById(R.id.ivAwardImage);
        tvAwardName = view.findViewById(R.id.tvAwardName);
        tvAwardDescription = view.findViewById(R.id.tvAwardDescription);

        if (award.getIcon() != null) {
            Glide.with(view.getContext()).load(award.getIcon().getUrl()).into(ivAwardImage);
        }

        tvAwardName.setText(award.getName());
        tvAwardDescription.setText(award.getDescription());

        ParseQuery<UserAward> query = new ParseQuery<>(UserAward.class);
        query.include("award");
        query.whereEqualTo("user", user);
        query.findInBackground(new FindCallback<UserAward>() {
            @Override
            public void done(List<UserAward> objects, ParseException e) {
                if (e != null) {
                    Log.e("Querying groups", "error with query");
                    e.printStackTrace();
                    return;
                }
                if (objects != null) {
                    for (int i = 0; i < objects.size(); i++) {
                        if (objects.get(i).getIfAchieved()) {
                            achievedAwards.add(objects.get(i).getAward());
                        }
                    }
                    for (int i = 0; i < achievedAwards.size(); i++) {
                        if (achievedAwards.get(i).get("name").toString().equalsIgnoreCase(award.get("name").toString())) {
                            Resources res = getContext().getResources();
                            final int goldTint = res.getColor(R.color.gold);
                            ivAwardImage.setColorFilter(goldTint);
                        }
                    }
                }
            }
        });
    }

    private void findUser() {
        ParseQuery<UserAward> query = new ParseQuery<>(UserAward.class);
        query.include("award");
        query.whereEqualTo("user", user);
        query.findInBackground(new FindCallback<UserAward>() {
            @Override
            public void done(List<UserAward> objects, ParseException e) {
                if (e != null) {
                    Log.e("Querying groups", "error with query");
                    e.printStackTrace();
                    return;
                }
                user = objects.get(0).getUser();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAwardFragmentInteractionListener) {
            mListener = (OnAwardFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnAwardFragmentInteractionListener {
        void onAwardFragmentInteraction();
    }
}

