package org.tuni.project_kasvis;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ImageViewModel extends AndroidViewModel {
    private final LiveData<List<Image>> allImages;
    LiveData<Integer> total_number;
    ImageRepository imageRepository;

    public ImageViewModel(@NonNull Application application) {
        super(application);

        imageRepository = new ImageRepository(application);
        allImages = imageRepository.getAll();
        total_number = imageRepository.getCounts();
      }

    public LiveData<List<Image>> getAllImages() {
        return allImages;
    }

    public LiveData<Integer> getCounts() {
        return total_number;
    }
}
