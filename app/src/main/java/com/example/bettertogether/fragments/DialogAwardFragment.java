package com.example.bettertogether.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Award;
import com.example.bettertogether.models.UserAward;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
// ...

public class DialogAwardFragment extends DialogFragment {


    private Award award;
    private ImageView ivAwardImage;
    private TextView tvAwardName;
    private TextView tvAwardDescription;
    private TextView tvAwardStatus;
    private List<Award> achievedAwards = new ArrayList<>();
    private List<UserAward> userAwards = new ArrayList<UserAward>();
    private Boolean isAchieved;
    private String username;

    public DialogAwardFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static DialogAwardFragment newInstance(Award award, boolean isAchieved, String username) {
        DialogAwardFragment frag = new DialogAwardFragment();
        Bundle args = new Bundle();
        args.putSerializable("award", award);
        args.putBoolean("isAchieved", isAchieved);
        args.putString("username", username);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return inflater.inflate(R.layout.fragment_dialog_award, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivAwardImage = view.findViewById(R.id.ivAwardImage);
        tvAwardName = view.findViewById(R.id.tvAwardName);
        tvAwardDescription = view.findViewById(R.id.tvAwardDescription);
        tvAwardStatus = view.findViewById(R.id.tvAwardStatus);

        // Fetch arguments from bundle and set title
        award = (Award) getArguments().getSerializable("award");
        isAchieved = (Boolean) getArguments().getBoolean("isAchieved");
        username = (String) getArguments().getString("username");

        getDialog().setTitle(award.getName());

        if (award.getIcon() != null) {
            Glide.with(view.getContext()).load(award.getIcon().getUrl()).into(ivAwardImage);
        }

        tvAwardName.setText(award.getName());
        tvAwardDescription.setText(award.getDescription());

        if (!isAchieved) {
            ivAwardImage.setColorFilter(getContext().getResources().getColor(R.color.grey));
        }

        ParseQuery<ParseUser> userQuery = new ParseQuery<ParseUser>(ParseUser.class);
        userQuery.whereEqualTo("username", username);
        userQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }

                final ParseUser user = objects.get(0);
                ParseQuery<UserAward> userAwardQuery = new ParseQuery<UserAward>(UserAward.class);
                userAwardQuery.whereEqualTo("user", user);
                userAwardQuery.whereEqualTo("award", award);
                userAwardQuery.include("numCompleted");
                userAwardQuery.include("numRequired");
                userAwardQuery.findInBackground(new FindCallback<UserAward>() {
                    @Override
                    public void done(List<UserAward> objects, ParseException e) {
                        UserAward userAward;
                        if (objects.size() == 0) {
                            userAward = new UserAward();
                            userAward.setAward(award);
                            userAward.setUser(user);
                            userAward.setIfAchieved(false);
                            userAward.setNumCompleted(0);
                            userAward.setNumRequired((Integer) award.get("numRequired"));
                            userAward.saveInBackground();
                            userAwards.add(userAward);
                            userAward.saveInBackground();
                        } else {
                            userAward = objects.get(0);
                        }
                        if (userAward.getIfAchieved()) {
                            SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
                            String dateString = format.format(userAward.getDateCompleted());
                            tvAwardStatus.setText("Completed on " + dateString);
                        } else {
                            tvAwardStatus.setText(userAward.getNumCompleted() + " / " + userAward.getNumRequired() + " completed");
                        }
                    }
                });
            }
        });
    }
}
