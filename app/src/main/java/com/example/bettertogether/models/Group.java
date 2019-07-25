package com.example.bettertogether.models;

import android.os.Parcelable;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.xml.sax.Parser;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;

@ParseClassName("Group")
public class Group extends ParseObject implements Serializable {
    public static final String KEY_OWNER = "owner";
    public static final String KEY_DESCRIPTION = "description";
    public static final String BANNER = "banner";
    public static final String NAME = "name";
    public static final String ICON = "icon";
    public static final String CATEGORY = "category";
    public static final String TOTAL_POINTS = "totalPoints";
    public static final String USERS_TO_POINTS = "usersToPoint";
    public static final String PRIVACY = "privacy";
    public static final String FREQUENCY = "frequency";
    public static final String IS_ACTIVE = "isActive";
    public static final String LOCATION = "location";
    public static final String USERS = "users";
    public static final String NUM_CHECK_INS = "numCheckIns";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String MIN_TIME = "minTime";
    public static final String NUM_WEEKS = "numWeeks";

    public ParseUser getOwner() {
        return getParseUser(KEY_OWNER);
    }

    public void setOwner(ParseUser parseUser) {
        put(KEY_OWNER, parseUser);
    }

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

    public String getCategory() {
        ParseObject cat = getParseObject(CATEGORY);
        return cat.getString(NAME);
    }

    public void setCategory(String catId) {
        put(CATEGORY, ParseObject.createWithoutData("Category", catId) );
        saveInBackground();
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

    public int getFrequency() {
        return getInt(FREQUENCY);
    }

    public void setFrequency(int frequency) {
        put(FREQUENCY, frequency);
    }

    public String getPrivacy() {
        return getString(PRIVACY);
    }

    public void setPrivacy(String privacy) {
        put(PRIVACY, privacy);
    }

    public String getStartDate() {
        return getString(START_DATE);
    }

    public void setStartDate(String startDate) { put(START_DATE, startDate); }

    public String getEndDate() { return getString(END_DATE); }

    public void setEndDate(String endDate) {
        put(END_DATE, endDate);
    }

    public Boolean getIsActive() {
        return getBoolean(IS_ACTIVE);
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

    public int getNumUsers() throws ParseException {
        return getRelation(USERS).getQuery().count();
    }

    public void setUsers(ParseRelation users) {
        put(USERS, users);
    }

    public String getDate() {
        return String.valueOf(getCreatedAt());
    }

    public void setMinTime(int minTime) {
        put(MIN_TIME, minTime);
    }

    public int getMinTime() {
        return getInt(MIN_TIME);
    }

    public int getNumWeeks() {
        return getInt(NUM_WEEKS);
    }

    public void setNumWeeks(int weeks) {
        put(NUM_WEEKS, weeks);
    }

    }
