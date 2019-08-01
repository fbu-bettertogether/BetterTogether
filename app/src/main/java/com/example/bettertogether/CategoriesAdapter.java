package com.example.bettertogether;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bettertogether.fragments.GroupFragment;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    //private static List<Group> mItems;
    private List<Group> mItems;
    //static Context mContext;
    Context mContext;
    public CategoriesAdapter(Context context,List<Group> objects) {
        mContext = context;
        mItems = objects;

    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivGroupProf;
        public TextView tvGroupName;
        public TextView tvNumMembers;
        public View rootView;

        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            ivGroupProf =(ImageView)itemView.findViewById(R.id.ivGroupIcon);
            tvGroupName =(TextView)itemView.findViewById(R.id.tvGroupName);
            tvNumMembers = (TextView) itemView.findViewById(R.id.tvNumPeople);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                // get the clicked-on group
                Group group = mItems.get(position);
                // switch to group-detail view fragment
                FragmentManager fragmentManager = ((AppCompatActivity) mContext).getSupportFragmentManager();
                GroupFragment fragment = GroupFragment.newInstance(group);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            }
        }
    }

    @Override
    public int getItemCount() {

        return mItems.size();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        Group group = mItems.get(position);
        if (group.getIcon() != null) {
            Glide.with(mContext)
                    .load(group.getIcon().getUrl())
                    .into(holder.ivGroupProf);
        }
        holder.tvGroupName.setText(group.getName());

            ParseQuery<Membership> parseQuery = new ParseQuery<Membership>(Membership.class);
            parseQuery.whereEqualTo("group", group);
            parseQuery.findInBackground(new FindCallback<Membership>() {
                @Override
                public void done(List<Membership> objects, ParseException e) {
                    holder.tvNumMembers.setText(String.format("%s members", String.valueOf(objects.size())));
                }
                }
            );


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int arg1) {
        LayoutInflater inflater =
                (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View convertView = inflater.inflate(R.layout.item_group_in_discovery, parent, false);
        return new ViewHolder(convertView);
    }

}
