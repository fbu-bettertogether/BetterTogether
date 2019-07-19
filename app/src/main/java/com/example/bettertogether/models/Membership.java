package com.example.bettertogether.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("Membership")
public class Membership extends ParseObject {
    public static final String KEY_USER = "user";
    public static final String GROUP = "group";
    public static final String KEY_NUM_CHECK_INS = "numCheckIns";


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

    public void setNumCheckIns(int i) {
        put(KEY_NUM_CHECK_INS, i);
    }

    public int getNumCheckIns() {
        return getInt(KEY_NUM_CHECK_INS);
    }

}

