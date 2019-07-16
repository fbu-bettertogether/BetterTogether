package com.example.bettertogether;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.MenuPopupWindow;
import androidx.core.content.FileProvider;

import com.example.bettertogether.models.Category;
import com.example.bettertogether.models.Group;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class MakeNewGroupActivity extends AppCompatActivity {
    public final String APP_TAG = "ComposeFragment";
    private EditText groupNameInput;
    private EditText descriptionInput;
    private ImageView ivGroupProf;
    private Spinner spPrivacy;
    private Spinner spCategory;
    private Spinner spFrequency;
    private CalendarDay cdStartDate;
    private CalendarDay cdEndDate;
    private Button createBtn;

    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
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
        spPrivacy.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);
        spCategory = (Spinner) findViewById(R.id.spCategory);
        spPrivacy.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);
        spFrequency = (Spinner) findViewById(R.id.spFrequency);
        cdStartDate = (CalendarDay) findViewById(R.id.cdStartDate);
        cdEndDate = (CalendarDay) findViewById(R.id.cdEndDate);
        createBtn = findViewById(R.id.create_btn);

        ivGroupProf.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final String description = descriptionInput.getText().toString();
                final String groupName = groupNameInput.getText().toString();
                final String privacy = spPrivacy.toString();
                final String category = spCategory.toString();
                final String frequency = spFrequency.toString();
//                final Date startDate = cdStartDate.getDate();
//                final Date endDate = cdEndDate.getDate();
                final ParseUser user = ParseUser.getCurrentUser();

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
                createGroup(description, parseFile, groupName, privacy, category, frequency, user);
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
        Uri fileProvider = FileProvider.getUriForFile(getApplicationContext(), "com.codepath.fileprovider", photoFile);
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

    private void createGroup(String description, ParseFile imageFile, String groupName, String privacy, String category, String frequency, ParseUser user) {
        final Group newGroup = new Group();
        newGroup.setDescription(description);
        newGroup.setIcon(imageFile);
        newGroup.setName(groupName);
        newGroup.setPrivacy(privacy);
        newGroup.setCategory(category);
        newGroup.setFrequency(frequency);
        //newGroup.setStartDate(startDate);
        //newGroup.setEndDate(endDate);
        newGroup.setOwner(user);
        //ParseRelation<ParseObject> relation = newPost.getRelation("likes");

        newGroup.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("HomeActivity", "Create post success!");
//                    descriptionInput.setText("");
//                    ivGroupProf.setImageResource(0);
                    Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivityForResult(i, REQUEST_CODE);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }
}
