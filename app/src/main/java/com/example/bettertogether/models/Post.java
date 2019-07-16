package com.example.bettertogether.models;

import android.text.format.DateUtils;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_DESCRIPTION = "description";
    public static final String MEDIA = "postMedia";
    public static final String KEY_USER = "user";
    public static final String GROUP = "group";
    public static final String TAGGED_USERS = "taggedUsers";
    public static final String LIKES = "likes";


    public ParseFile getMedia() {
        return getParseFile(MEDIA);
    }

    public void setMedia(ParseFile parseFile) {
        put(MEDIA, parseFile);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public String getDate() {
        return String.valueOf(getCreatedAt());
    }


    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser parseUser) {
        put(KEY_USER, parseUser);
    }

    public ParseRelation<Like> getLikes() {
        return getRelation(LIKES);

    }

    public void setLikes(ParseRelation likes) {
        put(LIKES, likes);
    }

    public ParseObject getGroup() {
        return getParseObject(GROUP);
    }

    public void setGroup(Group group) {
        put(GROUP, group);
    }

    public ParseRelation<ParseUser> getTaggedUsers() {
        return getRelation(TAGGED_USERS);

    }

    public void setTaggedUsers(ParseRelation taggedUsers) {
        put(TAGGED_USERS, taggedUsers);
    }

    public static class Query extends ParseQuery<Post> {
        public Query() {
            super(Post.class);
        }

        public Query getTop() {
            setLimit(20);
            return this;
        }

        public Query withUser() {
            include("user");

            return this;
        }

    }

    public String getRelativeTimeAgo(Date date) {
        String relativeDate = "";
        long dateMillis = date.getTime();
        relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();

        return relativeDate;
    }
}
