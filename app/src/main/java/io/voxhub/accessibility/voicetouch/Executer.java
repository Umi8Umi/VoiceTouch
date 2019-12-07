package io.voxhub.accessibility.voicetouch;
import io.voxhub.accessibility.voicetouch.database.CommandData;
import io.voxhub.accessibility.voicetouch.database.VoiceTouchDbHelper;
import jp.naist.ahclab.speechkit.logs.MyLog;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

public class Executer{

    private SimpleActivity simpleActivity;
    private AccessibilityManager manager;
    private LinkedList<Command> commandList = new LinkedList<Command>();
    private HashMap<String, Command> commandsMap = new HashMap<String, Command>();
    private boolean isRunningOne = false;
    private VoiceTouchDbHelper db;
    
    public Executer (SimpleActivity s) {
        simpleActivity = s;
        manager = (AccessibilityManager)s.getSystemService(Context.ACCESSIBILITY_SERVICE);
        db = new VoiceTouchDbHelper(s);
    }

    public void sendAccessibilityEvent(String string) {
        AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent
            .TYPE_ANNOUNCEMENT);
        event.setClassName(getClass().getName());
        event.setPackageName(simpleActivity.getPackageName());
        event.setEnabled(true);
        event.getText().clear();
        event.getText().add(string);
        event.getText().add(";");

//        MyLog.i("SimpleActivity event: " + event.toString());
        if(simpleActivity.dispatchPopulateAccessibilityEvent(event)) {
            MyLog.i("SimpleActivity dispatchPopulateAccessibilityEvent says OK");
        }
        else {
            MyLog.i("SimpleActivity dispatchPopulateAccessibilityEvent says ???");
        }
        manager.sendAccessibilityEvent(event);
        MyLog.i("SimpleActivity sent accessibility event");
    }

    public void sendAccessibilityEvent(String string, String gesturePoints) {
        AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent
                .TYPE_ANNOUNCEMENT);
        event.setClassName(getClass().getName());
        event.setPackageName(simpleActivity.getPackageName());
        event.setEnabled(true);
        event.getText().clear();
        event.getText().add(string);
        event.getText().add(";");
        event.getText().add(gesturePoints);
        event.getText().add(";");


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

        //get all commands and related gestures from db
        List<String> commandList = db.getAllCommandNames();
        for(String command: commandList){
            CommandData commandData = db.getCommand(command);
            String callname = commandData.getAlias();
            String[] gestures = commandData.getGestureArray();
            commandsMap.put(callname,        new CustomizedCommand(gestures));
            Log.i("register command",callname);
        }



    }

    private void runOne() {
        isRunningOne = true;
        Command c;
        int counter = 0;
        boolean firstCommand = true;
        final Handler handler = new Handler();
        while(commandList.size() > 0) {
            c = commandList.removeFirst();
            MyLog.i("commandList size after remove: " + commandList.size());
            if(firstCommand) {
                c.run();
                firstCommand = false;
            }
            else{
                counter += 1000;
                handler.postDelayed(c, counter);
                MyLog.i("postDelayed happened");
            }
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
                    if(c.isCombo()){
                        CustomizedCommand combo = (CustomizedCommand)c;
                        combo.addAllCommandsToList();
                    }else {
                        commandList.add(c);
                    }
                    MyLog.i("commandList size: " + commandList.size());
                } else{
                    c.run();
                }


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
        public boolean isCombo() {return false;}
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


    class CustomizedCommand extends Command {
        private List<SingleGestureCommand> list;

        CustomizedCommand (String[] gestureList) {
            this.list = new ArrayList<SingleGestureCommand>();
            for(String gesture: gestureList){
                String gesturePoints = db.getGesturePoints(gesture);
                this.list.add(new SingleGestureCommand(gesture, gesturePoints));
            }

        }


        public void addAllCommandsToList() {
            MyLog.i("Customized command got called");
            for(SingleGestureCommand command: list){
                commandList.add(command);
            }
        }

        @Override
        public void run() {
            MyLog.i("This is combo command, this command should not be ran");
        }

        @Override
        public boolean isCombo() {
            return true;
        }
    }


    class SingleGestureCommand extends Command {

        private String name;
        private String points;

        public SingleGestureCommand(String n, String p){
            name = n;
            points = p;
        }

        @Override
        public void run() {
            MyLog.i("SimpleActivity spotted gesture :" + name);

            sendAccessibilityEvent("customization", points);
            MyLog.i("SimpleActivity sent gesture:" + name);
            MyLog.i("SimpleActivity sent gesture points:" + points);
        }
    }
}
