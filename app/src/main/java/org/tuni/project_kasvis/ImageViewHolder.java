package org.tuni.project_kasvis;

import android.app.AlertDialog;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ImageViewHolder extends RecyclerView.ViewHolder {

    public static final String TAG = "ZZ ImageViewHolder: ";
    ImageView imageView;
    TextView textViewLabel;

    Image currentImage;
    ImageRepository imageRepository;

    Context context;

    public ImageViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView = itemView.findViewById(R.id.imageView);
        textViewLabel = itemView.findViewById(R.id.textViewLabel);

        context = itemView.getContext();

        Application application = (Application) itemView.getContext().getApplicationContext();
        imageRepository = new ImageRepository(application);

        itemView.setOnLongClickListener(view-> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Selected saved image will be removed permanently, are you sure?");
            builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                imageRepository.delete(currentImage);
                removePhotoFromDevice(view.getContext(), currentImage);
            });
            builder.setNegativeButton("No", (dialogInterface, i) -> Toast.makeText(view.getContext(), "cancelled", Toast.LENGTH_LONG).show());
            AlertDialog ad = builder.create();
            ad.show();
            return true;
        });
    }

    public void removePhotoFromDevice(Context context, Image image){
        Uri uri = Uri.parse(image.getImageUri());
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(uri, null, null);
    }

    public void bind(Image image) {
        currentImage = image;
        Log.d(TAG, "image uri: " + image.getDate());
        Log.d(TAG, "image uri: " + image.getImageUri());
        Log.d(TAG, "image uri: " + image.getId());

        Uri uri = Uri.parse(image.getImageUri());
        try {
            imageView.setImageURI(uri);
        } catch (Exception e) {
            Toast.makeText(context, "This image file was deleted outside this app, please remove this from database too", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            imageView.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.ic_menu_help));
        }

        String ret = "Name: " + image.getName() + "\n" +
                "Date: " + image.getDate() + "\n" +
                "Place: " + image.getAddress() + "\n" +
                "Latitude: " + image.getLatitude() + "\n" +
                "Longitude: " + image.getLongitude() + "\n";
        textViewLabel.setText(ret);
    }

    static ImageViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_image, parent, false);
        return new ImageViewHolder(view);
    }
}
