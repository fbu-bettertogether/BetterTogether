package com.example.bettertogether.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ParseClassName("UserAward")
public class UserAward extends ParseObject {
    public static final String USER = "user";
    public static final String AWARD = "award";
    public static final String ACHIEVED = "achieved";
    public static final String NUM = "numCompleted";
    public static final String NUM_REQ = "numRequired";


    public Award getAward() {
        return (Award) getParseObject(AWARD);
    }

    public void setAward(Award award) {
        put(AWARD, award);
    }

    public ParseUser getUser() {
        return (ParseUser)getParseObject(USER);
    }

    public void setUser(ParseUser user) {
        put(USER, user);
    }

    public static List<Award> getAllAwards(List<UserAward> userAwards) {
        List<Award> awards = new ArrayList<>();
        for (int i = 0; i < userAwards.size(); i++) {
            awards.add(userAwards.get(i).getAward());
        }
        return awards;
    }

    public static List<ParseUser> getAllUsers(List<UserAward> userAwards) {
        List<ParseUser> users = new ArrayList<>();
        for (int i = 0; i < userAwards.size(); i++) {
            users.add(userAwards.get(i).getUser());
        }
        return users;
    }

    public Boolean getIfAchieved() {
        return getBoolean(ACHIEVED);
    }

    public void setIfAchieved(Boolean ach) { put(ACHIEVED, ach); }

    public Date getDateCompleted() {
        if (getIfAchieved()) {
            return getUpdatedAt();
        } else {
            return null;
        }
    }

    public void setNumCompleted(int num) { put(NUM, num); }

    public int getNumCompleted() {
        return getInt(NUM);
    }

    public void setNumRequired(int num) {
        put(NUM_REQ, num);
    }

    public int getNumRequired() {
        return getInt(NUM_REQ);
    }
}

