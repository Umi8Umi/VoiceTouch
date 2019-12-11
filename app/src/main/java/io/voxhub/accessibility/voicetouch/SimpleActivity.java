package io.voxhub.accessibility.voicetouch;
import io.voxhub.accessibility.voicetouch.command.CommandListActivity;
import io.voxhub.accessibility.voicetouch.gesture.GestureListActivity;
import jp.naist.ahclab.speechkit.logs.MyLog;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
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

public class SimpleActivity extends Activity {

    final String TAG = "SimpleActivity";

    private Executer executer;
    private AccessibilityManager manager;

    private Button btn_start;
   // private Button btn_setting;
    private ImageButton btn_setting;
    private enum ConnectionIcon { DISCONNECTED, CONNECTED, NOWORKERS };
    private Map<ConnectionIcon, ImageView> connectionIcon = new HashMap<ConnectionIcon, ImageView>();

    //installed-app-list
    //private Context mContext;
    //private Activity mActivity;

    private ImageButton mic;
    private TextView connection;
    private TextView noWorkersText;
    private Button btn_stop;
    private Button btn_enable;
    private Button btn_overlay;
//    private Button btn_about;
    private Button btn_gesture;
    private Button btn_command;
   // private Button btn_help;
    private ImageButton btn_help;
    private ProgressBar progress;
   // private ProgressBar pb;
    private Overlay overlay = null;
    /*private*/ EditText ed_result;
    private boolean requestListen = false;
    private boolean askedForOverlayPermission;

    private static ServerInfo serverInfo; 
    private static Recognizer _currentRecognizer;
    private static ThreadAdapter _currentListener;

    void init_speechkit(ServerInfo serverInfo){
        if (_currentRecognizer == null) {
            SpeechKit _speechKit = SpeechKit.initialize(getApplication().getApplicationContext(), "", "", serverInfo);
            _currentListener = new ThreadAdapter(new SpeechkitCode());
            _currentRecognizer = _speechKit.createRecognizer(_currentListener);
            _currentRecognizer.connect();
        }
        else {
            _currentListener = new ThreadAdapter(new SpeechkitCode());
            _currentRecognizer.useListener(_currentListener);
        }
    }

    void make_speechkit() {
        if (!makeServerInfo() && _currentRecognizer != null){
            return;
        }
        
        destroy_speechkit();
        requestMicPermissions();
    }

