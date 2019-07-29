package com.example.bettertogether.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Invitation")
public class Invitation extends ParseObject {
    public static final String GROUP = "group";
    public static final String INVITER = "inviter";
    public static final String RECEIVER = "receiver";
    public static final String ACCEPTED = "accepted";

    public Group getGroup() {
        return (Group) getParseObject(GROUP);
    }

    public void setGroup(Group group) {
        put(GROUP, group);
    }

    public ParseUser getInviter() {
        return getParseUser(INVITER);
    }

    public void setInviter(ParseUser parseUser) {
        put(INVITER, parseUser);
    }

    public ParseUser getReceiver() {
        return getParseUser(RECEIVER);
    }

    public void setReceiver(ParseUser parseUser) {
        put(RECEIVER, parseUser);
    }

    public String getAccepted() {
        return getString(ACCEPTED);
    }

    public void setAccepted(String accepted) {
        put(ACCEPTED, accepted);
    }

}
