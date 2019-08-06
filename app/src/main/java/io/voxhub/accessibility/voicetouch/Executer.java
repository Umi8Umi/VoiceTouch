package io.voxhub.accessibility.voicetouch;
import jp.naist.ahclab.speechkit.logs.MyLog;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import jp.naist.ahclab.speechkit.Recognizer;
import jp.naist.ahclab.speechkit.ServerInfo;
import jp.naist.ahclab.speechkit.SpeechKit;
import jp.naist.ahclab.speechkit.view.ListeningDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.Manifest;
import android.net.Uri;
import android.provider.Settings;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat; 
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.Gravity;
import android.view.ViewGroup;

public class Executer{

    private SimpleActivity simpleActivity;
    private AccessibilityManager manager;
    private List<Command> exeList;
    private HashMap<String, Command> commandsMap;
    
    public Executer (SimpleActivity s) {
        simpleActivity = s;
        manager = (AccessibilityManager)s.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    public void sendAccessibilityEvent(String string) {
        AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent
            .TYPE_ANNOUNCEMENT);
        event.setClassName(getClass().getName());
        event.setPackageName(this.getPackageName());
        event.setEnabled(true);
        event.getText().clear();
        event.getText().add(string);
        event.getText().add(":");
        //MyLog.i("SimpleActivity event: " + event.toString());
        if(simpleActivity.dispatchPopulateAccessibilityEvent(event)) {
            MyLog.i("SimpleActivity dispatchPopulateAccessibilityEvent says OK");
        }
        else {
            MyLog.i("SimpleActivity dispatchPopulateAccessibilityEvent says ???");
        }
        manager.sendAccessibilityEvent(event);
        MyLog.i("SimpleActivity sent accessibility event");
    }

    private String filterText(String input) {
        String[] words = input.split(" ");
        ArrayList<String> result = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        for(String word : words) {
            if(word.charAt(0) == '[' && word.charAt(word.length()-1) == ']') continue;
            if(word.charAt(0) == '<' && word.charAt(word.length()-1) == '>') continue;
            if(sb.length() > 0) sb.append(" ");
            sb.append(word);
        }
        return sb.toString();
    }

