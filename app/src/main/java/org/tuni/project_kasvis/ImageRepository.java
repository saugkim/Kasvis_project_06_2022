package org.tuni.project_kasvis;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ImageRepository {

    public static String TAG = "ZZ UserRepository ";
    ImageDao dao;
    LiveData<List<Image>> images;

    ImageRepository (Application application) {
        dao = ImageDatabase.getDatabase(application).imageDao();
        images = dao.getAll();
    }

    public LiveData<List<Image>> getAll() {
        return images;
    }

    public void insert(Image image) {
        ImageDatabase.databaseWriteExecutor.execute(()-> {
            int id = (int) dao.insert(image);
            image.setId(id);
        });
    }

    public void deleteAll() {
        ImageDatabase.databaseWriteExecutor.execute(()-> dao.deleteAll());
    }

    public void delete(Image image) {
        ImageDatabase.databaseWriteExecutor.execute(()-> dao.delete(image));
        Log.d(TAG, "delete method after execute ");
    }

    public void deleteById(int id) {
        Log.d(TAG, "deleteById method before execute id? " + id);
        ImageDatabase.databaseWriteExecutor.execute(()-> dao.deleteById(id));
    }

    public LiveData<Integer> getCounts() {
        return dao.getCounts();
    }
}
