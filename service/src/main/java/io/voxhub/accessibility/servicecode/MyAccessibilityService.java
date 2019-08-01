package io.voxhub.accessibility.servicecode;
//package com.example.android.apis.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

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

    private enum GestureType {
        GESTURE_SWIPE_LEFT,
        GESTURE_SWIPE_RIGHT,
        GESTURE_SWIPE_UP,
        GESTURE_SWIPE_DOWN,
        GESTURE_TAP_LEFT_SIDE,
        GESTURE_TAP_RIGHT_SIDE,
        GESTURE_TAP_CENTER,
    }

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

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i("accessibilityservice", "onAccessibilityEvent called, event: " + event.toString());
        if(event.getEventType() == AccessibilityEvent.TYPE_ANNOUNCEMENT) {
            Log.i("accessibilityservice", "got announce");
            String text = getTextFor(event);
            Log.i("accessibilityservice", "announce event text is [" + text + "]");

            if(text.equals("next")) {
                doGesture(GestureType.GESTURE_TAP_LEFT_SIDE);
            }
            else if(text.equals("previous")) {
                doGesture(GestureType.GESTURE_TAP_RIGHT_SIDE);
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
        }
    }
    @Override
    public void onInterrupt() {
    
    }
}

