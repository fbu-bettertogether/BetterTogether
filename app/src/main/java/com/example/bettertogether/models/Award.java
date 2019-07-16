package com.example.bettertogether.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

@ParseClassName("Award")
public class Award extends ParseObject {
    public static final String KEY_DESCRIPTION = "description";
    public static final String NAME = "name";
    public static final String ICON = "icon";
    public static final String CATEGORY = "category";
    public static final String TYPE = "type";

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

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseObject getCategory() {
        return getParseObject(CATEGORY);
    }

    public void setCategory(Category category) {
        put(CATEGORY, category);
    }

    public String getType() {
        return getString(TYPE);
    }

    public void setType(String type) {
        put(TYPE, type);
    }

}
