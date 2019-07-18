package com.example.bettertogether;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bettertogether.models.Group;

import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private List<Group> mItems;
    Context mContext;
    public CategoriesAdapter(Context context,List<Group> objects) {
        mContext = context;
        mItems = objects;

    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        public  ImageView ivGroupProf;
        public  TextView tvGroupName;
        public View rootView;
        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            ivGroupProf =(ImageView)itemView.findViewById(R.id.ivGroupIcon);
            tvGroupName =(TextView)itemView.findViewById(R.id.tvGroupName);
        }
    }

    @Override
    public int getItemCount() {

        return mItems.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Group group = mItems.get(position);
        if (group.getIcon() != null) {
            Glide.with(mContext)
                    .load(group.getIcon().getUrl())
                    .into(holder.ivGroupProf);
        }
        holder.tvGroupName.setText(group.getName());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int arg1) {
        LayoutInflater inflater =
                (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View convertView = inflater.inflate(R.layout.item_group_in_discovery, parent, false);
        return new ViewHolder(convertView);
    }

}
