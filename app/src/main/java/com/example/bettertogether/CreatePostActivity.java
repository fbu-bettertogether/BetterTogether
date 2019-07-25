package com.example.bettertogether;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bettertogether.fragments.HomeFragment;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Post;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CreatePostActivity extends AppCompatActivity {
    private final int TAG_REQUEST_CODE = 20;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public final static int PROFILE_IMAGE_ACTIVITY_REQUEST_CODE = 583;
    public static final int RESULT_OK = -1;
    public final String APP_TAG = "BetterTogether";
    ParseRelation<ParseObject> relation;

    private String photoFileName = "photo.jpg";
    private File photoFile;
    private Group group;

    private ImageView ivIcon;
    private ImageView ivTag;
    private ImageView ivCamera;
    private TextView tvTag;
    private TextView tvCamera;
    private TextView tvUsername;
    private TextView tvGroup;
    private TextInputEditText etPost;
    private Toolbar toolbar;
    private ArrayList<ParseUser> taggedUsers;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        ivIcon = findViewById(R.id.ivIcon);
        ivTag = findViewById(R.id.ivAdd);
        ivCamera = findViewById(R.id.ivCamera);
        tvTag = findViewById(R.id.tvTag);
        tvCamera = findViewById(R.id.tvCamera);
        tvUsername = findViewById(R.id.tvUsername);
        tvGroup = findViewById(R.id.tvGroup);
        etPost = findViewById(R.id.etPost);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // The Group needs to be passed to this activity through an intent
        group = (Group) Parcels.unwrap(getIntent().getParcelableExtra("group"));

        if(ParseUser.getCurrentUser().getParseFile("profileImage") != null) {
            // loading in profile image if available
            Glide.with(this)
                    .load(ParseUser.getCurrentUser().getParseFile("profileImage").getUrl())
                    .apply(RequestOptions.circleCropTransform()).into(ivIcon);
        }

        tvUsername.setText(ParseUser.getCurrentUser().getUsername());
        tvGroup.setText(String.format("Post in %s", group.getName()));
        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLaunchCamera(view);
            }
        });
        ivTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreatePostActivity.this, TagActivity.class);
                intent.putExtra("group", (Serializable) group);
                startActivityForResult(intent, TAG_REQUEST_CODE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.compose_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuCompose:
            default:
                createPost();
                return true;
        }
    }

    public void createPost() {
        final Post post = new Post();
        if (photoFile != null) {
            post.setMedia(new ParseFile(photoFile));
        }
        if (taggedUsers != null) {
            relation = post.getRelation("taggedUsers");
            for (int i = 0; i < taggedUsers.size(); i++) {
                relation.add(taggedUsers.get(i));
            }
        }
        post.setDescription(etPost.getText().toString());
        post.setUser(ParseUser.getCurrentUser());
        post.setGroup(group);
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                } else {
                    Log.d(APP_TAG, "success");
//                    finish();
                }

                ParseRelation<Post> groupRelation = group.getRelation("posts");
                groupRelation.add(post);

                group.saveInBackground(new SaveCallback() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            e.printStackTrace();
                        } else {
                            MyFirebaseMessagingService mfms = new MyFirebaseMessagingService();
                            mfms.logToken(getApplicationContext());
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("message", "New Notification from Your Friend.");
                            hashMap.put("notification_key", (String) taggedUsers.get(0).get("deviceId"));
                            mfms.sendPushToSingleInstance(getApplicationContext(), hashMap, (String) taggedUsers.get(0).get("deviceId"));
                            //mfms.sendNotification((String) taggedUsers.get(0).get("deviceId"), getApplicationContext());
                            //sendNotification();
//                            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
//                            installation.put("device_id", (String) taggedUsers.get(0).get("deviceId"));
//                            installation.saveInBackground();
//
//                            ParseQuery query = ParseInstallation.getQuery();
//                            query.whereEqualTo("device_id", (String) taggedUsers.get(0).get("deviceId"));
//                            ParsePush push = new ParsePush();
//                            push.setMessage("Better Together");
//                            push.setQuery(query);
//                            push.sendInBackground();
//
//                            final String currUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                            String content = "You were tagged in " + ParseUser.getCurrentUser().getUsername() + "'s new post.";
//                            sendMessage(currUser, (String) taggedUsers.get(0).get("deviceId"), content);
//                            ParsePush push = new ParsePush();
//                            push.setMessage("Better Together");
//                            push.sendInBackground();
                            finish();
                        }
                    }
                });
            }
        });
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    public void onLaunchCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference to access to future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(this, "com.bettertogether.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            // Start the image capture intent to take photo
            this.startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                ivCamera.setImageBitmap(takenImage);
            } else if (requestCode == TAG_REQUEST_CODE) {
                taggedUsers = data.getParcelableArrayListExtra("taggedUsers");
                String tagText = "";
                if (taggedUsers == null) {
                    taggedUsers = new ArrayList<>();
                }
                for (int i = 0; i < taggedUsers.size(); i++) {
                    tagText = tagText + "@" + taggedUsers.get(i).getUsername() + ",";
                }
                if (tagText.length() > 0) {
                    tagText = tagText.substring(0, tagText.length() - 1);
                }
                tvTag.setText(tagText);
                etPost.setText(etPost.getText() + " " + tagText);
            }
        }
    }

    private void sendMessage(String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        reference.child("Chats").push().setValue(hashMap);
    }

    private void sendNotification() {
        MyFirebaseMessagingService mfms = new MyFirebaseMessagingService();
        mfms.logToken(getApplicationContext());
        createNotificationChannel();
        Intent intent = new Intent(this, HomeFragment.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        String title = "BetterTogether";
        String content = "You were tagged in " + ParseUser.getCurrentUser().getUsername() + "'s new post.";
        int importance = NotificationManagerCompat.IMPORTANCE_DEFAULT;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID")
                .setSmallIcon(R.drawable.handshake)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(content))
                .setPriority(importance)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1100, builder.build());
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String title = "BetterTogether";
            String content = "BetterTogether's Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", title, importance);
            channel.setDescription(content);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}