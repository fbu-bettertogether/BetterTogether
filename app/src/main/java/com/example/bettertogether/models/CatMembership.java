package com.example.bettertogether.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("CatMembership")
public class CatMembership extends ParseObject {
    public static final String CATEGORY = "category";
    public static final String GROUP = "group";


    public Group getGroup() {
        return (Group) getParseObject(GROUP);
    }

    public void setGroup(Group group) {
        put(GROUP, group);
    }

    public Category getCategory() {
        return (Category)getParseObject(CATEGORY);
    }

    public void setCategory(Category category) {
        put(CATEGORY, category);
    }

    public static List<Group> getAllGroups(List<CatMembership> catMemberships) {
        List<Group> groups = new ArrayList<>();
        for (int i = 0; i < catMemberships.size(); i++) {
            groups.add(catMemberships.get(i).getGroup());
        }
        return  groups;
    }

    public static List<Category> getAllCategories(List<CatMembership> catMemberships) {
        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < catMemberships.size(); i++) {
            categories.add(catMemberships.get(i).getCategory());
        }
        return categories;
    }

}

