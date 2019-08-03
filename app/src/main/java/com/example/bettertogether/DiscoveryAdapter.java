package com.example.bettertogether;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bettertogether.models.Category;
import com.example.bettertogether.models.Group;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.List;

public class DiscoveryAdapter extends RecyclerView.Adapter<DiscoveryAdapter.ViewHolder> {

    public static final String LOGTAG ="carousels";
    private List<List<Group>> mRows;
    Context mContext;
    public DiscoveryAdapter(List<List<Group>> objects,Context context) {
        mContext = context;
        mRows = objects;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public RecyclerView mRecyclerViewRow;
        public TextView tvCatName;
        public ViewHolder(View itemView) {
            super(itemView);
            mRecyclerViewRow =(RecyclerView)itemView.findViewById(R.id.recyclerView_row);
            tvCatName = (TextView)itemView.findViewById(R.id.tvCategoryName);
        }
    }

    @Override
    public int getItemCount() {
        return mRows.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        List<Group> RowItems = mRows.get(position);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext,LinearLayoutManager.HORIZONTAL,false);
        holder.mRecyclerViewRow.setLayoutManager(layoutManager);
        holder.mRecyclerViewRow.setHasFixedSize(true);
        CategoriesAdapter rowsRecyclerAdapter = new CategoriesAdapter(mContext, RowItems);
        holder.mRecyclerViewRow.setAdapter(rowsRecyclerAdapter);
        if (RowItems != null && RowItems.size() > 0) {
            try {
                holder.tvCatName.setText((String)((Category)RowItems.get(0).get("category")).fetch().get("name"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        final RecyclerView finalRecyclerView = holder.mRecyclerViewRow;

        finalRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged (RecyclerView recyclerView, int newState){

                switch(newState){

                    case RecyclerView.SCROLL_STATE_IDLE:

                        float targetBottomPosition1 = finalRecyclerView.getX();
                        float targetBottomPosition2 = finalRecyclerView.getX() + finalRecyclerView.getWidth();

                        Log.i(LOGTAG,"targetBottomPosition1 = " + targetBottomPosition1);
                        Log.i(LOGTAG,"targetBottomPosition2 = " + targetBottomPosition2);

                        View v1 = finalRecyclerView.findChildViewUnder(targetBottomPosition1,0);
                        View v2 = finalRecyclerView.findChildViewUnder(targetBottomPosition2,0);

                        float x1 = targetBottomPosition1;
                        if(v1!=null){
                            x1 =v1.getX();
                        }

                        float x2 = targetBottomPosition2;
                        if(v2!=null){
                            x2 =v2.getX();
                        }

                        Log.i(LOGTAG,"x1 = " + x1);
                        Log.i(LOGTAG,"x2 = " + x2);

                        float dx1 = Math.abs(finalRecyclerView.getX()-x1 );
                        float dx2 = Math.abs(finalRecyclerView.getX()+ finalRecyclerView.getWidth()-x2);

                        Log.i(LOGTAG,"dx1 = " + dx1);
                        Log.i(LOGTAG,"dx2 = " + dx2);

                        float visiblePortionOfItem1 = 0;
                        float visiblePortionOfItem2 = 0;

                        if(x1<0 && v1 != null){
                            visiblePortionOfItem1 = v1.getWidth() - dx1;
                        }

                        if(v2 != null){
                            visiblePortionOfItem2 = v2.getWidth() - dx2;
                        }

                        Log.i(LOGTAG,"visiblePortionOfItem1 = " + visiblePortionOfItem1);
                        Log.i(LOGTAG,"visiblePortionOfItem2 = " + visiblePortionOfItem2);

                        int position = 0;
                        if(visiblePortionOfItem1>=visiblePortionOfItem2){
                            position = finalRecyclerView.getChildAdapterPosition(finalRecyclerView.findChildViewUnder(targetBottomPosition1,0));
                        }else{

                            position = finalRecyclerView.getChildAdapterPosition(finalRecyclerView.findChildViewUnder(targetBottomPosition2,0));
                        }
                        finalRecyclerView.scrollToPosition(position);
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
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int arg1) {
        LayoutInflater inflater =
                (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View convertView = inflater.inflate(R.layout.fragment_category, parent, false);
        return new ViewHolder(convertView);
    }

}