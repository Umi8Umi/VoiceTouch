package io.voxhub.accessibility.voicetouch.database;

import android.graphics.Bitmap;

public final class GestureData {

    private String name;
    private String points;
    private Bitmap background;

    public GestureData(String n, String p,  Bitmap b){
        this.name = n;
        this.points = p;
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


    public void setName(String name) {
        this.name = name;
    }

    public void setPoints(String points) {
        this.points = points;
    }

}
