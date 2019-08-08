package com.example.bettertogether;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.bettertogether.fragments.AwardFragment;
import com.example.bettertogether.fragments.DiscoveryFragment;
import com.example.bettertogether.fragments.GroupsFragment;
import com.example.bettertogether.fragments.HomeFragment;
import com.example.bettertogether.fragments.ProfileFragment;
import com.example.bettertogether.models.Award;
import com.example.bettertogether.models.Invitation;
import com.example.bettertogether.models.Membership;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements ProfileFragment.OnProfileFragmentInteraction {

    private static final int ACCESS_FINE_LOCATION_REQUEST_READ_CONTACTS = 36;
    public final int INVITATION_REQUEST_CODE = 232;
    public final int GROUP_INVITATION_REQUEST_CODE = 514;
    public final static int PROFILE_IMAGE_ACTIVITY_REQUEST_CODE = 121;
    public final String APP_TAG = "BetterTogether";
    private String photoFileName = "photo.jpg";
    private File profilePhotoFile;
    private BottomNavigationView bottomNavigationView;
    private Award friendshipGoals;
    private AwardFragment af;

    public PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        af = new AwardFragment();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.action_home:
                        deleteLocation();
                        fragment = new HomeFragment();
                        break;
                    case R.id.action_discovery:
                        deleteLocation();
                        fragment = new DiscoveryFragment();
                        break;
                    case R.id.action_groups:
                        deleteLocation();
                        fragment = new GroupsFragment();
                        break;
                    case R.id.action_profile:
                    default:
                        deleteLocation();
                        fragment = ProfileFragment.newInstance(ParseUser.getCurrentUser());
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_REQUEST_READ_CONTACTS);
        }
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_groups);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // locatinos-related task you need to do.
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            ACCESS_FINE_LOCATION_REQUEST_READ_CONTACTS);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
    public void deleteLocation(){
            ParseQuery<Membership> parseQuery = new ParseQuery<Membership>(Membership.class);
            parseQuery.addDescendingOrder("updatedAt");
            parseQuery.whereEqualTo("user", ParseUser.getCurrentUser());
            parseQuery.include("location");
            parseQuery.findInBackground(new FindCallback<Membership>() {
        @Override
        public void done(List<Membership> objects, ParseException e) {
            if (e != null) {
                Log.e("Querying groups", "error with query");
                e.printStackTrace();
                return;
            }
            for (int i = 0; i < objects.size(); i++) {
                objects.get(i).remove("location");
                objects.get(i).saveInBackground();
            }
        }
    });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PROFILE_IMAGE_ACTIVITY_REQUEST_CODE) {
                final ParseFile parseFile = new ParseFile(profilePhotoFile);
                parseFile.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            e.printStackTrace();
                        }
                        ParseUser user = ParseUser.getCurrentUser();
                        user.put("profileImage", parseFile);
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    e.printStackTrace();
                                } else {
                                    Log.d(APP_TAG, "Successful Profile Picture");
                                }
                            }
                        });

                    }
                });
            } else {
                int unmaskedRequestCode = requestCode & 0x0000ffff;
                if (unmaskedRequestCode == INVITATION_REQUEST_CODE || unmaskedRequestCode == GROUP_INVITATION_REQUEST_CODE) {
                    List<Invitation> invitations = data.getParcelableArrayListExtra("taggedInvitations");
                    for (Invitation invitation : invitations) {
                        if (invitation.getGroup() == null) {
                            ParseUser receiver = new ParseUser();
                            ParseUser inviter = new ParseUser();
                            try {
                                receiver = invitation.getReceiver().fetchIfNeeded();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            try {
                                inviter = invitation.getInviter().fetchIfNeeded();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            ParseRelation receiverFriends = (ParseRelation) receiver.get("friends");
                            receiverFriends.add(invitation.getInviter());
                            receiver.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            MyFirebaseMessagingService mfms = new MyFirebaseMessagingService();
                            mfms.logToken(getApplicationContext());
                            Messaging.sendNotification((String) invitation.getInviter().get("deviceId"), invitation.getReceiver().getUsername() + " just accepted your friend request!");
                            ParseQuery<ParseObject> query = ParseQuery.getQuery("Award");
                            query.getInBackground(getString(R.string.friendship_goals_award), new GetCallback<ParseObject>() {
                                public void done(ParseObject object, ParseException e) {
                                    if (e == null) {
                                        friendshipGoals = (Award) object;
                                        af.queryAward(friendshipGoals, false, true, getApplicationContext());
                                    } else {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            Membership membership = new Membership();
                            membership.setUser(invitation.getReceiver());
                            membership.setGroup(invitation.getGroup());
                            membership.setNumCheckIns(new ArrayList<Integer>());
                            membership.setPoints(0);
                            membership.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            MyFirebaseMessagingService mfms = new MyFirebaseMessagingService();
                            mfms.logToken(getApplicationContext());
                            Messaging.sendNotification((String) invitation.getReceiver().get("deviceId"), ParseUser.getCurrentUser().getUsername() + " just accepted your request to be in their group!");
                        }
                        invitation.setAccepted("accepted");
                        invitation.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }

                }
            }
        }
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
    public void createProfilePicture(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference to access to future access
        profilePhotoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(this, "com.bettertogether.fileprovider", profilePhotoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, PROFILE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

}

