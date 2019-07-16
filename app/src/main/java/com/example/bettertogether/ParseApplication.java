package com.example.bettertogether;

import android.app.Application;

import com.example.bettertogether.models.Award;
import com.example.bettertogether.models.Category;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Like;
import com.example.bettertogether.models.ParseComment;
import com.example.bettertogether.models.Post;
import com.parse.Parse;
import com.parse.ParseObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Use for troubleshooting -- remove this line for production
        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        // Use for monitoring Parse OkHttp traffic
        // Can be Level.BASIC, Level.HEADERS, or Level.BODY
        // See http://square.github.io/okhttp/3.x/logging-interceptor/ to see the options.
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.networkInterceptors().add(httpLoggingInterceptor);
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Like.class);
        ParseObject.registerSubclass(ParseComment.class);
        ParseObject.registerSubclass(Group.class);
        ParseObject.registerSubclass(Category.class);
        ParseObject.registerSubclass(Award.class);

        // set applicationId, and server server based on the values in the Heroku settings.
        // clientKey is not needed unless explicitly configured
        // any network interceptors must be added with the Configuration Builder given this syntax
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("better-together") // should correspond to APP_ID env variable
                .clientKey(getString(R.string.master_key))  // set explicitly unless clientKey is explicitly configured on Parse server
                .clientBuilder(builder)
                .server("http://fbu-better-together.herokuapp.com/parse").build());
    }
}
