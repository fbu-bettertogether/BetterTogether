package com.example.bettertogether.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Like")
public class Like extends ParseObject {
    public static final String KEY_USER = "user";
    public static final String POST = "post";

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
}
