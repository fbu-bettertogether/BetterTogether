package com.example.bettertogether.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

@ParseClassName("Category")
public class Category extends ParseObject {
    public static final String NAME = "name";
    public static final String GROUPS = "groups";
    public static final String ICON = "icon";

    public static class Query extends ParseQuery<Category> {
        public Query() {
            super(Category.class);
        }

        public Query getTop() {
            setLimit(20);
            return this;
        }
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

    public ParseRelation<ParseObject> getGroups() {
        return getRelation(GROUPS);
    }

    public void setGroups(ParseRelation groups) {
        put(GROUPS, groups);
    }

}
