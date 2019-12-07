package io.voxhub.accessibility.voicetouch.command;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import io.voxhub.accessibility.voicetouch.R;
import io.voxhub.accessibility.voicetouch.SimpleActivity;
import io.voxhub.accessibility.voicetouch.database.VoiceTouchDbHelper;
import io.voxhub.accessibility.voicetouch.gesture.GestureAdapter;
import io.voxhub.accessibility.voicetouch.gesture.GestureListActivity;
import io.voxhub.accessibility.voicetouch.gesture.GestureSettingActivity;

public class CommandListActivity extends Activity {

    private VoiceTouchDbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command_list);

        db = new VoiceTouchDbHelper(this);
        //show all commands

        ListView listView = (ListView) findViewById(R.id.command_list);


        final List<String> values = db.getAllCommandNames();

        CommandAdapter adapter = new CommandAdapter(values, this);

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id){
                String commandName = values.get(position);

                Intent intent = new Intent(CommandListActivity.this, CommandSettingActivity.class);
                //based on item add info to intent
                intent.putExtra("source","CommandList");
                intent.putExtra("command_name", commandName);
                startActivity(intent);

            }
        });

    }


    public void jumpToAddCommand(View view){
        Intent intent = new Intent(this, CommandSettingActivity.class);
        intent.putExtra("source","null");
        //pass the command name
        startActivity(intent);
    }

}
