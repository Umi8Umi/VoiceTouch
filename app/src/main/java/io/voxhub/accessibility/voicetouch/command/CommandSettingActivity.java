package io.voxhub.accessibility.voicetouch.command;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import io.voxhub.accessibility.voicetouch.Constants;
import io.voxhub.accessibility.voicetouch.R;
import io.voxhub.accessibility.voicetouch.database.CommandData;
import io.voxhub.accessibility.voicetouch.database.GestureData;
import io.voxhub.accessibility.voicetouch.database.VoiceTouchDbHelper;

public class CommandSettingActivity extends Activity {
    private List<String> allGestures;
    private VoiceTouchDbHelper db;
    private List<String> gestureList;
    private GestureAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command_setting);

        db = new VoiceTouchDbHelper(this);
        allGestures = db.getAllGestureNames();
        gestureList = new ArrayList<>();

        //if displaying some exsiting command, just show it

        Bundle extras = getIntent().getExtras();


        if(extras != null && extras.containsKey(Constants.SOURCE_STR) && !extras.getString(Constants.SOURCE_STR).equals("null")){
            String name = extras.getString("command_name");
            CommandData data = db.getCommand(name);
            EditText editText =  (EditText) findViewById(R.id.command_name);
            editText.setText(name);
            editText = (EditText) findViewById(R.id.command_callname);
            editText.setText(data.getAlias());

            String[] gestureArray = data.getGestureArray();
            for(int i=0;i<gestureArray.length;i++){
                gestureList.add(gestureArray[i]);
            }

        }



        ListView listView = (ListView) findViewById(R.id.gesture_list_for_command);
        adapter = new GestureAdapter(gestureList, this);
        listView.setAdapter(adapter);




    }


    public void showAllGestures(View view){
        //show gesture dialog for user to choose
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(CommandSettingActivity.this);
        builderSingle.setTitle("Select One Gesture:");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(CommandSettingActivity.this, android.R.layout.select_dialog_singlechoice);
        for(String gesture: allGestures){
            arrayAdapter.add(gesture);
        }


        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String gestureName = arrayAdapter.getItem(which);
                addOneGesture(gestureName);
                dialog.dismiss();
            }
        });
        builderSingle.show();


    }


    public void addOneGesture(String gestureName){
        gestureList.add(gestureName);
        adapter.notifyDataSetChanged();
    }


    public void createCommand(View view){
        EditText editText =  (EditText) findViewById(R.id.command_name);
        final String name = String.valueOf(editText.getText());
        EditText editText2 =  (EditText) findViewById(R.id.command_callname);
        final String callname = String.valueOf(editText2.getText());

        final String[] gestureArray = new String[gestureList.size()];
        for(int i=0;i<gestureList.size();i++){
            gestureArray[i] = gestureList.get(i);
        }

        if(db.isCommandExist(name)){
            //update
            new AlertDialog.Builder(this)
                    .setTitle("This command name is already taken")
                    .setMessage("Do you want to overwrite this existing command?")
                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setPositiveButton("Yes",  new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            //if it does not exist, save the gesture to database and redirect user to the gesture list page
                            CommandData newCommand = new CommandData(name, callname, gestureArray);
                            db.updateCommand(newCommand, name);
                            jumpToListPage();
                        }
                    })
                    .setNegativeButton("No", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }else{
            //create command
            CommandData newCommand = new CommandData(name, callname, gestureArray);
            db.createCommand(newCommand, name);
            jumpToListPage();
        }


    }



    public void deleteCommand(View view){
        EditText editText =  (EditText) findViewById(R.id.command_name);
        String name = String.valueOf(editText.getText());
        if(db.isCommandExist(name)){
            db.deleteCommand(name);
        }
        jumpToListPage();
    }


    public void jumpToListPage(){
        Intent intent = new Intent(this, CommandListActivity.class);
        startActivity(intent);
    }

}
