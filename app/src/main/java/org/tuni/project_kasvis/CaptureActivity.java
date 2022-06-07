package org.tuni.project_kasvis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CaptureActivity extends AppCompatActivity {

    public static String URI_EXTRA = "org.tuni.project_kasvis.uri_extra_key";
//    public static String LONG_EXTRA = "org.tuni.project_kasvis.longitude_extra_key";
//    public static String LAT_EXTRA = "org.tuni.project_kasvis.latitude_extra_key";

    public static String REL_PATH = Environment.DIRECTORY_DCIM + File.separator + "KASVIT";

    Executor executor = Executors.newSingleThreadExecutor();

    PreviewView previewView;
    FloatingActionButton acceptButton;

    LocationManager locationManager;
    LocationListener locationListener;
    Location currentLocation;

    ImageCapture imageCaptureInstance;
    String imageUriString = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        previewView = findViewById(R.id.previewView);
        acceptButton = findViewById(R.id.acceptButton);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = location -> currentLocation = location;

        startCamera();

        acceptButton.setOnClickListener(view -> {
            if (imageCaptureInstance == null) {
                Toast.makeText(this, "ImageCapture instance is null, clear memory and try again", Toast.LENGTH_LONG).show();
                return;
            }

            ImageCapture.OutputFileOptions outputFileOptions;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = this.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, createDisplayName());
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, REL_PATH);

                outputFileOptions = new ImageCapture.OutputFileOptions
                                .Builder(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                                .build();

            } else {
                File dir = new File(Environment.getExternalStorageDirectory().toString() + "/KASVIT");
                if (!dir.exists() && !dir.mkdirs()) {
                    return;
                }
                File file = new File(dir.getPath(), createDisplayName());
                outputFileOptions = new ImageCapture.OutputFileOptions
                                .Builder(file)
                                .build();
            }

            imageCaptureInstance.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    imageUriString = Objects.requireNonNull(outputFileResults.getSavedUri()).toString();
                    Intent data = new Intent();
                    data.putExtra(URI_EXTRA, imageUriString);
//                    data.putExtra(LONG_EXTRA, getLocation()[0]);
//                    data.putExtra(LAT_EXTRA, getLocation()[1]);
                    setResult(RESULT_OK, data);
                    finish();
                }
                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    exception.printStackTrace();
                }
            });
        });
    }

    /**
     * used to start camera to bind view to the previewView
     */
    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * used to bind view to previewView
     * to get ImageCapture instance here
     * @param cameraProvider CameraProvider instance
     */
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        int width = previewView.getWidth();
        int height = previewView.getHeight();

        Preview preview = new Preview.Builder()
                .setTargetResolution(new Size(width, height))
                .setTargetRotation(Surface.ROTATION_0)
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .setTargetResolution(new Size(width, height))
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ViewPort viewPort =
                new ViewPort.Builder(new Rational(width, height), preview.getTargetRotation()).build();

        UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageAnalysis)
                .addUseCase(imageCapture)
                .setViewPort(viewPort)
                .build();

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector,useCaseGroup);
        imageCaptureInstance = imageCapture;
    }

    /**
     * get temporary filename to save image
     * @return filename
     */
    public String createDisplayName() {
        String timeStamp = new SimpleDateFormat("dd-MM-yyyy-HHmmss", Locale.getDefault()).format(new Date());
        return String.format("Kasvi_%s.png", timeStamp);
    }

//    private float[] getLocation() {
//        float[] loc = new float[] {-200, -200};
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "access fine location permission granted");
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//            if (currentLocation != null) {
//                loc[0] = (float) currentLocation.getLongitude();
//                loc[1] = (float) currentLocation.getLatitude();
//            }
//        }
//        return loc;
//    }

}