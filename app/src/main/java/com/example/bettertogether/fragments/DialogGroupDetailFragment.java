package com.example.bettertogether.fragments;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
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

import com.example.bettertogether.FriendAdapter;
import com.example.bettertogether.MemberAdapter;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
// ...

public class DialogGroupDetailFragment extends DialogFragment {

    private ImageView ivCatIcon;
    private RecyclerView rvMembers;
    private FriendAdapter adapter;
    private TextView tvDates;
    private TextView tvDescription;
    private ImageView ivClose;

    public DialogGroupDetailFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static DialogGroupDetailFragment newInstance(Group group) {
        DialogGroupDetailFragment frag = new DialogGroupDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("group", group);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_group_detail, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        ivCatIcon = view.findViewById(R.id.ivCatIcon);
        rvMembers = view.findViewById(R.id.rvMembers);
        tvDates = view.findViewById(R.id.tvDates);
        tvDescription = view.findViewById(R.id.tvDescription);
        ivClose = view.findViewById(R.id.ivClose);

        // Fetch arguments from bundle and set title
        Group group = (Group) getArguments().getSerializable("group");
        getDialog().setTitle(group.getName());

        switch (group.getCategory()) {
            case "Get-Togethers":
                ivCatIcon.setImageResource(R.drawable.sharp_people_black_18dp);
                break;
            case "Fitness":
                ivCatIcon.setImageResource(R.drawable.sharp_fitness_center_black_18dp);
                break;
            case "Service":
                ivCatIcon.setImageResource(R.drawable.sharp_local_florist_black_18dp);
                break;
        }
        ivCatIcon.setColorFilter(getResources().getColor(R.color.orange));

        String initialStartDate = group.getStartDate();
        String startDate = initialStartDate.substring(0, 10) + "," + initialStartDate.substring(23);
        String initialEndDate = group.getEndDate();
        String endDate = initialEndDate.substring(0, 10) + "," + initialEndDate.substring(23);
        tvDates.setText(endDate + " - " + startDate);
        tvDescription.setText(group.getDescription());

        // query for members
        ParseQuery<Membership> membersQuery = new ParseQuery<Membership>(Membership.class);
        membersQuery.whereEqualTo("group", group);
        membersQuery.include("user");
        membersQuery.findInBackground(new FindCallback<Membership>() {
            @Override
            public void done(List<Membership> objects, ParseException e) {
                List<ParseUser> members = Membership.getAllUsers(objects);
                adapter = new FriendAdapter(members, getFragmentManager());
                rvMembers.setAdapter(adapter);
                rvMembers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            }
        });

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
