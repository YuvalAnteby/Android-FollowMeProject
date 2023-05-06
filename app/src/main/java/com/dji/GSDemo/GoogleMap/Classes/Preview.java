package com.dji.GSDemo.GoogleMap.Classes;

import android.graphics.Bitmap;

/**
 * Used to show the pictures taken by user using the drone and will be showed in the saving activity
 * bitman for showing the picture from the phone using the path, date of when the picture was taken (getting it using the dji api)
 */
public class Preview {
    private Bitmap bitmap;
    private String dateCreated;

    public Preview(Bitmap bitmap, String dateCreated) {
        this.bitmap = bitmap;
        this.dateCreated = dateCreated;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
}
