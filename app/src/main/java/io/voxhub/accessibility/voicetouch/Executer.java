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
import java.util.LinkedList;

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
    private LinkedList<Command> commandList = new LinkedList<Command>();
    private HashMap<String, Command> commandsMap = new HashMap<String, Command>();
    private boolean isRunningOne = false;
    
    public Executer (SimpleActivity s) {
        simpleActivity = s;
        manager = (AccessibilityManager)s.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    public void sendAccessibilityEvent(String string) {
        AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent
            .TYPE_ANNOUNCEMENT);
        event.setClassName(getClass().getName());
        event.setPackageName(simpleActivity.getPackageName());
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

    void bringApplicationToBackground() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        simpleActivity.startActivity(i);
    }
    
    private void bringApplicationToForeground() {
        ActivityManager am =
            (ActivityManager) simpleActivity.getSystemService(simpleActivity.ACTIVITY_SERVICE);
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
                        .equals(simpleActivity.getPackageName())){
                        
                        am.moveTaskToFront(tasksList.get(i).id, 0);
                    }
                }
            }
        }
    }

    void constructMap() {
        commandsMap.put("next page",        new AccessibilityCommand("next"));
        commandsMap.put("previous page",    new AccessibilityCommand("previous"));
        commandsMap.put("swipe up",         new AccessibilityCommand("up"));
        commandsMap.put("swipe down",       new AccessibilityCommand("down"));
        commandsMap.put("swipe left",       new AccessibilityCommand("left"));
        commandsMap.put("swipe right",      new AccessibilityCommand("right"));
        commandsMap.put("center",           new AccessibilityCommand("center"));
        commandsMap.put("stop listening",   new Command() {
                                                @Override
                                                public void run() {
                                                    MyLog.i("SimpleActivity spotted " +
                                                        "stop listening");
                                                    simpleActivity.stopListening();
                                                    MyLog.i("SimpleActivity sent " +
                                                        "stop listening");
                                                }
                                                public boolean isInstant() {return true;} });
        commandsMap.put("go home",          new Command() {
                                                @Override
                                                public void run() {
                                                    MyLog.i("SimpleActivity spotted go home");
                                                    bringApplicationToBackground();
                                                    MyLog.i("SimpleActivity sent go home");
                                                } });
        commandsMap.put("foreground",       new Command() {
                                                @Override
                                                public void run() {
                                                    MyLog.i("SimpleActivity spotted background");
                                                    bringApplicationToForeground();
                                                    MyLog.i("SimpleActivity sent background");
                                                } });
    }

    private void runOne() {
        isRunningOne = true;
        Command c;
        boolean firstCommand = true;
        final Handler handler = new Handler();
        while(commandList.size() > 0) {
            c = commandList.removeFirst();
            MyLog.i("commandList size after remove: " + commandList.size());
            if(firstCommand) {
                c.run();
                firstCommand = false;
            }
            //MyLog.i("just ran command and list size is: " + commandList.size());
            else{
                handler.postDelayed(c, 1000);
                MyLog.i("postDelayed happened");
            }
            /*try {
                synchronized (simpleActivity) {
                    simpleActivity.wait(500);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }*/
        }
        isRunningOne = false;
    }

    public void executeCommand(String s) {
        if(!manager.isEnabled()) { 
            simpleActivity.ed_result.setText(s + "...[service not running]");
            MyLog.i("SimpleActivity manager not enabled");
            return;
        }
        String canonical = filterText(s);
        MyLog.i("SimpleActivity recognized [" + canonical + "]");
        String[] words = canonical.split(" ");
        StringBuilder sb = new StringBuilder();
        Command c;
        for(String word : words) {
            if(sb.length() > 0) sb.append(" ");
            sb.append(word);
            if(commandsMap.containsKey(sb.toString()) == false)
                continue;
            else{
                MyLog.i("found command: " + sb.toString());
                c = commandsMap.get(sb.toString());
                if (!c.isInstant()){
                    commandList.add(c);
                    MyLog.i("commandList size: " + commandList.size());}
                else c.run();
                sb.delete(0,sb.length());
            }
        }
        if(!isRunningOne && commandList.size() > 0)
            MyLog.i("runOne is about to be entered");
            runOne();
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
            MyLog.i("SimpleActivity spotted " + action);
            sendAccessibilityEvent(action);
            MyLog.i("SimpleActivity sent " + action);
        }
    }
}





            /*if (canonical.equals("go back")) {
                MyLog.i("SimpleActivity spotted background");
                onBackPressed();
                MyLog.i("SimpleActivity sent background");
            }*/
/*        
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
        */