    private boolean haveMicPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED;
    }

    void destroy_speechkit() {
        if (_currentRecognizer != null) {
            stopListening();
            _currentRecognizer.shutdownThreads();
           // _currentRecognizer.cancel();
            _currentRecognizer = null;
        }
        if (_currentListener != null) {
            _currentListener.stop();
            _currentListener = null;
        }
    }

    void requestMicPermissions(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;  // how do we handle this case?

        if(haveMicPermissions()) {
            init_speechkit(serverInfo);
        }
        else {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 101 && grantResults.length > 0){
            boolean granted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
            if(granted) {
                init_speechkit(serverInfo);
            }
            else {
                requestMicPermissions();  // infinite loop
                Toast.makeText(this,
                    "App requires audio permissions", Toast.LENGTH_LONG).show();
                //finishAffinity();  // exit app
            }
        }
    }

    public boolean makeServerInfo() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String newserver = pref.getString("server", "silvius-server.voxhub.io");
        int newport = Integer.parseInt(pref.getString("port", "8023"));
        if (serverInfo == null 
            || !serverInfo.getAddr().equals(newserver)
            || serverInfo.getPort() != newport) {

            serverInfo = new ServerInfo(newserver, newport);
            serverInfo.setAppSpeech(this.getResources().getString(
                R.string.default_server_app_speech));
            serverInfo.setAppStatus(this.getResources().getString(
                R.string.default_server_app_status));
            return true;
        }
        else return false;
    }
    
    public void makeOverlay(boolean enabled) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        if(!Settings.canDrawOverlays(this)) return;

        overlay = Overlay.getInstance();
        if(overlay == null) overlay = new Overlay(this);

        if(enabled && requestListen) overlay.show();
        else overlay.hide();
    }

    public void makeOverlay() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        makeOverlay(pref.getBoolean("overlay_enabled", true));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateOverlayUI() {
        makeOverlay();
        btn_overlay.setVisibility(Settings.canDrawOverlays(this) ? View.GONE : View.VISIBLE);
    }

    private void startListening() {
        startListening(true);
    }
    private void startListening(boolean askRecognizer) {
        requestListen = true;
        MyLog.i("Setting requestListen to " + requestListen);
        if(askRecognizer) _currentRecognizer.start();
        makeOverlay();
        progress.setVisibility(View.VISIBLE);
        btn_stop.setVisibility(View.VISIBLE);
        
        if (btn_enable.getVisibility() == View.VISIBLE) {
            new AlertDialog.Builder(SimpleActivity.this)
                .setTitle("Warning")
                .setMessage("Accessibility Settings have not been enabled")
                .setPositiveButton("close", null)
                .show();
            return;
        }
        SharedPreferences pref = PreferenceManager
            .getDefaultSharedPreferences(SimpleActivity.this);
        if(pref.getBoolean("autoBackground", true)) {
            btn_start.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        executer.bringApplicationToBackground();
                    }
                }, 500);
            Toast.makeText(getApplicationContext(),
                "Please launch e-book reader",
                Toast.LENGTH_SHORT).show();
        }
    }

    public void stopListening() {
        requestListen = false;
        MyLog.i("Setting requestListen to " + requestListen);
        _currentRecognizer.stopRecording();
        makeOverlay();
        progress.setVisibility(View.INVISIBLE);
        btn_stop.setVisibility(View.INVISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateState() {
        btn_enable.setVisibility(manager.isEnabled() ? View.GONE : View.VISIBLE); 
        updateOverlayUI();
    }

    boolean VersionUpgraded() {
        try {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            MyLog.i("current version: [" + version + "]");
            if (!(pref.getString("currentVersion", version).equals(version))) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("currentVersion", version);
                editor.apply();
                return true;
            }
            else return false;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false; // should this be set to t or f ???
        }
    }

    private void showConnectionIcon(ConnectionIcon icon) {
        for(ConnectionIcon i : ConnectionIcon.values()) {
            ImageView image = connectionIcon.get(i);
            if(image != null) {
                image.setVisibility(i == icon ? View.VISIBLE : View.INVISIBLE);
            }
        }
        noWorkersText.setVisibility(icon == ConnectionIcon.NOWORKERS
            ? View.VISIBLE : View.INVISIBLE);
    }

    /*protected HashMap<String,String> getInstalledPackages(){
        PackageManager packageManager = getPackageManager();

        // Initialize a new intent
        Intent intent = new Intent(Intent.ACTION_MAIN,null);
        // Set the intent category
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        // Set the intent flags
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                |Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        // Initialize a new list of resolve info
        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(intent,0);

        // Initialize a new hash map of package names and application label
        HashMap<String,String> map = new HashMap<String,String>();

        // Loop through the resolve info list
        for(ResolveInfo resolveInfo : resolveInfoList){
            // Get the activity info from resolve info
            ActivityInfo activityInfo = resolveInfo.activityInfo;

            // Get the package name
            String packageName = activityInfo.applicationInfo.packageName;

            // Get the application label
            String label = (String) packageManager.getApplicationLabel(activityInfo.applicationInfo);

            // Put the package name and application label to hash map
            map.put(packageName,label);
        }
        return map;
    }*/

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyLog.i("onCreate has been entered");
        manager = (AccessibilityManager)this.getSystemService(Context.ACCESSIBILITY_SERVICE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictation);

        executer = new Executer(this);
        executer.constructMap();


        //installed-app-list
        //mContext = getApplicationContext();
        //mActivity = SimpleActivity.this;
        //HashMap<String,String> map = getInstalledPackages();
        //final String[] values = map.values().toArray(new String[map.size()]);
        //final String[] keys = map.keySet().toArray(new String[map.size()]);

        btn_start = (Button)findViewById(R.id.btn_start);
        btn_setting = (ImageButton)findViewById(R.id.btn_setting);
        connection = (TextView)findViewById(R.id.connection);
        noWorkersText = (TextView)findViewById(R.id.noWorkersText);
        connectionIcon.put(ConnectionIcon.CONNECTED, (ImageView)findViewById(R.id.connected));
        connectionIcon.put(ConnectionIcon.DISCONNECTED, (ImageView)findViewById(R.id.disconnected));
        connectionIcon.put(ConnectionIcon.NOWORKERS, (ImageView)findViewById(R.id.noworkers));
        mic = (ImageButton)findViewById(R.id.mic);
        btn_stop = (Button) this.findViewById(R.id.btn_stop);
        btn_enable = (Button) this.findViewById(R.id.btn_enable);
        btn_overlay = (Button) this.findViewById(R.id.btn_overlay); 
//        btn_about = (Button) this.findViewById(R.id.btn_about);
        btn_gesture = (Button) this.findViewById(R.id.btn_gesture);
        btn_command = (Button) this.findViewById(R.id.btn_command);
        btn_help = (ImageButton) this.findViewById(R.id.btn_help);
        progress = (ProgressBar)findViewById(R.id.progress_listening);
        ed_result = (EditText)findViewById(R.id.ed_result);


        if(_currentRecognizer == null || makeServerInfo()) {
            make_speechkit();
        } else {
            init_speechkit(serverInfo);
            // inherit old audio thread
            if(_currentRecognizer.isRecording()) {
                startListening(false);
            }
        }

        if (VersionUpgraded())
            new AlertDialog.Builder(SimpleActivity.this)
                .setTitle("Re-enable Accessibility Settings")
                .setMessage("You have recently updated this app. " +
                    "If the service was previously malfunctioning, you may wish to reenable it.")
                .setPositiveButton("Re-enable", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                            android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Dismiss", null)
                .show();

        //installed-app-list
        /*final PreferenceCategory cat = new PreferenceCategory(getApplicationContext());
        cat.setTitle(R.string.installedApps);
        for (final String appName :map.keySet()) {
            final Preference app = new Preference(getApplicationContext());
            app.setTitle(appName);
            app.setSummary(map.get(appName));
            cat.addPreference(app);
        }*/

        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // Open settings page
                Intent intent = new Intent(SimpleActivity.this, SettingsActivity.class);
                startActivity(intent);
                MyLog.i("SettingsActivity intent started");
            }
        });

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_stop.getVisibility() == View.INVISIBLE)
                    startListening();
                else stopListening();
            }
        });
        
        // Button start
        MyLog.i("onCreate - buttons made");
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startListening();
            }
        });
        
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopListening();
            }
        });
        
        btn_enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider
                    .Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        });

