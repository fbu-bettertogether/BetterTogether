package com.example.bettertogether;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bettertogether.models.CatMembership;
import com.example.bettertogether.models.Category;
import com.example.bettertogether.models.Group;
import com.example.bettertogether.models.Membership;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MakeNewGroupActivity extends AppCompatActivity {
    public final String APP_TAG = "MakeNewGroupActivity";
    private EditText groupNameInput;
    private EditText descriptionInput;
    private ImageView ivGroupProf;
    private Spinner spPrivacy;
    private Spinner spCategory;
    private Spinner spFrequency;
    private MaterialCalendarView cdStartDate;
    private Button createBtn;
    private Boolean active;
    private NumberPicker npMinTime;
    private Button btnAddUsers;
    private NumberPicker npNumWeeks;
    private Date start;

    // declaring added users fields
    private RecyclerView rvAddedMembers;
    private AddUsersAdapter adapter;
    private ArrayList<ParseUser> addedMembers;
    private List<ParseUser> addedUsers = new ArrayList<ParseUser>();

    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    private final int ADD_REQUEST_CODE = 20;
    public String photoFileName = "photo.jpg";
    private File photoFile;
    private final int REQUEST_CODE = 20;
    private static final int RESULT_LOAD_IMAGE = 1;

    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAArU_ed7A:APA91bHInx5XEXefgNJI83z6_kCcryuj_LqFrz-9kHPLb-MEClcwIMt_XI9HVTb4AreIxonQuOAEG5_UZDTCgkY1SykbUbE_fOobhRv6WehEF6TuMUmiKofMQjUPGoF1zz7WQTwyIEj9";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";

    String notificationTitle;
    String notificationMsg;
    String notificationTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        addedMembers = new ArrayList<>();
        groupNameInput = findViewById(R.id.tvGroupName);
        descriptionInput = findViewById(R.id.tvGroupDescription);
        ivGroupProf = findViewById(R.id.ivGroupProf);
        spPrivacy = (Spinner) findViewById(R.id.spPrivacy);
        spCategory = (Spinner) findViewById(R.id.spCategory);
        spFrequency = (Spinner) findViewById(R.id.spFrequency);
        cdStartDate = (MaterialCalendarView) findViewById(R.id.calStartDate);
        rvAddedMembers = (RecyclerView) findViewById(R.id.rvAddedMembers);

        createBtn = (Button) findViewById(R.id.create_btn);
        npMinTime = (NumberPicker) findViewById(R.id.npMinTime);
        btnAddUsers = (Button) findViewById(R.id.btnAddUsers);
        npNumWeeks = (NumberPicker) findViewById(R.id.npNumWeeks);

        // configuring the number picker
        final String[] npVals = new String[60];
        // setting the number picker values from 10-600 with 10 step size
        for (int i = 1; i <= 60; i++) {
            npVals[i-1] = Integer.toString(i * 10);
        }
        npMinTime.setDisplayedValues(npVals);
        npMinTime.setMaxValue(59);
        npMinTime.setMinValue(0);
        npMinTime.setWrapSelectorWheel(true);
        // preventing keyboard from popping up
        npMinTime.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        npNumWeeks.setMaxValue(52);
        npNumWeeks.setMinValue(1);
        npNumWeeks.setWrapSelectorWheel(true);
        // preventing keyboard from popping up
        npNumWeeks.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        ivGroupProf.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

        // configuring recycler view for added members
        // setting up recycler view of posts
        rvAddedMembers = findViewById(R.id.rvAddedMembers);
        addedMembers = new ArrayList<>();
        adapter = new AddUsersAdapter(addedUsers, addedUsers);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MakeNewGroupActivity.this);
        rvAddedMembers.setLayoutManager(linearLayoutManager);
        rvAddedMembers.setAdapter(adapter);

        btnAddUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MakeNewGroupActivity.this, AddUsersActivity.class);
                //Keeps track of which users have already been added, so that they cannot be added again.
                intent.putParcelableArrayListExtra("alreadyAdded", (ArrayList<? extends Parcelable>) addedUsers);
                startActivityForResult(intent, ADD_REQUEST_CODE);
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final String description = descriptionInput.getText().toString();
                final String groupName = groupNameInput.getText().toString();
                final String privacy = spPrivacy.getSelectedItem().toString();
                final String category = spCategory.getSelectedItem().toString();
                final int frequency = Integer.parseInt(spFrequency.getSelectedItem().toString());
                final ParseUser user = ParseUser.getCurrentUser();
                final int minTime = Integer.parseInt(npVals[npMinTime.getValue()]);

                final CalendarDay now = CalendarDay.today();
                if (cdStartDate.getSelectedDate() == null) {
                    Log.e(APP_TAG, "startDate is empty.");
                    Toast.makeText(getApplicationContext(), "There needs to be a start date!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (cdStartDate.getSelectedDate().isAfter(now)) {
                    active = false;
                } else if (cdStartDate.getSelectedDate().isBefore(now)) {
                    Log.e(APP_TAG, "Start date is out of range.");
                    Toast.makeText(getApplicationContext(), "Start date is out of range.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    active = true;
                }

                Calendar cal =  Calendar.getInstance();
                int day = cdStartDate.getSelectedDate().getDay();
                int month = cdStartDate.getSelectedDate().getMonth();
                int year = cdStartDate.getSelectedDate().getYear();
                cal.set(Calendar.DATE, day);
                cal.set(Calendar.MONTH, month - 1);
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                start = cal.getTime();
                String startDate = cal.getTime().toString();
                cal.add(Calendar.WEEK_OF_YEAR, npNumWeeks.getValue());
                Date date = cal.getTime();
                String endDate = date.toString();

                if (description == null || description == "") {
                    Log.e(APP_TAG, "Description field is empty.");
                    Toast.makeText(getApplicationContext(), "Description cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (groupName == null || groupName == "") {
                    Log.e(APP_TAG, "Groupname field is empty.");
                    Toast.makeText(getApplicationContext(), "Your group needs a name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                //later on, they user can choose to take an image or upload an image

                File mediaStorageDir = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

                if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                    Log.d(APP_TAG, "failed to create directory");
                }

                final File file = new File(mediaStorageDir.getPath() + File.separator + photoFileName);
                if (file == null || ivGroupProf.getDrawable() == null) {
                    Log.e(APP_TAG, "No photo to submit");
                    Toast.makeText(getApplicationContext(), "There is no photo!", Toast.LENGTH_SHORT).show();
                    return;
                }

                final ParseFile parseFile = new ParseFile(file);
                createGroup(description, parseFile, groupName, privacy, category, frequency, startDate, endDate, user, minTime);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = rotateBitmapOrientation(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ivGroupProf.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(getApplicationContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
//        } else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
//            Uri selectedImage = data.getData();
//            photoFile = getPhotoFileUri(getRealPathFromURI(getApplicationContext(), selectedImage));
//            groupProf.setImageURI(selectedImage);
        } else if (requestCode == ADD_REQUEST_CODE && resultCode == RESULT_OK) {
            addedMembers = data.getParcelableArrayListExtra("addedMembers");
            addedUsers.addAll(addedMembers);
            adapter.notifyDataSetChanged();
        }
    }

    public Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        // Return result
        return rotatedBitmap;
    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference to access to future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getApplicationContext(), "com.bettertogether.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    private void createGroup(String description, ParseFile imageFile, String groupName, String privacy, final String category, int frequency, String startDate, String endDate, final ParseUser user, int minTime) {
        final Group newGroup = new Group();
        newGroup.setDescription(description);
        newGroup.setIcon(imageFile);
        newGroup.setName(groupName);
        newGroup.setPrivacy(privacy);
        newGroup.setCategory(category);
        newGroup.setFrequency(frequency);
        newGroup.setStartDate(startDate);
        newGroup.setEndDate(endDate);
        newGroup.setOwner(user);
        newGroup.setIsActive(active);
        newGroup.setMinTime(minTime);
        newGroup.setNumWeeks(npNumWeeks.getValue());
        newGroup.saveInBackground();

        final Category.Query catQuery = new Category.Query();
        catQuery.findInBackground(new FindCallback<Category>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void done(List<Category> objects, final ParseException e) {
                if (e == null) {
                    saveCat(objects, addedMembers, newGroup, category);
                    MyFirebaseMessagingService mfms = new MyFirebaseMessagingService();
                    mfms.logToken(getApplicationContext());
                    if (addedMembers != null) {
                        for (int i = 0; i < addedMembers.size(); i++) {
                            Messaging.sendNotification((String) addedMembers.get(i).get("deviceId"), "A new group was created by " + user.getUsername() + "!");
                        }
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void saveCat(List<Category> objects, final ArrayList<ParseUser> addedMembers, final Group newGroup, String category) {
        final ArrayList<Category> catList = new ArrayList<>();
        for (int i = 0; i < objects.size(); ++i) {
            Log.d("DiscoveryActivity", "Category[" + i + "] = "
                    + objects.get(i).getName());
        }
        catList.addAll(objects);
        //mainRecyclerAdapter.notifyDataSetChanged();
        Category cat = new Category();
        Boolean found = false;
        for (int i = 0; i < catList.size(); i++) {
            if (catList.get(i).getName().equalsIgnoreCase(category)) {
                cat = catList.get(i);
                found = true;
                break;
            }
        }
        if (!found) {
            cat.setName(category);
            cat.saveInBackground();
        }
        final CatMembership catMembership = new CatMembership();
        catMembership.setGroup(newGroup);
        catMembership.setCategory(cat);
        newGroup.setCategory(cat.getObjectId());
        newGroup.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                catMembership.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            e.printStackTrace();
                        }
                    }
                });
                saveMemberships(addedMembers, newGroup);
            }
        });
    }

    private void saveMemberships(final ArrayList<ParseUser> addedMembers, final Group newGroup) {
        List<Membership> memberships = new ArrayList<>();
        for (int i = 0; i < addedMembers.size(); i++) {
            memberships.add( new Membership());
            memberships.get(i).setGroup(newGroup);
            memberships.get(i).setUser(addedMembers.get(i));
            ArrayList<Integer> numCheckIns = new ArrayList<Integer>();
            memberships.get(i).setNumCheckIns(numCheckIns);
            memberships.get(i).setPoints(0);
            final int finalI = i;
            memberships.get(i).saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        e.printStackTrace();
                    }
                    if (finalI == addedMembers.size() - 1) {
                        scheduleAlarm(newGroup);
                        Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivityForResult(i, REQUEST_CODE);
                    }
                }
            });
        }
    }

    // set up recurring alarm so that numCheckIns gets moved to new entry each week
    // will also check if group is active
    public void scheduleAlarm(Group group) {
        // construct an intent to execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("group", (Serializable) group);
        intent.putExtra("bundle", bundle);
        // create a pending intent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // setup periodic alarm every week from the start day onwards
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            alarm.setInexactRepeating(AlarmManager.RTC, start.getTime(), AlarmManager.INTERVAL_DAY * 7, pIntent);
        }
    }
}
