package io.voxhub.accessibility.voicetouch.database;

import java.util.List;

public class CommandData {
    private String  name;
    private String alias;
    private String[] gestureArray;

    public CommandData(String n, String a, String[] array){
        name = n;
        alias = a;
        gestureArray = array;
    }


    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public String[] getGestureArray() {
        return gestureArray;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setGestureArray(String[] gestureArray) {
        this.gestureArray = gestureArray;
    }

}
