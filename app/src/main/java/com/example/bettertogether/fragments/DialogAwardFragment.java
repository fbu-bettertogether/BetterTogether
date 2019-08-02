package com.example.bettertogether.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bettertogether.FriendAdapter;
import com.example.bettertogether.MemberAdapter;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Award;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.example.bettertogether.models.UserAward;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.parse.ParseUser.getCurrentUser;
// ...

public class DialogAwardFragment extends DialogFragment {


    private Award award;
    private ImageView ivAwardImage;
    private TextView tvAwardName;
    private TextView tvAwardDescription;
    private List<Award> achievedAwards = new ArrayList<>();
    private List<UserAward> userAwards = new ArrayList<UserAward>();
    private Boolean isAchieved;

    public DialogAwardFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static DialogAwardFragment newInstance(Award award, boolean isAchieved) {
        DialogAwardFragment frag = new DialogAwardFragment();
        Bundle args = new Bundle();
        args.putSerializable("award", award);
        args.putBoolean("isAchieved", isAchieved);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_award, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivAwardImage = view.findViewById(R.id.ivAwardImage);
        tvAwardName = view.findViewById(R.id.tvAwardName);
        tvAwardDescription = view.findViewById(R.id.tvAwardDescription);

        // Fetch arguments from bundle and set title
        award = (Award) getArguments().getSerializable("award");
        isAchieved = (Boolean) getArguments().getBoolean("isAchieved");
        getDialog().setTitle(award.getName());

        if (award.getIcon() != null) {
            Glide.with(view.getContext()).load(award.getIcon().getUrl()).into(ivAwardImage);
        }

        tvAwardName.setText(award.getName());
        tvAwardDescription.setText(award.getDescription());

        if (!isAchieved) {
            ivAwardImage.setColorFilter(getContext().getResources().getColor(R.color.grey));
        }
    }
}
