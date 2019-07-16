package com.example.bettertogether.models;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.Dictionary;

@ParseClassName("Group")
public class Group extends ParseObject {
    public static final String KEY_DESCRIPTION = "description";
    public static final String BANNER = "banner";
    public static final String NAME = "name";
    public static final String ICON = "icon";
    public static final String CATEGORY = "category";
    public static final String TOTAL_POINTS = "totalPoints";
    public static final String USERS_TO_POINTS = "usersToPoint";
    public static final String PRIVACY = "privacy";
    public static final String IS_ACTIVE = "isActive";
    public static final String LOCATION = "location";
    public static final String USERS = "users";
    public static final String NUM_CHECK_INS = "numCheckIns";


    public ParseFile getBanner() {
        return getParseFile(BANNER);
    }

    public void setBanner(ParseFile parseFile) {
        put(BANNER, parseFile);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseFile getIcon() {
        return getParseFile(ICON);
    }

    public void setIcon(ParseFile parseFile) {
        put(ICON, parseFile);
    }

    public String getName() {
        return getString(NAME);
    }

    public void setName(String name) {
        put(NAME, name);
    }

    public ParseObject getCategory() {
        return getParseObject(CATEGORY);
    }

    public void setCategory(Category category) {
        put(CATEGORY, category);
    }

    public int getTotalPoints() {
        return getInt(TOTAL_POINTS);
    }

    public void setTotalPoints(int totalPoints) {
        put(TOTAL_POINTS, totalPoints);
    }

    public ParseObject getUsersToPoints() {
        return getParseObject(USERS_TO_POINTS);
    }

    public void setUsersToPoints(Dictionary userToPoints) {
        put(USERS_TO_POINTS, userToPoints);
    }

    public String getPrivacy() {
        return getString(PRIVACY);
    }

    public void setPrivacy(String privacy) {
        put(PRIVACY, privacy);
    }

    public String getIsActive() {
        return getString(IS_ACTIVE);
    }

    public void setIsActive(boolean isActive) {
        put(IS_ACTIVE, isActive);
    }
    public ParseObject getLocation() {
        return getParseObject(LOCATION);
    }

    public void setLocation(ParseObject location) {
        put(LOCATION, location);
    }

    public ParseRelation<ParseUser> getUsers() {
        return getRelation(USERS);

    }

    public void setUsers(ParseRelation users) {
        put(USERS, users);
    }

    public ParseObject getNumCheckIns() {
        return getParseObject(NUM_CHECK_INS);
    }
    public void setNumCheckIns(ParseObject numCheckIns) {
        put(NUM_CHECK_INS, numCheckIns);
    }

    public String getDate() {
        return String.valueOf(getCreatedAt());
    }


    }
