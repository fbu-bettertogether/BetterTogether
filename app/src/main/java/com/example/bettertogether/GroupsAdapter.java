package com.example.bettertogether;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.bettertogether.fragments.GroupFragment;
import com.example.bettertogether.models.Group;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {

    private Context context;
    private List<Group> groups;

    public GroupsAdapter(Context context, List<Group> groups) {
        this.context = context;
        this.groups = groups;
    }

    @NonNull
    @Override
    public GroupsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view);
    }

    // bind the data into the viewholder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get group at the current position
        Group group = groups.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }


    public void clear() {
        groups.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView ivGroupProf;
        private ImageView ivBadgeIcon;
        private ImageView ivBadgeInnerIcon;
        private TextView tvGroupName;
        private TextView tvCategory;
        private TextView tvDescription;
        private TextView tvDates;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // lookup widgets by id
            ivGroupProf = itemView.findViewById(R.id.ivGroupProf);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDates = itemView.findViewById(R.id.tvDates);
            ivBadgeInnerIcon = itemView.findViewById(R.id.ivBadgeInnerIcon);
            ivBadgeIcon = itemView.findViewById(R.id.ivBadgeIcon);

            itemView.setOnClickListener(this);
        }


        public void bind(Group group) {

            tvGroupName.setText(group.getName());

            // loading in the rest of the group fields if they are available
//            if (group.getCategory() != null) {
//                tvCategory.setText(group.getCategory());
//            } else {
                tvCategory.setText("");
//            }

            if (group.getDescription() != null) {
                tvDescription.setText(group.getDescription());
            } else {
                tvDescription.setText(String.format("We set aside time to meet our goals %d times every week!", group.getFrequency()));
            }

            if (group.getIcon() != null) {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));
                Glide.with(context)
                        .load(group.getIcon().getUrl())
                        .apply(requestOptions)
                        .into(ivGroupProf);
            }

            if (group.getShowCheckInReminderBadge()) {
                ivBadgeIcon.setVisibility(View.VISIBLE);
                ivBadgeInnerIcon.setVisibility(View.VISIBLE);
            } else {
                ivBadgeIcon.setVisibility(View.INVISIBLE);
                ivBadgeInnerIcon.setVisibility(View.INVISIBLE);
            }

            String startDateUgly = group.getStartDate();
            String endDateUgly = group.getEndDate();

            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");
            Date start = null;
            Date end = null;
            Calendar cal = Calendar.getInstance();
            try {
                start = sdf.parse(startDateUgly);
                cal.add(Calendar.DATE, group.getNumWeeks() * 7);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            try {
                end = sdf.parse(endDateUgly);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            Date currentDate = Calendar.getInstance().getTime();

            Date now = Calendar.getInstance().getTime();
            final boolean nowBeforeStart = now.before(start);

            long diffInMillis = 0;
            if (group.getIsActive()) {
                diffInMillis = end.getTime() - now.getTime();
            } else if (nowBeforeStart) {
                diffInMillis = start.getTime() - now.getTime();
            } else {
                diffInMillis = end.getTime() - now.getTime();
            }

            long diff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
            int weekDiff = (int) diff / 7;
            String unit = "weeks";
            int time = weekDiff;

            if (weekDiff == 0) {
                unit = "days";
                time = (int) diff;
            }

            if(group.getIsActive()) {
                tvDates.setText(String.format("%d %s left", time, unit));
                tvDates.setTextColor(ContextCompat.getColor(context, R.color.orange));
            } else if (currentDate.before(start)){
                tvDates.setText(String.format("starts in %d %s", time, unit));
                tvDates.setTextColor(ContextCompat.getColor(context, R.color.design_default_color_on_secondary));
            } else if (currentDate.after(cal.getTime())) {
                tvDates.setText(String.format("completed %d %s ago", time, unit));
                tvDates.setTextColor(ContextCompat.getColor(context, R.color.design_default_color_on_secondary));
            }
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                // get the clicked-on group
                Group group = groups.get(position);
                // switch to group-detail view fragment
                FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                GroupFragment fragment = GroupFragment.newInstance(group);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            }
        }
    }
}
