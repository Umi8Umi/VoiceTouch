package io.voxhub.accessibility.voicetouch;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.view.View;
import android.content.Intent;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.TextView;

public class HelpActivity extends Activity {

    private Button btn_back;
    private TextView title;
    private TextView faq;
    private TextView body;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_dictation);
        Intent intent = getIntent();
        btn_back = (Button) findViewById(R.id.btn_back);
        title = (TextView) findViewById(R.id.help_title);
        faq = (TextView) findViewById(R.id.faq);
        body = (TextView) findViewById(R.id.faq_body);
        
        body.setText(Html.fromHtml(
            "<b>\"What commands can I use?\"</b><br>Navigate to the app settings --> commands to see a full list of supported commands.<br><br>"), 
            TextView.BufferType.SPANNABLE);
        
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // Open main page
               finish();
            }
        });
    };
}