//        btn_about.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) { // Open about page
//                Intent intent = new Intent(SimpleActivity.this, AboutActivity.class);
//                startActivity(intent);
//            }
//        });

        btn_gesture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // Open gesture page
                Intent intent = new Intent(SimpleActivity.this, GestureListActivity.class);
                startActivity(intent);
            }
        });

        btn_command.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // Open command page
                Intent intent = new Intent(SimpleActivity.this, CommandListActivity.class);
                startActivity(intent);
            }
        });

        btn_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // Open help page
//                Intent intent = new Intent(SimpleActivity.this, HelpActivity.class);
//                startActivity(intent);
                Intent intent = new Intent(SimpleActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        /* Stops recording once dialog goes away
        lst_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                _currentRecognizer.stopRecording();
            }
        });*/

        btn_overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, 
                    Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 8888);
            }
        });

        MyLog.i("onCreate - OnClickListeners are made");

        updateState();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        MyLog.i("requestCode: " + requestCode + " resultCode: " + resultCode + " data: " + data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 8888) {
            MyLog.i("can draw overlays: " + (Settings.canDrawOverlays(this)));
            final SimpleActivity outer = this;
            btn_overlay.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        outer.updateOverlayUI();
                    }
                }, 500);

            if(resultCode == RESULT_OK){
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(getApplicationContext(),
                        "ACTION_MANAGE_OVERLAY_PERMISSION Permission Denied",
                        Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        MyLog.i("onDestroy");
       // _currentListener.stop();
       // _currentRecognizer.shutdownThreads();
        destroy_speechkit();
        MyLog.i("SimpleActivity stopped listening");
        if(Overlay.getOverlayExists()) {
            Overlay.getInstance().hide();  // destroy?
        }
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        make_speechkit();
        updateState();

        MyLog.i("onResume called");

        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(btn_stop.getVisibility() == View.VISIBLE)
            outState.putString("stopBtnState", "visible");
        else
            outState.putString("stopBtnState", "invisible");
    }
        
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //--- inner class ~ SpeechkitCode ---//
    class SpeechkitCode implements Recognizer.Listener {

        @Override
        public void onReady(String reason) {
            btn_start.setEnabled(true);
            mic.setVisibility(View.VISIBLE);
            showConnectionIcon(ConnectionIcon.CONNECTED);
            //Toast.makeText(getApplicationContext(),"Connected to server: "+reason,
                //Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNotReady(String reason) {
            btn_start.setEnabled(false);
            mic.setVisibility(View.INVISIBLE);
            showConnectionIcon(ConnectionIcon.NOWORKERS);
            //Toast.makeText(getApplicationContext(),
                //"Server connected, but not ready, reason: "+reason,
                //Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onUpdateStatus(SpeechKit.Status status) {
          /*  Toast.makeText(getApplicationContext(),"Status changed: " + status.name(),
                    Toast.LENGTH_SHORT).show();*/
            MyLog.i("SimpleActivity has new status: " + status.name());        
        }

        @Override
        public void onFinalResult(String result) {
            ed_result.setText(result + ".");
            if (Overlay.getOverlayExists())
                overlay.setText(result + ".");
            executer.executeCommand(result);
        }


        @Override
        public void onNoConnection(String reason) {
            btn_start.setEnabled(false);
            mic.setVisibility(View.INVISIBLE);
            showConnectionIcon(ConnectionIcon.DISCONNECTED);
        }

        @Override
        public void onFinish(String reason) {
            _currentRecognizer.stopRecording();
            MyLog.i("SimpleActivity stopped listening");
        }

        @Override
        public void onPartialResult(String result) {
            ed_result.setText(result);
            if(Overlay.getOverlayExists())
                overlay.setText(result);
        }

        @Override
        public void onRecordingBegin() {
            ed_result.setText("");        
        }

        @Override
        public void onRecordingDone() {
            if (requestListen) {
                _currentRecognizer.start();
                MyLog.i("SimpleActivity restarted listening.");
            }
        }

        @Override
        public void onError(Exception error) {
    //        Toast.makeText(getApplicationContext(),"Error: " + error,
    //                Toast.LENGTH_SHORT).show();
            MyLog.i("SimpleActivity has error: " + error);
            
            for (StackTraceElement e : error.getStackTrace()) {
                MyLog.i("SimpleActivity stack trace: " + e.toString());
            }    
            _currentRecognizer.stopRecording();
            if (requestListen) {
                _currentRecognizer.start();
                MyLog.i("SimpleActivity restarted listening.");
            }
        }

    }

}
