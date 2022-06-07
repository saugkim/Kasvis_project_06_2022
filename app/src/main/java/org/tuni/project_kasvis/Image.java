package org.tuni.project_kasvis;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


@Entity(tableName="kasvikset")
public class Image {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name="uri")
    private String imageUri;

    @ColumnInfo(name="name")
    private String name;

    @ColumnInfo(name="date")
    private String date;

    @ColumnInfo(name="longitude")
    private double longitude;

    @ColumnInfo(name="latitude")
    private double latitude;

    @ColumnInfo(name="address")
    private String address;

    public Image() {
        longitude = -200;
        latitude = -200;
    }

    @Ignore
    public Image(String filename, String imageUri, String name){
        this.date = filename;
        this.name = name;
        this.imageUri = imageUri;
        longitude = -200;
        latitude = -200;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setAddress(String address) { this.address = address; }

    public int getId() {
        return id;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getAddress() { return address; }

}
