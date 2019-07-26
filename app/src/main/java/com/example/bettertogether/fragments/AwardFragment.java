package com.example.bettertogether.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.bettertogether.CreatePostActivity;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Award;
import com.example.bettertogether.models.UserAward;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


import static com.example.bettertogether.fragments.GroupFragment.REQUEST_CODE;
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
    private Award newAward = new Award();
    private UserAward userAward;
    private ParseUser user = getCurrentUser();
    private OnAwardFragmentInteractionListener mListener;

    private ImageView ivAwardImage;
    private TextView tvAwardName;
    private TextView tvAwardDescription;
    private TextView tvAwardStatus;
    private List<Award> achievedAwards = new ArrayList<>();
    private List<UserAward> userAwards = new ArrayList<UserAward>();


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
        queryAward(award, true, false, getContext());
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
        tvAwardStatus = view.findViewById(R.id.tvAwardStatus);

        if (award.getIcon() != null) {
            Glide.with(view.getContext()).load(award.getIcon().getUrl()).into(ivAwardImage);
        }

        tvAwardName.setText(award.getName());
        tvAwardDescription.setText(award.getDescription());
        if (userAward != null) {

        }

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

    private void setAwardStatus(List<UserAward> objects) {
        if (objects != null && objects.size() != 0) {
            userAward = objects.get(0);
            if (userAward.getIfAchieved()) {
                SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
                String dateString = format.format(userAward.getDateCompleted());
                tvAwardStatus.setText("Completed on " + dateString);
            } else {
                tvAwardStatus.setText(userAward.getNumCompleted() + " / " + userAward.getNumRequired() + " completed.");
            }
        } else {
            userAward = new UserAward();
            userAward.setAward(award);
            userAward.setUser(user);
            userAward.setIfAchieved(false);
            userAward.setNumCompleted(0);
            userAward.setNumRequired((Integer) award.get("numRequired"));
            tvAwardStatus.setText(userAward.getNumCompleted() + " / " + userAward.getNumRequired() + " completed.");
            userAward.saveInBackground();
        }
    }

    public void queryAward (Award awd, final Boolean setStatus, final Boolean checkAward, final Context con) {
        newAward = awd;
        ParseQuery<UserAward> query = new ParseQuery<>(UserAward.class);
        query.include("award");
        query.whereEqualTo("user", user);
        query.whereEqualTo("award", newAward);
        query.findInBackground(new FindCallback<UserAward>() {
            @Override
            public void done(List<UserAward> objects, ParseException e) {
                if (e != null) {
                    Log.e("Querying groups", "error with query");
                    e.printStackTrace();
                    return;
                }
                userAwards = new ArrayList<>();
                userAwards.addAll(objects);
                if (checkAward) {
                    checkAward(newAward, con);
                }
                if (setStatus) {
                    setAwardStatus(userAwards);
                }
            }
        });
    }

    public void checkAward(Award awd, Context context) {
        String objId = awd.getObjectId();
        if (userAwards != null && userAwards.size() != 0) {
            userAward = userAwards.get(0);
            if (userAward.getIfAchieved())
                return;
        } else {
            userAward = new UserAward();
            userAward.setAward(awd);
            userAward.setUser(user);
            userAward.setIfAchieved(false);
            userAward.setNumCompleted(0);
            userAward.setNumRequired((Integer) awd.get("numRequired"));
            userAward.saveInBackground();
            userAwards.add(userAward);
        }
        userAward.setNumCompleted(userAward.getNumCompleted() + 1);
        if (userAward.getNumCompleted() == userAward.getNumRequired()) {
            userAward.setIfAchieved(true);
            showAlert("Congrats!", "You have received a new award: "+ awd.get("name") + ". Please check your trophy case on profile page.", context);
            return;
        }
        userAward.saveInBackground();

        if (objId.equalsIgnoreCase(context.getResources().getString(R.string.flakiest_award))) {
            showAlert("Attention!", "Seems like you have been a bit flaky recently, you're " + (userAward.getNumRequired() - userAward.getNumCompleted()) + " groups from getting the Flakiest Award!", context);
        } else if (objId.equalsIgnoreCase(context.getResources().getString(R.string.social_butterfly_award))){
            showAlert("Congrats on joining a new group!", "You're " + (userAward.getNumRequired() - userAward.getNumCompleted()) + " groups from getting the Social Butterfly Award!", context);
        } else if (objId.equalsIgnoreCase(context.getResources().getString(R.string.angel_award))){
            showAlert("Congrats on joining a new group!", "You're " + (userAward.getNumRequired() - userAward.getNumCompleted()) + " groups from getting the Angel Award!", context);
        } else if (objId.equalsIgnoreCase(context.getResources().getString(R.string.swole_award))){
            showAlert("Congrats on joining a new group!", "You're " + (userAward.getNumRequired() - userAward.getNumCompleted()) + " groups from getting the Swole Award!", context);
        } else if (objId.equalsIgnoreCase(context.getResources().getString(R.string.tenacity_guru_award))){
            showAlert("Thanks for checking in!", "You are " + (userAward.getNumRequired() - userAward.getNumCompleted()) + " check-ins away from unlocking the Tenacity Guru Award!", context);
        } else if (objId.equalsIgnoreCase(context.getResources().getString(R.string.friendship_goals_award))){
            showAlert("Congrats on adding a new friend!", "You are " + (userAward.getNumRequired() - userAward.getNumCompleted()) + " friends away from unlocking the Friendship Goals Award!", context);
        } else if (objId.equalsIgnoreCase(context.getResources().getString(R.string.one_week_streak_award))){
            showAlert("Thanks for checking in!", "You are " + (userAward.getNumRequired() - userAward.getNumCompleted()) + " check-ins away from unlocking the One Week Streak Award!", context);
        } else if (objId.equalsIgnoreCase(context.getResources().getString(R.string.first_complete_award))){
            showAlert("Congrats on your first check-in!", "You have received a new award: First Complete. Please check your trophy case on profile page.", context);
        } else return;
    }

    public void showAlert(String title, String message, Context con) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(con);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                alert.show();
            }
        }, 3000);
//        alert.show();
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

