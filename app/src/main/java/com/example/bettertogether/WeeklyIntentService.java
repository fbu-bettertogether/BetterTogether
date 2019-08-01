package com.example.bettertogether;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.JobIntentService;

import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class WeeklyIntentService extends JobIntentService {

    List<Membership> memberships = new ArrayList<Membership>();
    int numWeeks;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, WeeklyIntentService.class, 1, work);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        if (intent != null) {
            Bundle bundle = intent.getBundleExtra("bundle");
            final Group group = (Group) bundle.getSerializable("group");
            ParseQuery<Membership> parseQuery = new ParseQuery<Membership>(Membership.class);
            parseQuery.whereEqualTo("group", group);
            parseQuery.include("group");
            parseQuery.include("user");
            parseQuery.findInBackground(new FindCallback<Membership>() {
                @Override
                public void done(List<Membership> objects, ParseException e) {
                    if (e != null) {
                        Log.e("Querying groups", "error with query");
                        e.printStackTrace();
                        return;
                    } else if (objects == null || objects.size() == 0) {
                        Log.e("membership querying", "no membership objects found with this group");
                    }

                    memberships.addAll(objects);
                    numWeeks = objects.get(0).getGroup().getNumWeeks();
                    if (memberships.get(0).getNumCheckIns().size() < numWeeks) {
                        for (int i = 0; i < memberships.size(); i++) {
                            final Membership currMember = memberships.get(i);
                            List<Integer> numCheckIns = currMember.getNumCheckIns();
                            numCheckIns.add(0);
                            currMember.setNumCheckIns(numCheckIns);
                            currMember.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        e.printStackTrace();
                                    } else {
                                        Log.d("alarm", "refreshing numCheckIns for " + currMember.getUser().getUsername() + ", " + currMember.getGroup().getName());
                                    }
                                }
                            });
                        }
                    } else {
                        ParseQuery<Group> groupQuery = new ParseQuery<Group>(Group.class);
                        groupQuery.whereEqualTo("objectId", group.getObjectId());
                        groupQuery.findInBackground(new FindCallback<Group>() {
                            @Override
                            public void done(List<Group> objects, ParseException e) {
                                Group group = objects.get(0);
                                group.setIsActive(false);
                                group.setShowCheckInReminderBadge(false);
                                group.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        Log.d("alarm", "changing activity of group");
                                    }
                                });
                            }
                        });
                        //TODO -- figure out how to cancel the alarms from within the service
                    }
                }
            });
        }
    }
}
