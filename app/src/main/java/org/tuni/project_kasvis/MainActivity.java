package org.tuni.project_kasvis;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    final static String TAG = "ZZ MainActivity: ";

    public static String URI_EXTRA = "org.tuni.project_kasvis.uri_extra_key";

    private final String[] REQUIRED_PERMISSIONS = new String[] {
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.INTERNET",
            "android.permission.CAMERA"
    };

    private final String[] REQUIRED_PERMISSIONS_28 = new String[] {
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.INTERNET",
            "android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    ImageView imageView;
    Button saveButton;
    TextView textViewDate, textViewAddress;
    EditText editTextName;
    FloatingActionButton captureButton, listButton;

    ConstraintLayout bottomLayout;

    LocationManager locationManager;
    LocationListener locationListener;
    Location currentLocation;

    ActivityResultLauncher<Intent> capturePhotoLauncher;
    ActivityResultLauncher<String> getContentLauncher;

    ActivityResultLauncher<String[]> requestMultiplePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                Log.d(TAG, "Launcher results: " + isGranted.toString());
                if (isGranted.containsValue(false)) {
                    Log.d(TAG, "Launcher, not granted");
                } else {
                    Log.d(TAG, "Launcher, all permission granted");
                }
            });

    String objectDate = null;
    String objectUri = null;
    String objectName = null;
    String objectAddress = null;
    double objectLongitude = -200;
    double objectLatitude = -200;

    Boolean isDuplicated = false;
    Boolean isLocationPermissionGranted = false;

    ImageRepository imageRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        captureButton = findViewById(R.id.floatingActionButtonCamera);
        listButton = findViewById(R.id.floatingActionButtonList);
        saveButton = findViewById(R.id.buttonSave);

        bottomLayout = findViewById(R.id.bottomLayout);

        textViewDate = findViewById(R.id.textViewDate);
        textViewAddress = findViewById(R.id.textViewAddress);
        editTextName = findViewById(R.id.editTextName);

        imageRepository = new ImageRepository(getApplication());

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = location -> currentLocation = location;

        checkAndRequestPermissions();
        resetUI();

        capturePhotoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        assert data != null;
                        String uriString = data.getStringExtra(URI_EXTRA);
                        objectUri = uriString;
                        try {
                            //imageView.setImageURI(Uri.parse(uriString));
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(uriString));
                            Bitmap temp = getOrientatedBitmap(bitmap, Uri.parse(uriString));
                            imageView.setImageBitmap(temp);
                            bottomLayout.setVisibility(View.VISIBLE);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        getContentLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        Bitmap temp = getOrientatedBitmap(bitmap, uri);
                        imageView.setImageBitmap(temp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG,"uri from getContent launcher: " + uri);
                    String retrievedFilename = getFilenameFromUri(this, uri);
                    Log.d(TAG, "filename: " + retrievedFilename);
                    Toast.makeText(this,"filename from getContent: " + retrievedFilename, Toast.LENGTH_LONG).show();

                    if (retrievedFilename.startsWith("Kasvi")) {
                        isDuplicated = true;
                    }
                });

        captureButton.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this,"This feature need device camera, currently camera permission not accepted, you can take photo outside this app and use it", Toast.LENGTH_LONG).show();
                return;
            }
            resetUI();

            Intent captureIntent = new Intent(this, CaptureActivity.class);
            capturePhotoLauncher.launch(captureIntent);

            getCurrentDate();
            if (isLocationPermissionGranted) {
                getLocation();
            }
        });

        saveButton.setOnClickListener(view -> {
            if (isDuplicated) {
                Toast.makeText(this, "This image is already in device", Toast.LENGTH_LONG).show();
                return;
            }
            saveImageDatabase();
            resetUI();
            Toast.makeText(this, "Data is saved successfully, make a new entry!", Toast.LENGTH_LONG).show();

        });

        listButton.setOnClickListener(view-> {
            resetUI();
            startActivity(new Intent(this, StatisticsActivity.class));
        });
    }

    /**
     * Used to checking and asking permissions for camera action and getting location information
     * App working without those permissions but some features are not working for example taking photo inside app
     */
    private void checkAndRequestPermissions() {
        Log.d(TAG, "inside checkPermissions()");
        List<String> permissionNeeded = new ArrayList<>();

        for (String p : Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? REQUIRED_PERMISSIONS : REQUIRED_PERMISSIONS_28) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                permissionNeeded.add(p);
                Log.d(TAG, "not granted " + p);
            }
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                isLocationPermissionGranted = true;
                Log.d(TAG, "checkPermissions() FINE LOCATION granted");
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            }
        }

        if (!permissionNeeded.isEmpty()) {
            Log.d(TAG, "asking permissions .....");
            requestMultiplePermissionLauncher.launch(permissionNeeded.toArray(new String[0]));
        }
    }

    /**
     * Used to get current location when taking photo.
     * App works without this permission.
     * If fine location permission is granted, current location info will be added.
     * location info is only available with taking photo not importing(loading) photo from gallery
     * even if photo in the gallery has location data in it, this app will not try to find the location info.
     *
     * if permission is denied, simply location information will not be added to the database.
     *
     * if permission granted, one can re-visit that place and take additional photos (different season, angle, and so on)
     * to re-train deep-learning model, in case the label of the image is incorrect
     */
    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "access fine location permission granted");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            if (currentLocation != null) {
                objectLongitude = currentLocation.getLongitude();
                objectLatitude = currentLocation.getLatitude();
                Log.d(TAG, "longitude: " + objectLongitude + ", latitude: " + objectLatitude);
                objectAddress = getAddress();
                textViewAddress.setText(objectAddress);
            }
        }
    }

    /**
     * Used to get filename from image uri and to check duplication of the image.
     * Getting file from uri is different for different uri scheme,
     * a. For "File Uri Scheme" - We will get file from uri.
     * b. For "Content Uri Scheme" - We will get the file by querying content resolver.
     * @param uri Uri.
     * @return String filename.
     */
    public String getFilenameFromUri(Context context, Uri uri) {
        String filename = null;
        if (uri != null) {
            // File Scheme.
            if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
                File file = new File(uri.getPath());
                filename = file.getName();
            }
            // Content Scheme.
            else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
                Cursor returnCursor =
                        context.getContentResolver().query(uri, null, null, null, null);
                if (returnCursor != null && returnCursor.moveToFirst()) {
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    filename = returnCursor.getString(nameIndex);
                    returnCursor.close();
                }
            }
        }
        return filename;
    }

    /**
     * used to initialize(Reset) main ui when necessary
     */
    private void resetUI() {
        bottomLayout.setVisibility(View.INVISIBLE);

        isDuplicated = false;
        imageView.setImageResource(android.R.color.transparent);
        imageView.destroyDrawingCache();
        editTextName.getText().clear();

        objectDate = null;
        objectName = null;
        objectUri = null;
        objectAddress = null;
        objectLongitude = -200;
        objectLatitude = -200;
    }

    public void getCurrentDate() {
        String today = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
        objectDate = today;
        textViewDate.setText(String.format("Date: %s", today));
    }

    public String getAddress() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            Log.d(TAG, "getAddress try block");
            List<Address> addresses = geocoder.getFromLocation(objectLatitude, objectLongitude, 1);
            String locality = addresses.get(0).getLocality();
            //objectAddress = addresses.get(0).getAddressLine(0);
            objectAddress = locality;
            return String.format("Place: %s", locality);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Place: location not available";
    }

    /**
     * Used to save data (custom Image object) to the Room database
     */
    public void saveImageDatabase() {
        objectName = editTextName.getText().toString();

        Image image = new Image();
        image.setDate(objectDate);
        image.setName(objectName);
        image.setImageUri(objectUri);
        image.setLatitude(objectLatitude);
        image.setLongitude(objectLongitude);
        image.setAddress(objectAddress);

        imageRepository.insert(image);
    }

    /**
     * Device has own camera (sensor) orientation, this method rotate bitmap to the right angle(same as user's action)
     * ExifInterface library used to get correct orientation and rotate bitmap accord to it.
     * @param bitmap bitmap image of saved photo
     * @param uri uri of saved image
     * @return oriented bitmap for running(performing) torch model as a source image
     */
    public Bitmap getOrientatedBitmap(Bitmap bitmap, Uri uri){
        try (InputStream inputStream = this.getContentResolver().openInputStream(uri)) {
            ExifInterface exif = new ExifInterface(inputStream);
            int orientationTag = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            int rotationAngle = 0;
            if (orientationTag == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientationTag == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientationTag == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

            Log.d(TAG, "orientation from exif: " + rotationAngle);
            Matrix mat = new Matrix();
            mat.postRotate(rotationAngle);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}