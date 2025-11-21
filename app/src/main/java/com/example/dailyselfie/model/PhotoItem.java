package com.example.dailyselfie.model;

import java.io.File;

public class PhotoItem {

    public static final int TYPE_DATE = 0;
    public static final int TYPE_PHOTO = 1;

    public int type;
    public String date; // dùng cho header ngày
    public File file;   // dùng cho ảnh

    // Constructor cho header ngày
    public static PhotoItem createDate(String date) {
        PhotoItem item = new PhotoItem();
        item.type = TYPE_DATE;
        item.date = date;
        return item;
    }

    // Constructor cho ảnh
    public static PhotoItem createPhoto(File file) {
        PhotoItem item = new PhotoItem();
        item.type = TYPE_PHOTO;
        item.file = file;
        return item;
    }
}
