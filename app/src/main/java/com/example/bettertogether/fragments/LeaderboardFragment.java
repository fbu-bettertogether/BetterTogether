package com.example.bettertogether.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bettertogether.GraphAdapter;
import com.example.bettertogether.R;
import com.example.bettertogether.models.CatMembership;
import com.example.bettertogether.models.Category;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardFragment extends Fragment {

    private boolean isQueryComplete = false;
    private ImageView ivServicePoints;
    private ImageView ivFitnessPoints;
    private ImageView ivGetTogetherPoints;
    private RecyclerView rvChart;
    private GraphAdapter graphAdapter;
    private OnFragmentInteractionListener mListener;
    private ArrayList<Integer> entries;
    private ArrayList<ParseFile> imageEntries;
    Map<String, Integer> groupsToPoints;
    Map<String, Group> idsToGroups;


    public LeaderboardFragment() {
        // Required empty public constructor
    }

    public static LeaderboardFragment newInstance(String param1, String param2) {
        LeaderboardFragment fragment = new LeaderboardFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entries = new ArrayList<>();
        imageEntries = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvChart = view.findViewById(R.id.rvChart);
        graphAdapter = new GraphAdapter(entries, imageEntries);
        rvChart.setAdapter(graphAdapter);
        rvChart.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        ivFitnessPoints = view.findViewById(R.id.ivFitnessPoints);
        ivServicePoints = view.findViewById(R.id.ivServicePoints);
        ivGetTogetherPoints = view.findViewById(R.id.ivGetTogetherPoints);
        ivFitnessPoints.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                ivFitnessPoints.setImageTintList(getResources().getColorStateList(R.color.orange, getActivity().getTheme()));
                ivGetTogetherPoints.setImageTintList(getResources().getColorStateList(R.color.quantum_black_100, getActivity().getTheme()));
                ivServicePoints.setImageTintList(getResources().getColorStateList(R.color.quantum_black_100, getActivity().getTheme()));
                drawGraph(groupsToPoints, idsToGroups, "Fitness");

            }
        });
        ivGetTogetherPoints.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                ivGetTogetherPoints.setImageTintList(getResources().getColorStateList(R.color.orange, getActivity().getTheme()));
                ivFitnessPoints.setImageTintList(getResources().getColorStateList(R.color.quantum_black_100, getActivity().getTheme()));
                ivServicePoints.setImageTintList(getResources().getColorStateList(R.color.quantum_black_100, getActivity().getTheme()));
                drawGraph(groupsToPoints, idsToGroups, "Get-Togethers");

            }
        });
        ivServicePoints.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                ivServicePoints.setImageTintList(getResources().getColorStateList(R.color.orange, getActivity().getTheme()));
                ivFitnessPoints.setImageTintList(getResources().getColorStateList(R.color.quantum_black_100, getActivity().getTheme()));
                ivGetTogetherPoints.setImageTintList(getResources().getColorStateList(R.color.quantum_black_100, getActivity().getTheme()));
                drawGraph(groupsToPoints, idsToGroups, "Service");
            }
        });
        queryMembers();
    }

    public void queryGroups(Category category) {
        ParseQuery<CatMembership> catMembershipParseQuery = new ParseQuery<CatMembership>("CatMembership");
        catMembershipParseQuery.whereEqualTo("category", category);
        catMembershipParseQuery.include("group");
        catMembershipParseQuery.findInBackground(new FindCallback<CatMembership>() {
            @Override
            public void done(List<CatMembership> objects, ParseException e) {
                ArrayList<Group> groups = new ArrayList<>();
                for (int i = 0; i < objects.size(); i++) {
                    if (objects.get(i).getGroup().getIsActive()) {
                        groups.add(objects.get(i).getGroup());
                    }
                }
            }
        });
    }

    public void queryMembers() {
        ParseQuery<Membership> membershipParseQuery = new ParseQuery<Membership>("Membership");
        membershipParseQuery.setLimit(1000);
        membershipParseQuery.whereNotEqualTo("numCheckIns", new ArrayList<>());
        membershipParseQuery.include("group");
        membershipParseQuery.findInBackground(new FindCallback<Membership>() {
            @Override
            public void done(List<Membership> objects, ParseException e) {
                List<Group> groups = new ArrayList<>();
                groupsToPoints = new HashMap<>();
                idsToGroups = new HashMap<>();
                List<String> groupIds = new ArrayList<>();
                for (int i = 0; i < objects.size(); i++) {
                    if (!idsToGroups.containsKey(objects.get(i).getGroup().getObjectId())) {
                        idsToGroups.put(objects.get(i).getGroup().getObjectId(), objects.get(i).getGroup());
                        groupsToPoints.put(objects.get(i).getGroup().getObjectId(), sum(objects.get(i).getNumCheckIns()));
                    } else {
                        groupsToPoints.put(objects.get(i).getGroup().getObjectId(), groupsToPoints.get(objects.get(i).getGroup().getObjectId()) + sum(objects.get(i).getNumCheckIns()));
                    }
                }
                drawGraph(groupsToPoints, idsToGroups);
            }
        });

    }

    private void drawGraph(Map<String, Integer> groupsToPoints, Map<String, Group> idsToGroups) {
        int i = 0;
        final ArrayList<String> xLabels = new ArrayList<>();
        for (Map.Entry<String, Group> element : idsToGroups.entrySet()) {
            if (0 < groupsToPoints.get(element.getKey())) {
                entries.add(groupsToPoints.get(element.getKey()));
                imageEntries.add(element.getValue().getIcon());
                    i++;
            }
        }
        graphAdapter.notifyDataSetChanged();

    }

    private void drawGraph(Map<String, Integer> groupsToPoints, Map<String, Group> idsToGroups, String categoryName) {
        entries.clear();
        imageEntries.clear();
        int i = 0;
        final ArrayList<String> xLabels = new ArrayList<>();
        for (Map.Entry<String, Group> element : idsToGroups.entrySet()) {
            if (element.getValue().getCategory().equals(categoryName))
                if (0 < groupsToPoints.get(element.getKey())) {
                    entries.add(groupsToPoints.get(element.getKey()));
                    imageEntries.add(element.getValue().getIcon());
                    i++;
                }
        }
        graphAdapter.notifyDataSetChanged();

    }

    public int sum(List<Integer> list) {
        int sum = 0;
        for (int i = 0; i < list.size(); i++)
            sum += list.get(i);
        return sum;

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
