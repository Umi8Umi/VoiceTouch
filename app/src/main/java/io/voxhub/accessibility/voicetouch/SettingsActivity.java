package io.voxhub.accessibility.voicetouch;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.EditTextPreference;
import android.preference.SwitchPreference;
import android.os.Bundle;
import java.util.HashMap;
import java.util.List;

public class SettingsActivity extends PreferenceActivity {
    
    private void overlayChanged(boolean enabled) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("overlay_enabled", enabled);
        editor.apply();
    }

    private void serverPortChanged(String server, String port) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("server", server);
        editor.putString("port", port);
        editor.apply();
    }

    private void borderChanged(String border) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("border", border);
        editor.apply();
    }

    private void autoBackgroundChanged(boolean enabled) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("autoBackground", enabled);
        editor.apply();
    }

    protected HashMap<String,String> getInstalledPackages(){
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
            String label = (String) packageManager.getApplicationLabel(activityInfo
                .applicationInfo);

            // Put the package name and application label to hash map
            map.put(packageName,label);
        }
        return map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        //installed-app-list
//        HashMap<String,String> map = getInstalledPackages();

        final SwitchPreference hovertext = (SwitchPreference) findPreference(this.getResources()
                                           .getString(R.string.hover_text)); 
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        hovertext.setChecked(pref.getBoolean("overlay_enabled", true));
        hovertext.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                overlayChanged((Boolean)newValue);
                return true;
            }
        });

        final EditTextPreference servertext = (EditTextPreference)findPreference("server"); 
        final EditTextPreference porttext = (EditTextPreference)findPreference("port"); 

        servertext.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                serverPortChanged((String)newValue, porttext.getText().toString());
                return true;
            }
        });

        porttext.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                serverPortChanged(servertext.getText().toString(), (String)newValue);
                return true;
            }
        });
        
        final EditTextPreference bordertext = (EditTextPreference)findPreference("border"); 

        bordertext.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                borderChanged((String) newValue);
                return true;
            }
        });

        final SwitchPreference autoBackground = 
            (SwitchPreference) findPreference("autoBackground"); 
        autoBackground.setChecked(pref.getBoolean("autoBackground", true));
        autoBackground
            .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                autoBackgroundChanged((Boolean) newValue);
                return true;
            }
        });

        //installed-app-list
/*        final PreferenceCategory cat = new PreferenceCategory(getApplicationContext());
        cat.setTitle(R.string.installedApps);
        for (final String appName :map.keySet()) {
            final Preference app = new Preference(getApplicationContext());
            app.setTitle(appName);
            app.setSummary(map.get(appName));
            cat.addPreference(app);
        }
*/

    }
}
