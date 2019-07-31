package com.example.bettertogether.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bettertogether.R;
import com.example.bettertogether.models.CatMembership;
import com.example.bettertogether.models.Category;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardFragment extends Fragment {

    private boolean isQueryComplete = false;
    private OnFragmentInteractionListener mListener;

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
                Map<String, Integer> groupsToPoints = new HashMap<>();
                Map<String, Group> idsToGroups = new HashMap<>();
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
        View view = getView();
        BarChart chart = view.findViewById(R.id.barchart);
        ArrayList<Bitmap> imageList = new ArrayList<>();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground);

        int i = 0;
        final ArrayList<String> xLabels = new ArrayList<>();
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Group> element : idsToGroups.entrySet()) {
            if (1 < groupsToPoints.get(element.getKey())) {
                entries.add(new BarEntry(i++, groupsToPoints.get(element.getKey())));
                xLabels.add(element.getValue().getName());
                imageList.add(bitmap);
            }
        }
        BarDataSet dataSet = new BarDataSet(entries, "Groups");
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return xLabels.get((int) value);
            }
        });
        dataSet.setBarBorderWidth(0.9f);
        List<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.colorPrimary));
        colors.add(getResources().getColor(R.color.o4));
        colors.add(getResources().getColor(R.color.o8));
        colors.add(getResources().getColor(R.color.colorPrimaryDark));
        dataSet.setColors(colors);
        BarData data = new BarData(dataSet);
        chart.setData(data);
        chart.setDrawBarShadow(true);
        chart.animateXY(2000, 2000);
        chart.invalidate();

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

    public class DayAxisValueFormatter extends ValueFormatter {
        private final BarLineChartBase<?> chart;

        public DayAxisValueFormatter(BarLineChartBase<?> chart) {
            this.chart = chart;
        }

        @Override
        public String getFormattedValue(float value) {
            return "your text " + value;
        }
    }

}
