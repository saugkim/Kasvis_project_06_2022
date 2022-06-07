package org.tuni.project_kasvis;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Image.class}, version = 1, exportSchema = false)
public abstract class ImageDatabase extends RoomDatabase {
    public abstract ImageDao imageDao();

    private static volatile ImageDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static ImageDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ImageDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room
                            .databaseBuilder(context.getApplicationContext(), ImageDatabase.class, "image_database")
                            .addCallback(startRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback startRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                ImageDao dao = INSTANCE.imageDao();
//                dao.deleteAll();
            });
        }
    };
}
