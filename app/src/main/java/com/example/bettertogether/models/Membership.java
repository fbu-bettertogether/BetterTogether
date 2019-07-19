package com.example.bettertogether.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

@ParseClassName("Membership")
public class Membership extends ParseObject {
    public static final String KEY_USER = "user";
    public static final String GROUP = "group";


    public String getDate() {
        return String.valueOf(getCreatedAt());
    }

    public Group getGroup() {
        return (Group)getParseObject(GROUP);
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

}

