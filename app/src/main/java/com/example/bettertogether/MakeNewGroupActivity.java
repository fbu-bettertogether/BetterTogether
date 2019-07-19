package com.example.bettertogether;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import com.example.bettertogether.fragments.GroupFragment;
import com.example.bettertogether.models.Group;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

public class MakeNewGroupActivity extends AppCompatActivity {
    public final String APP_TAG = "MakeNewGroupActivity";
    private EditText groupNameInput;
    private EditText descriptionInput;
    private ImageView ivGroupProf;
    private Spinner spPrivacy;
    private Spinner spCategory;
    private Spinner spFrequency;
    private MaterialCalendarView cdStartDate;
    private MaterialCalendarView cdEndDate;
    private Button createBtn;
    private Boolean active;
    private NumberPicker npMinTime;
    private Button btnAddUsers;

    // declaring added users fields
    private RecyclerView rvAddedMembers;
    private MemberAdapter adapter;
    private ArrayList<ParseUser> addedMembers;

    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    private final int ADD_REQUEST_CODE = 20;
    public String photoFileName = "photo.jpg";
    private File photoFile;
    private final int REQUEST_CODE = 20;
    private static final int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        groupNameInput = findViewById(R.id.tvGroupName);
        descriptionInput = findViewById(R.id.tvGroupDescription);
        ivGroupProf = findViewById(R.id.ivGroupProf);
        spPrivacy = (Spinner) findViewById(R.id.spPrivacy);
        spCategory = (Spinner) findViewById(R.id.spCategory);
        spFrequency = (Spinner) findViewById(R.id.spFrequency);
        cdStartDate = (MaterialCalendarView) findViewById(R.id.calStartDate);
        cdEndDate = (MaterialCalendarView) findViewById(R.id.calEndDate);
        createBtn = (Button) findViewById(R.id.create_btn);
        npMinTime = (NumberPicker) findViewById(R.id.npMinTime);
        btnAddUsers = (Button) findViewById(R.id.btnAddUsers);

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

        ivGroupProf.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

//        // configuring recycler view for added members
//        // setting up recycler view of posts
//        rvAddedMembers = findViewById(R.id.rvTimeline);
//        addedMembers = new ArrayList<>();
//        adapter = new PostsAdapter(getContext(), mPosts);
//        rvTimeline.setAdapter(adapter);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//        rvTimeline.setLayoutManager(linearLayoutManager);
//
        btnAddUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MakeNewGroupActivity.this, AddUsersActivity.class);;
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
                } else if (cdEndDate.getSelectedDate() == null) {
                    Log.e(APP_TAG, "endDate is empty.");
                    Toast.makeText(getApplicationContext(), "There needs to be an end date!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (cdStartDate.getSelectedDate().isAfter(now) && cdEndDate.getSelectedDate().isAfter(cdStartDate.getSelectedDate())) {
                    active = true;
                } else if (cdStartDate.getSelectedDate().isBefore(now) || cdEndDate.getSelectedDate().isBefore(cdStartDate.getSelectedDate())) {
                    Log.e(APP_TAG, "Start date or end date is out of range.");
                    Toast.makeText(getApplicationContext(), "Start date or end date is out of range.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    active = false;
                }

                String start = cdStartDate.getSelectedDate().toString();
                final String startDate = start.substring(12, start.length() - 1);
                String end = cdEndDate.getSelectedDate().toString();
                final String endDate = end.substring(12, end.length() - 1);

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
//            if (addedMembers.size() != 0) {
//                relation = post.getRelation("taggedUsers");
//                for (int i = 0; i < taggedUsers.size(); i++) {
//                    relation.add(taggedUsers.get(i));
//                }
//            }
            Toast.makeText(this, "added members", Toast.LENGTH_LONG).show();
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

    private void createGroup(String description, ParseFile imageFile, String groupName, String privacy, String category, int frequency, String startDate, String endDate, ParseUser user, int minTime) {
        final Group newGroup = new Group();
        newGroup.setDescription(description);
        newGroup.setIcon(imageFile);
        newGroup.setName(groupName);
        newGroup.setPrivacy(privacy);
//        newGroup.setCategory(category);
        newGroup.setFrequency(frequency);
        newGroup.setStartDate(startDate);
        newGroup.setEndDate(endDate);
        newGroup.setOwner(user);
        newGroup.setIsActive(active);
        newGroup.setMinTime(minTime);

        newGroup.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("Carmel", "made it this far");
                    ParseRelation<ParseUser> relation = newGroup.getRelation("users");
                    for (int i = 0; i < addedMembers.size(); i++) {
                        relation.add(addedMembers.get(i));
                    }
                    newGroup.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            Log.d("HomeActivity", "Create post success!");
                            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                            startActivityForResult(i, REQUEST_CODE);
                        }
                    });
                } else {
                    e.printStackTrace();
                }
            }
        });
    }
}
