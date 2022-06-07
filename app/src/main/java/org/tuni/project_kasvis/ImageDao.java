package org.tuni.project_kasvis;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ImageDao {

    @Query("SELECT * FROM kasvikset")
    LiveData<List<Image>> getAll();

    @Query("SELECT COUNT(id) FROM kasvikset")
    LiveData<Integer> getCounts();

    @Query("DELETE FROM kasvikset")
    void deleteAll();

    @Delete
    void delete(Image image);

    @Query("DELETE FROM kasvikset WHERE id = :id")
    void deleteById(long id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Image image);

    @Update
    void update(Image image);

}
