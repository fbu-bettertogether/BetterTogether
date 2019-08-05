package com.example.bettertogether.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@ParseClassName("Membership")
public class Membership extends ParseObject {
    public static final String KEY_USER = "user";
    public static final String GROUP = "group";
    public static final String KEY_NUM_CHECK_INS = "numCheckIns";
    public static final String KEY_POINTS = "points";
    public static final String LOCATION = "location";
    public static final String LAST_CHECK_IN = "lastCheckIn";

    public String getDate() {
        return String.valueOf(getCreatedAt());
    }

    public Group getGroup() {
        return (Group) getParseObject(GROUP);
    }

    public void setGroup(Group group) {
        put(GROUP, group);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser parseUser) {
        put(KEY_USER, parseUser);
    }

    public static List<Group> getAllGroups(List<Membership> memberships) {
        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < memberships.size(); i++) {
            groups.add(memberships.get(i).getGroup());
        }
        return  groups;
    }

    public static List<ParseUser> getAllUsers(List<Membership> memberships) {
        List<ParseUser> users = new ArrayList<>();
        for (int i = 0; i < memberships.size(); i++) {
            users.add(memberships.get(i).getUser());
        }
        return users;
    }

    public void setNumCheckIns(List i) {
        put(KEY_NUM_CHECK_INS, i);
    }

    public List<Integer> getNumCheckIns() {
        return getList(KEY_NUM_CHECK_INS);
    }

    public void setPoints(int i) {
        put(KEY_POINTS, i);
    }

    public int getPoints() {
        return getInt(KEY_POINTS);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(LOCATION);
    }

    public void setLocation(ParseGeoPoint location) {
        put(LOCATION, location);
    }

    public Date getLastCheckIn() {
        String str = getString(LAST_CHECK_IN);
        if (str == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            return cal.getTime();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");
        Date lastCheckIn = null;
        try {
            lastCheckIn = sdf.parse(str);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return lastCheckIn;
    }

    public void setLastCheckIn(Date date) {
        put(LAST_CHECK_IN, date.toString());
    }

}

