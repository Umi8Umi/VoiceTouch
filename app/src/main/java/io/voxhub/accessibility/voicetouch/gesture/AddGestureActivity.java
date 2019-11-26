package io.voxhub.accessibility.voicetouch.gesture;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.voxhub.accessibility.voicetouch.R;


public class AddGestureActivity extends Activity {

    private long delay = 0;
    private boolean inProgress = false;

    private ProgressBar mProgressBar;
    private FingerLine fl;
    private String gestureName;
    private static Bitmap backgroundPic;

    private boolean waitState = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        gestureName = null;


        //hide top bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        //hide bottom bar
        if(Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if(Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }


        setContentView(R.layout.activity_add_gesture);





        mProgressBar=(ProgressBar)findViewById(R.id.progressbar);
        mProgressBar.setProgress(0);


        //Check for touches on our main layout
        fl = (FingerLine) findViewById(R.id.finger_line);


        Bundle extras = getIntent().getExtras();
        String source = extras.getString("source");

        ImageView image = (ImageView) findViewById(R.id.background_pic);
        image.setImageBitmap(backgroundPic);

        //Start a thread that will keep count of the time
        final Thread t = new Thread("Listen for touch thread") {
            public void run() {
                while(true){
                    try{
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if(inProgress == false){
                        //Incement the delay
                        delay += 1000;
                        mProgressBar.setProgress((int)delay/1000*100/(3000/1000));
                        //If our delay in MS is over 10,000
                        if (delay > 2000) {
                            List<Point> list = fl.getPoints();
                            saveCurrentGesture(list, backgroundPic);
                            backgroundPic = null;
                            return;
                        }
                    }
                }
            }
        };




        if(source.equals("GestureSettingActivity")){
            String points = extras.getString("points");
            fl.points = PointsSerializer.convertStrToPointlist(points);
            gestureName = extras.getString("name");
            waitState = false;
            t.start();
            mProgressBar.setVisibility(View.VISIBLE);
        }else{
            mProgressBar.setVisibility(View.INVISIBLE);
        }


        fl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //If someone touches, reset our delay
                delay = 0;
                mProgressBar.setProgress(0);
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(waitState == true){
                        waitState = false;
                        t.start();
                    }
                    mProgressBar.setVisibility(View.INVISIBLE);
                    inProgress = true;
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    mProgressBar.setVisibility(View.VISIBLE);
                    inProgress = false;
                }

                return false;
            }
        });




    }


    private void saveCurrentGesture(List<Point> points, Bitmap backgroundPic){
        //pass the points back
        Intent intent = new Intent(this, GestureSettingActivity.class);
        intent.putExtra("source","AddGesture");
        GestureSettingActivity.setBackgroundImage(backgroundPic);
        intent.putExtra("points", PointsSerializer.convertPointlistToStr(points));
        if(gestureName != null){
            intent.putExtra("name", gestureName);
        }
        startActivity(intent);
    }






    public static void setBackgroundPic(Bitmap bm){
        backgroundPic = bm;
    }

}
