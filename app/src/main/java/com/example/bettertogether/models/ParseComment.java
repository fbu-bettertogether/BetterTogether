package com.example.bettertogether.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("ParseComment")
public class ParseComment extends ParseObject {

    public static final String KEY_TEXT = "text";
    public static final String KEY_USER = "user";
    public static final String POST = "post";
    public static final String REPLY_TO = "replyTo";

    public String getText() {
        return getString(KEY_TEXT);
    }

    public void setText(String description) {
        put(KEY_TEXT, description);
    }

    public String getDate() {
        return String.valueOf(getCreatedAt());
    }

    public ParseObject getPost() {
        return getParseObject(POST);
    }

    public void setPost(Post post) {
        put("post", post);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser parseUser) {
        put(KEY_USER, parseUser);
    }

    public ParseObject getReplyTo() {
        return getParseObject(REPLY_TO);
    }

    public void setReplyTo(ParseComment parseComment) {
        put(REPLY_TO, parseComment);
    }

}