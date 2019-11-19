package io.voxhub.accessibility.voicetouch.database;

import android.graphics.Bitmap;

public final class GestureData {

    private String name;
    private String points;
    private Bitmap image;
    private Bitmap background;

    public GestureData(String n, String p, Bitmap i, Bitmap b){
        this.name = n;
        this.points = p;
        this.image = i;
        this.background = b;
    }

    public Bitmap getBackground() {
        return background;
    }

    public void setBackground(Bitmap background) {
        this.background = background;
    }

    public String getName() {
        return name;
    }

    public String getPoints() {
        return points;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}
