package com.example.bettertogether;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bettertogether.models.Invitation;
import com.parse.ParseFile;

import java.util.List;

public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.ViewHolder> {

    InvitationAdapter.ViewHolder viewHolder;
    private List<Invitation> invitations;
    private List<Invitation> taggedInvitations;

    private Context context;

    public InvitationAdapter(List<Invitation> invitations, List<Invitation> taggedInvitations) {
        this.invitations = invitations;
        this.taggedInvitations = taggedInvitations;

    }

    @NonNull
    @Override
    public InvitationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.member_layout, parent, false);
        viewHolder = new InvitationAdapter.ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final InvitationAdapter.ViewHolder holder, int position) {
        // get the data according to position

        final Invitation invitation = invitations.get(position);
        holder.ivTag.setImageDrawable(holder.ivTag.getContext().getDrawable(R.drawable.thumb_up));
        holder.tvUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (taggedInvitations.contains(invitation)) {
                    taggedInvitations.remove(invitation);
                    holder.ivTag.setVisibility(View.INVISIBLE);
                } else {
                    taggedInvitations.add(invitation);
                    holder.ivTag.setVisibility(View.VISIBLE);
                }
            }
        });

        if (invitation.getGroup() == null) {
            holder.tvUsername.setText(invitation.getInviter().getString("username"));
            if (invitation.getInviter().get("profileImage") != null) {
                Glide.with(context).load(((ParseFile) invitation.getInviter().get("profileImage")).getUrl()).apply(RequestOptions.circleCropTransform()).into(holder.ivUserIcon);
            }
        } else {
            holder.tvUsername.setText(invitation.getGroup().getName());
            if (invitation.getGroup().getIcon() != null) {
                Glide.with(context).load(((ParseFile) invitation.getGroup().getIcon()).getUrl()).apply(RequestOptions.circleCropTransform()).into(holder.ivUserIcon);
            }
        }
    }

    @Override
    public int getItemCount() {
        return invitations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivUserIcon;
        public ImageView ivTag;
        public TextView tvUsername;

        public ViewHolder(View itemView) {
            super(itemView);
            ivUserIcon = itemView.findViewById(R.id.ivUserIcon);
            ivTag = itemView.findViewById(R.id.ivAdd);
            tvUsername = itemView.findViewById(R.id.tvUsername);

        }

    }

}
