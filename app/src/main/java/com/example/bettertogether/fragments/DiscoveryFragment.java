package com.example.bettertogether.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bettertogether.DiscoveryAdapter;
import com.example.bettertogether.GroupsAdapter;
import com.example.bettertogether.R;
import com.example.bettertogether.models.Category;
import com.example.bettertogether.models.Group;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import static com.example.bettertogether.models.Group.CATEGORY;
import static com.example.bettertogether.models.Group.ICON;
import static com.example.bettertogether.models.Group.KEY_DESCRIPTION;
import static com.example.bettertogether.models.Group.NAME;
import static com.parse.ParseUser.getCurrentUser;

import static com.parse.ParseUser.getCurrentUser;

public class DiscoveryFragment extends Fragment {
    public static final String LOGTAG ="carousels";

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    boolean scrolling;
    private RecyclerView rvDiscovery;
    private DiscoveryAdapter adapter;
    private List<Category> mCategories;
    private ProgressBar progressBar = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_discovery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView)view.findViewById(R.id.recyclerview_rootview);
        mLayoutManager = new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        List<List<Group>> listOfListOfItems = new ArrayList<List<Group>>();

        int numOfRows = 1;
        int numOfColumns = 1;
        for(int i = 0 ; i<numOfRows ; i++){
            List<Group> listOfItems = new ArrayList<Group>();
            for(int j = 0 ; j<numOfColumns ; j++){
                Group item = new Group();
                listOfItems.add(item);
            }
            listOfListOfItems.add(listOfItems);
        }

        DiscoveryAdapter mainRecyclerAdapter = new DiscoveryAdapter(listOfListOfItems, getContext());
        mRecyclerView.setAdapter(mainRecyclerAdapter);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged (RecyclerView recyclerView, int newState){

                switch(newState){

                    case RecyclerView.SCROLL_STATE_IDLE:

                        Log.i(LOGTAG,"X = " + (mRecyclerView.getX() + mRecyclerView.getWidth() )+ " and Y = " + (mRecyclerView.getY()+ mRecyclerView.getHeight()));
                        float targetBottomPosition1 = mRecyclerView.getY();
                        float targetBottomPosition2 = mRecyclerView.getY() + mRecyclerView.getHeight();

                        Log.i(LOGTAG,"targetBottomPosition1 = " + targetBottomPosition1);
                        Log.i(LOGTAG,"targetBottomPosition2 = " + targetBottomPosition2);

                        View v1 = mRecyclerView.findChildViewUnder(500, targetBottomPosition1);
                        View v2 = mRecyclerView.findChildViewUnder(500, targetBottomPosition2);

                        float y1 = targetBottomPosition1;
                        if(v1!=null){
                            y1 =v1.getY();
                        }

                        float y2 = targetBottomPosition2;
                        if(v2!=null){
                            y2 =v2.getY();
                        }

                        Log.i(LOGTAG,"y1 = " + y1);
                        Log.i(LOGTAG,"y2 = " + y2);

                        float dy1 = Math.abs(y1-mRecyclerView.getY() );
                        float dy2 = Math.abs(y2-(mRecyclerView.getY()+ mRecyclerView.getHeight()));

                        Log.i(LOGTAG,"dy1 = " + dy1);
                        Log.i(LOGTAG,"dy2 = " + dy2);

                        float visiblePortionOfItem1 = 0;
                        float visiblePortionOfItem2 = 0;

                        if(y1<0 && v1 != null){
                            visiblePortionOfItem1 = v1.getHeight() - dy1;
                        }

                        if(v2 != null){
                            visiblePortionOfItem2 = v2.getHeight() - dy2;
                        }


                        int position = 0;
                        if(visiblePortionOfItem1<=visiblePortionOfItem2){
                            position = mRecyclerView.getChildPosition(mRecyclerView.findChildViewUnder(500, targetBottomPosition1));
                        }else{

                            position = mRecyclerView.getChildPosition(mRecyclerView.findChildViewUnder(500, targetBottomPosition2));
                        }
                        mRecyclerView.scrollToPosition(position);

                        break;

                    case RecyclerView.SCROLL_STATE_DRAGGING:

                        break;

                    case RecyclerView.SCROLL_STATE_SETTLING:

                        break;

                }
            }

            @Override
            public void onScrolled (RecyclerView recyclerView, int dx, int dy){

//				Log.i(LOGTAG,"X = " + dx + " and Y = " + dy);
            }
        });

//        for (int i = 0; i < mCategories.size(); i++) {
//            queryCategories(mCategories.get(i));
//        }
    }

    private void queryCategories(Category cat) {
        cat.getRelation("groups").getQuery().findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> categories, ParseException e) {
                if (e != null) {
                    Log.e("Querying groups", "error with query");
                    e.printStackTrace();
                    return;
                }

                Log.d("carmel",Integer.toString(categories.size()));

                // add posts to list and update view with adapter
                mCategories.addAll((List<Category>) (Object) categories);
                adapter.notifyDataSetChanged();
            }
        });
    }
    private void queryGroups(Category cat) {
        cat.getRelation("groups").getQuery().findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> categories, ParseException e) {
                if (e != null) {
                    Log.e("Querying groups", "error with query");
                    e.printStackTrace();
                    return;
                }

                Log.d("carmel",Integer.toString(categories.size()));

                // add posts to list and update view with adapter
                mCategories.addAll((List<Category>) (Object) categories);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
