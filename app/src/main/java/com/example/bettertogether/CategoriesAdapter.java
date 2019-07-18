package com.example.bettertogether;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bettertogether.models.Category;
import com.example.bettertogether.models.Group;
import com.parse.ParseFile;

import java.io.File;
import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private List<Group> mItems;
    Context mContext;
    public CategoriesAdapter(Context context,List<Group> objects) {
        mContext = context;
        mItems = objects;

    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        public  ImageView mImageView;
        public  TextView mTextView;
        public View rootView;
        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            mImageView =(ImageView)itemView.findViewById(R.id.image);
            mTextView =(TextView)itemView.findViewById(R.id.title);
        }
    }

    @Override
    public int getItemCount() {

        return mItems.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Group group = mItems.get(position);

//		Picasso.with(mContext).load(item.getImageUri()).into(holder.mImageView);
//		int drawableResourceId = mContext.getResources().getIdentifier(item.getImageUri(), "drawable", mContext.getPackageName());
        Glide.with(mContext).load(group.getIcon()).into(holder.mImageView);
        holder.mTextView.setText(group.getCategory());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int arg1) {
        LayoutInflater inflater =
                (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View convertView = inflater.inflate(R.layout.activity_discovery, parent, false);
        return new ViewHolder(convertView);
    }

}
