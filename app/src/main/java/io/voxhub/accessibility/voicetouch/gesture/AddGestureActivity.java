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
        fl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //If someone touches, reset our delay
                delay = 0;
                mProgressBar.setProgress(0);
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    mProgressBar.setVisibility(View.INVISIBLE);
                    inProgress = true;
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    mProgressBar.setVisibility(View.VISIBLE);
                    inProgress = false;
                }

                return false;
            }
        });

        Bundle extras = getIntent().getExtras();
        String source = extras.getString("source");

        if(source.equals("GestureSettingActivity")){
            String points = extras.getString("points");
            fl.points = convertStrToPointlist(points);
            gestureName = extras.getString("name");
        }

        if(backgroundPic != null){
            ImageView image = (ImageView) findViewById(R.id.background_pic);
            image.setImageBitmap(backgroundPic);
        }


        //Start a thread that will keep count of the time
        new Thread("Listen for touch thread") {
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
                                Bitmap screenShot = getScreenShot();
                                List<Point> list = fl.getPoints();
                                saveCurrentGesture(list, screenShot, backgroundPic);
                                backgroundPic = null;
                                return;
                            }
                        }
                    }
                }
            }.start();


    }


    private void saveCurrentGesture(List<Point> points, Bitmap screenshot, Bitmap backgroundPic){
        //pass the points back
        Intent intent = new Intent(this, GestureSettingActivity.class);
        intent.putExtra("source","AddGesture");
        GestureSettingActivity.setScreenShotImage(screenshot);
        GestureSettingActivity.setBackgroundImage(backgroundPic);
        intent.putExtra("points", convertPointlistToStr(points));
        if(gestureName != null){
            intent.putExtra("name", gestureName);
        }
        startActivity(intent);
    }


    private List<Point> convertStrToPointlist(String str){
        //convert json array string to list of points
        List<Point> list = new ArrayList<Point>();
        try {
            JSONArray jsonArray = new JSONArray(str);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject explrObject = jsonArray.getJSONObject(i);
                list.add(new Point(Float.valueOf(String.valueOf(explrObject.get("x"))), Float.valueOf(String.valueOf(explrObject.get("y")))) );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("json", String.valueOf(list.size()) );

        return list;
    }


    private String convertPointlistToStr(List<Point> points)  {

        //convert list of points to json array string
        JSONArray jsonArray = new JSONArray();
        try {
            for (Point p : points) {
                JSONObject pointJson = new JSONObject();
                pointJson.put("x", String.valueOf(p.x));
                pointJson.put("y", String.valueOf(p.y));
                jsonArray.put(pointJson);
            }

        }catch(JSONException e){
            Log.d("json", "fail to convert point list to str");
            return null;
        }
        return jsonArray.toString();

    }


    private Bitmap getScreenShot(View view) {
        mProgressBar.setVisibility(View.INVISIBLE);
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }



    private Bitmap getScreenShot(){
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        Bitmap bm = getScreenShot(rootView);
        return bm;
    }


    public static void setBackgroundPic(Bitmap bm){
        backgroundPic = bm;
    }

}
