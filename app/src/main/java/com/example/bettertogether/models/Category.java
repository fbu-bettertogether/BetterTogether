package com.example.bettertogether.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("Category")
public class Category extends ParseObject {
    public static final String NAME = "name";
    public static final String ICON = "icon";
    public static final String LOCATION_TYPES = "locationTypes";

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

    public JSONArray getLocationTypes() {
        return getJSONArray(LOCATION_TYPES);
    }
    public ArrayList<String> getLocationTypesList() throws JSONException {
        ArrayList<String> array = new ArrayList<>();
        JSONArray types = getLocationTypes();
        for (int i = 0; i < types.length(); i++) {
            array.add(types.getString(i));
        }
        return array;
    }

    public void setLocationTypes(JSONArray array) {
        put(LOCATION_TYPES, array);
    }

}
