package com.example.bettertogether.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Group;
import com.parse.ParseFile;
import com.parse.ParseUser;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnGroupFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupFragment extends Fragment {
    private static final String ARG_GROUP = "group";
    private Group group;
    private OnGroupFragmentInteractionListener mListener;

    private ImageView ivBanner;
    private ImageView ivUserIcon;
    private TextView tvGroupName;
    private TextView tvCreatePost;
    private Button btnCheckIn;
    private RecyclerView rvTimeline;


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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivBanner = view.findViewById(R.id.ivBanner);
        ivUserIcon = view.findViewById(R.id.ivUserIcon);
        tvGroupName = view.findViewById(R.id.tvGroupName);
        tvCreatePost = view.findViewById(R.id.tvCreatePost);
        btnCheckIn = view.findViewById(R.id.btnCheckIn);
        rvTimeline = view.findViewById(R.id.rvTimeline);

        if (group.getBanner() != null) {
            Glide.with(view.getContext()).load(group.getBanner().getUrl()).into(ivBanner);
        }
        if (ParseUser.getCurrentUser().getParseFile("profileImage") != null) {
            Glide.with(view.getContext()).load(ParseUser.getCurrentUser().getParseFile("profileImage").getUrl()).apply(RequestOptions.circleCropTransform()).into(ivUserIcon);
        }
        tvGroupName.setText(group.getName());
        btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Check in button", "Success");
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGroupFragmentInteractionListener) {
            mListener = (OnGroupFragmentInteractionListener) context;
        }
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