    private void bringApplicationToBackground() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
    }
    
    private void bringApplicationToForeground() {
        ActivityManager am =
            (ActivityManager) getSystemService(SimpleActivity.this.ACTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<ActivityManager.AppTask> tasksList = am.getAppTasks();
            for (ActivityManager.AppTask task : tasksList){
              task.moveToFront();
            }
        }
        else{
            List<ActivityManager.RunningTaskInfo> tasksList =
                am.getRunningTasks(Integer.MAX_VALUE);
            if(!tasksList.isEmpty()){
                int nSize = tasksList.size();
                for(int i = 0; i < nSize;  i++){
                    if(tasksList.get(i).topActivity.getPackageName()
                        .equals(getPackageName())){
                        
                        am.moveTaskToFront(tasksList.get(i).id, 0);
                    }
                }
            }
        }
    }

    abstract class Command implements Runnable {
        public abstract void run();
        public boolean isInstant() {return false;}
    }

    class AccessibilityCommand extends Command {
        private String action;

        AccessibilityCommand (String a) {
            action = a;
        }
        
        public void run() {
            sendAccessibilityEvent(action);
        }
    }
}



    private void constructMap() {
        commandsMap.put("next page",        new AccessibilityCommand("next"));
        commandsMap.put("previous page",    new AccessibilityCommand("previous"));
        commandsMap.put("swipe up",         new AccessibilityCommand("swipe up"));
        commandsMap.put("swipe down",       new AccessibilityCommand("swipe down"));
        commandsMap.put("go home",          new AccessibilityCommand("go home"));
        commandsMap.put("center",           new AccessibilityCommand("center"));
        commandsMap.put("stop listening", );
        commandsMap.put("foreground", );
    }


            String canonical = filterText(result);
            MyLog.i("SimpleActivity recognized [" + canonical + "]");

            if (canonical.equals("stop listening")) {
                MyLog.i("SimpleActivity spotted stop listening");
                stopListening();
            }
            else if (canonical.equals("foreground")) {
                MyLog.i("SimpleActivity spotted foreground");
                bringApplicationToForeground();
                MyLog.i("SimpleActivity sent foreground");
            }
            else if (canonical.equals("go home")) { 
                MyLog.i("SimpleActivity spotted background");
                bringApplicationToBackground();
                MyLog.i("SimpleActivity sent background");
            }
            /*if (canonical.equals("back")) {
                MyLog.i("SimpleActivity spotted background");
                onBackPressed();
                MyLog.i("SimpleActivity sent background");
            }*/

            else if(!manager.isEnabled()) { // This will never be called bc start button
                ed_result.setText(result + "...[service not running]");

                MyLog.i("SimpleActivity manager not enabled");
                return;
            }

            else if (canonical.equals("next page")) {
                MyLog.i("SimpleActivity spotted next page");
                sendAccessibilityEvent("next");
                MyLog.i("SimpleActivity sent next page");
            }
            else if (canonical.equals("previous page")) {
                MyLog.i("SimpleActivity spotted previous page");
                sendAccessibilityEvent("previous");
                MyLog.i("SimpleActivity sent previous page");
            }
            else if (canonical.equals("swipe left")) {
                MyLog.i("SimpleActivity spotted next page");
                sendAccessibilityEvent("left");
                MyLog.i("SimpleActivity sent next page");
            }
            else if (canonical.equals("swipe right")) {
                MyLog.i("SimpleActivity spotted next page");
                sendAccessibilityEvent("right");
                MyLog.i("SimpleActivity sent next page");
            }
            else if (canonical.equals("swipe up")) {
                MyLog.i("SimpleActivity spotted previous page");
                sendAccessibilityEvent("up");
                MyLog.i("SimpleActivity sent previous page");
            }
            else if (canonical.equals("swipe down")) {
                MyLog.i("SimpleActivity spotted previous page");
                sendAccessibilityEvent("down");
                MyLog.i("SimpleActivity sent previous page");
            }
            else if (canonical.equals("center")) {
                MyLog.i("SimpleActivity spotted center");
                sendAccessibilityEvent("center");
                MyLog.i("SimpleActivity sent center");
            }
            else {
                onFinalResultLong(canonical);
            }
            /*if (canonical.equals("unknowncommande")) {
                MyLog.i("SimpleActivity spotted ");
                if(Overlay.getOverlayExists())
                    overlay.hide();
                MyLog.i("SimpleActivity paused listening");
            }*/
        
        public void onFinalResultLong(String canonical) {
            String[] tempArray = canonical.split(" ");
            final Handler handler = new Handler();
            for(int i = 0; i < tempArray.length; i++) {
                if (tempArray[i].equals("center")) {
                    MyLog.i("SimpleActivity spotted center");
                    if(i == 0)
                        sendAccessibilityEvent("center");
                    MyLog.i("SimpleActivity sent center");
//                    sleep(1000);
                    if(i > 0) {
//                        final Handler handler = new Handler();
                        handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    sendAccessibilityEvent("center");
                                }
                            }, 500 * i + 500);
                    }
                }
                else if (i < (tempArray.length - 1) && 
                    (tempArray[i]+ " " + tempArray[i+1]).equals("stop listening")) {
                    MyLog.i("SimpleActivity spotted stop listening");
                    stopListening();
                }
                else if ((tempArray[i]).equals("foreground")) {
                    MyLog.i("SimpleActivity spotted foreground");
                    if(i == 0)
                        bringApplicationToForeground();
                    MyLog.i("SimpleActivity sent foreground");
//                    sleep(1000);
                    if(i > 0) {
//                        final Handler handler = new Handler();
                        handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    bringApplicationToForeground();
                                }
                            }, 500 * i);
                    }
                }
                else if (i < (tempArray.length - 1) && 
                    (tempArray[i]+ " " + tempArray[i+1]).equals("go home")) { 
                    MyLog.i("SimpleActivity spotted background");
                    if(i == 0)
                        bringApplicationToBackground();
                    MyLog.i("SimpleActivity sent background");
//                    sleep(1000);
                    if(i > 0) {
//                        final Handler handler = new Handler();
                        handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    bringApplicationToBackground();
                                }
                            }, 500 * i);
                    }
                }
                else if (i < (tempArray.length - 1) && 
                    (tempArray[i]+ " " + tempArray[i+1]).equals("next page")) {
                    MyLog.i("SimpleActivity spotted next page");
                    if(i == 0)
                        sendAccessibilityEvent("next");
                    MyLog.i("SimpleActivity sent next page");
//                    sleep(1000);
                    if(i > 0) {
//                        final Handler handler = new Handler();
                        handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    sendAccessibilityEvent("next");
                                }
                            }, 500 * i);
                    }
                }
                else if (i < (tempArray.length - 1) && 
                    (tempArray[i]+ " " + tempArray[i+1]).equals("previous page")) {
                    MyLog.i("SimpleActivity spotted previous page");
                    if(i == 0)
                        sendAccessibilityEvent("previous");
                    MyLog.i("SimpleActivity sent previous page");
//                    sleep(1000);
                    if(i > 0) {
//                        final Handler handler = new Handler();
                        handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    sendAccessibilityEvent("previous");
                                }
                            }, 500 * i);
                    }
                }
                else if (i < (tempArray.length - 1) && 
                    (tempArray[i]+ " " + tempArray[i+1]).equals("swipe left")) {
                    MyLog.i("SimpleActivity spotted next page");
                    if(i == 0)
                        sendAccessibilityEvent("left");
                    MyLog.i("SimpleActivity sent next page");
//                    sleep(1000);
                    if(i > 0) {
//                        final Handler handler = new Handler();
                        handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    sendAccessibilityEvent("left");
                                }
                            }, 500 * i);
                    }
                }
                else if (i < (tempArray.length - 1) && 
                    (tempArray[i]+ " " + tempArray[i+1]).equals("swipe right")) {
                    MyLog.i("SimpleActivity spotted next page");
                    if(i == 0)
                        sendAccessibilityEvent("right");
                    MyLog.i("SimpleActivity sent next page");
//                    sleep(1000);
                    if(i > 0) {
//                        final Handler handler = new Handler();
                        handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    sendAccessibilityEvent("right");
                                }
                            }, 500 * i);
                    }
                }
                else if (i < (tempArray.length - 1) && 
                    (tempArray[i]+ " " + tempArray[i+1]).equals("swipe up")) {
                    MyLog.i("SimpleActivity spotted previous page");
                    if(i == 0)
                        sendAccessibilityEvent("up");
                    MyLog.i("SimpleActivity sent previous page");
//                    sleep(1000);
                    if(i > 0) {
//                        final Handler handler = new Handler();
                        handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    sendAccessibilityEvent("up");
                                }
                            }, 500 * i);
                    }
                }
                else if (i < (tempArray.length - 1) && 
                    (tempArray[i]+ " " + tempArray[i+1]).equals("swipe down")) {
                    MyLog.i("SimpleActivity spotted previous page");
                    if(i == 0)
                        sendAccessibilityEvent("down");
                    MyLog.i("SimpleActivity sent previous page");
 //                   sleep(1000);
                    if(i > 0) {
//                        final Handler handler = new Handler();
                        handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    sendAccessibilityEvent("down");
                                }
                            }, 500 * i);
                    }
                }
                MyLog.i("onFinalResultLong: " + i);
            }
        }
