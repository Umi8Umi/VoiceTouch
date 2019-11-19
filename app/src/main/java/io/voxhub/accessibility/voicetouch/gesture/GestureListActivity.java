package io.voxhub.accessibility.voicetouch.gesture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;



import java.util.List;

import io.voxhub.accessibility.voicetouch.R;
import io.voxhub.accessibility.voicetouch.database.GestureDataDbHelper;


public class GestureListActivity extends Activity {

    private GestureDataDbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_list);

        db = new GestureDataDbHelper(this);

        ListView listView = (ListView) findViewById(R.id.list);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id){
                Object item = adapter.getItemAtPosition(position);

                Intent intent = new Intent(GestureListActivity.this, GestureSettingActivity.class);
                //based on item add info to intent
                startActivity(intent);
            }
        });

        final List<String> values = db.getAllGestureNames();

        MyCustomAdapter adapter = new MyCustomAdapter(values, this);

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id){
                String gestureName = values.get(position);

                Intent intent = new Intent(GestureListActivity.this, GestureSettingActivity.class);
                //based on item add info to intent
                intent.putExtra("source","GestureList");
                intent.putExtra("gesture_name", gestureName);
                startActivity(intent);

            }
        });

        //initialize other static parameters
        GestureSettingActivity.setScreenShotImage(null);

    }


    public void drawGesture(View view) {
        // Do something in response to button
        AddGestureActivity.setBackgroundPic(null);
        Intent intent = new Intent(this, AddGestureActivity.class);
        intent.putExtra("source","GestureList");
        startActivity(intent);
    }




}
