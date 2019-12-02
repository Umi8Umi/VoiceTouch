package io.voxhub.accessibility.servicecode;
//package com.example.android.apis.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyAccessibilityService extends AccessibilityService {
    public MyAccessibilityService() {
        Log.i("accessibilityservice", "constructor");
    }
   
    @Override
    public void onServiceConnected() {
        Log.i("accessibilityservice", "service connected");
    }

    private String getTextFor(AccessibilityEvent event) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence ch : event.getText()) {
            sb.append(ch.toString());
        }

        String text = sb.toString();
        Log.d("accessibilityservice", "split event text ["
            + text + "] -> [" + text.split(":")[0] + "]");
        return text.split(":")[0];
    }

    private String getGestureFromEvent(AccessibilityEvent event) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence ch : event.getText()) {
            sb.append(ch.toString());
        }

        String text = sb.toString();
        Log.d("accessibilityservice", "split event gesture ["
                + text + "] -> [" + text.split(":")[1] + "]");
        return text.split(":")[1];
    }

    private enum GestureType {
        GESTURE_SWIPE_LEFT,
        GESTURE_SWIPE_RIGHT,
        GESTURE_SWIPE_UP,
        GESTURE_SWIPE_DOWN,
        GESTURE_TAP_LEFT_SIDE,
        GESTURE_TAP_RIGHT_SIDE,
        GESTURE_TAP_CENTER,
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void doGesture(GestureType type) {
        SharedPreferences pref = PreferenceManager
            .getDefaultSharedPreferences(this);
        int BORDER = Integer.parseInt(pref.getString("border", "10"));
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        //final int BORDER = 10;
        final int leftX = BORDER;
        final int midX = displayMetrics.widthPixels / 2;
        final int rightX = displayMetrics.widthPixels - 1 - BORDER;
        final int topY = displayMetrics.heightPixels / 4;
        final int midY = displayMetrics.heightPixels / 2;
        final int botY = displayMetrics.heightPixels - 1 - BORDER;
        final int GESTURE_DURATION = 100;
        final int GESTURE_DURATION_slow = 500;

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();
        switch(type) {
        case GESTURE_TAP_LEFT_SIDE: 
            path.moveTo(leftX, midY);
            gestureBuilder.addStroke(new GestureDescription.StrokeDescription(
                path, 0, GESTURE_DURATION));
            Log.i("accessibilityservice", "gesture: tap left side");
            break;
        case GESTURE_TAP_RIGHT_SIDE: 
            path.moveTo(rightX, midY);
            gestureBuilder.addStroke(new GestureDescription.StrokeDescription(
                path, 0, GESTURE_DURATION));
            Log.i("accessibilityservice", "gesture: tap right side");
            break;
        case GESTURE_TAP_CENTER:
            path.moveTo(midX, midY);
            gestureBuilder.addStroke(new GestureDescription.StrokeDescription(
                path, 0, GESTURE_DURATION));
            Log.i("accessibilityservice", "gesture: tap center");
            break; 
        case GESTURE_SWIPE_LEFT: 
            path.moveTo(rightX, midY);
            path.lineTo(leftX, midY);
            gestureBuilder.addStroke(new GestureDescription.StrokeDescription(
                path, 0, GESTURE_DURATION_slow));
            Log.i("accessibilityservice", "gesture: tap left side");
            break;
        case GESTURE_SWIPE_RIGHT: 
            path.moveTo(leftX, midY);
            path.lineTo(rightX, midY);
            gestureBuilder.addStroke(new GestureDescription.StrokeDescription(
                path, 0, GESTURE_DURATION_slow));
            Log.i("accessibilityservice", "gesture: tap left side");
            break;
        case GESTURE_SWIPE_UP: 
            path.moveTo(midX, botY);
            path.lineTo(midX, topY);
            gestureBuilder.addStroke(new GestureDescription.StrokeDescription(
                path, 0, GESTURE_DURATION_slow));
            Log.i("accessibilityservice", "gesture: tap left side");
            break;
        case GESTURE_SWIPE_DOWN: 
            path.moveTo(leftX, topY);
            path.lineTo(rightX, botY);
            gestureBuilder.addStroke(new GestureDescription.StrokeDescription(
                path, 0, GESTURE_DURATION_slow));
            Log.i("accessibilityservice", "gesture: tap left side");
            break;

        default:
            Log.i("accessibilityservice", "gesture: unsupported gesture");
            return;
        } 

        Log.d("accessibilityservice", "gesture started");
        dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d("accessibilityservice", "gesture completed");
                super.onCompleted(gestureDescription);
            }
        }, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i("accessibilityservice", "onAccessibilityEvent called, event: " + event.toString());
        if(event.getEventType() == AccessibilityEvent.TYPE_ANNOUNCEMENT) {
            Log.i("accessibilityservice", "got announce");
            String text = getTextFor(event);
            Log.i("accessibilityservice", "announce event text is [" + text + "]");

            if(text.equals("next")) {
                doGesture(GestureType.GESTURE_TAP_RIGHT_SIDE);
            }
            else if(text.equals("previous")) {
                doGesture(GestureType.GESTURE_TAP_LEFT_SIDE);
            }
            else if(text.equals("left")) {
                doGesture(GestureType.GESTURE_SWIPE_LEFT);
            }
            else if(text.equals("right")) {
                doGesture(GestureType.GESTURE_SWIPE_RIGHT);
            }
            else if(text.equals("up")) {
                doGesture(GestureType.GESTURE_SWIPE_UP);
            }
            else if(text.equals("down")) {
                doGesture(GestureType.GESTURE_SWIPE_DOWN);
            }
            else if(text.equals("center")) {
                doGesture(GestureType.GESTURE_TAP_CENTER);
            }
            else if(text.equals("customization")){
                //this deals with customized gesture
                Log.i("accessibilityservice", "start customized gesture");
                String gesturePoints = getGestureFromEvent(event);
                List<Point> pointList = convertStrToPointlist(gesturePoints);
                doCustomizedGesture(pointList);
            }else{
                Log.i("accessibilityservice", "fail to identify gesture");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void doCustomizedGesture(List<Point> pointList) {
        Log.i("customized gesture","start");
        final int GESTURE_DURATION = 10;
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();
        if(pointList.size() == 0)
            return;

        Point firstPoint = pointList.get(0);
        path.moveTo(firstPoint.x, firstPoint.y);

        for(int i=1;i<pointList.size();i++){
            Point nextPoint = pointList.get(i);
            path.lineTo(nextPoint.x, nextPoint.y);
        }
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(
                path, 0, GESTURE_DURATION));

        Log.i("accessibilityservice", "gesture: customized gesture");

        dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d("accessibilityservice", "gesture completed");
                super.onCompleted(gestureDescription);
            }
        }, null);

    }
    @Override
    public void onInterrupt() {
    
    }

    class Point{
        float x;
        float y;

        public Point(float x, float y){
            this.x = x;
            this.y = y;
        }
    }

    public List<Point> convertStrToPointlist(String str){
        //convert json array string to list of points
        List<Point> list = new ArrayList<Point>();
        try {
            JSONArray jsonArray = new JSONArray(str);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject explrObject = jsonArray.getJSONObject(i);
                list.add(new Point(Float.valueOf(String.valueOf(explrObject.get("x"))), Float.valueOf(String.valueOf(explrObject.get("y")))) );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return list;
    }
}

