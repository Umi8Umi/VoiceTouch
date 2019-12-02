package io.voxhub.accessibility.voicetouch.gesture;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.voxhub.accessibility.voicetouch.R;
import io.voxhub.accessibility.voicetouch.database.GestureData;
import io.voxhub.accessibility.voicetouch.database.VoiceTouchDbHelper;

public class GestureSettingActivity extends Activity {


    private Dialog viewGestureDialog;
    private VoiceTouchDbHelper db;

    private String currentPoints;
    private String currentName;
    private static Bitmap currentBackground;

    private static final int GET_FROM_GALLERY = 1;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_setting);

        Display display = getWindowManager(). getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(size);
        }else{
            display.getSize(size);
        }
        int width = size.x;
        int height = size.y;


        //set the size of thumbnail picture
        View thumbnailView = (View) findViewById(R.id.gesture_thumbnail_canvas);
        thumbnailView.getLayoutParams().width = width/2;
        thumbnailView.getLayoutParams().height = height/2;


        db = new VoiceTouchDbHelper(this);

        Bundle extras = getIntent().getExtras();
        String source = extras.getString("source");

        if(source.equals("GestureList")){
            String name = extras.getString("gesture_name");

            //search db to set the image and points
            GestureData data = db.getGesture(name);
            currentName = name;
            currentPoints = data.getPoints();
            currentBackground = data.getBackground();

        }else if(source.equals("AddGesture")){
            currentPoints = extras.getString("points");
            if(extras.containsKey("name")){
                currentName = extras.getString("name");
            }else
                currentName = "default_name";
        }

        setBackGroundImg();
        EditText editText =  (EditText) findViewById(R.id.gesture_name);
        editText.setText(currentName);


        viewGestureDialog = new Dialog(this, DialogFragment.STYLE_NO_FRAME);
        viewGestureDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        viewGestureDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        viewGestureDialog.setContentView(getLayoutInflater().inflate(R.layout.gesture_display
                , null));

        //hide bottom bar for viewGestureDialog
        if(Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = viewGestureDialog.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if(Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = viewGestureDialog.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }


        FingerLine thumbnail = (FingerLine) findViewById(R.id.gesture_img);
        thumbnail.setHalfStrokeWidth();
        thumbnail.setHalfRadius();
        thumbnail.points = calcualteThumbnailPoints(PointsSerializer.convertStrToPointlist(currentPoints));
        thumbnail.setPaintingMode(false);
        thumbnail.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView image = (ImageView) viewGestureDialog.findViewById(R.id.background_pic_on_display);
                image.setImageBitmap(currentBackground);
                //draw on canvas
                FingerLine imageDisplay = (FingerLine) viewGestureDialog.findViewById(R.id.gesture_on_display);
                imageDisplay.points = PointsSerializer.convertStrToPointlist(currentPoints);

                imageDisplay.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getAction() == MotionEvent.ACTION_DOWN){
                            viewGestureDialog.dismiss();
                        }
                        return false;
                    }
                });



                viewGestureDialog.show();

                return false;
            }
        });



    }





    public void editGesture(View view){
        Intent intent = new Intent(this, AddGestureActivity.class);
        intent.putExtra("source","GestureSettingActivity");
        //pass the points
        intent.putExtra("points",currentPoints);
        EditText editText =  (EditText) findViewById(R.id.gesture_name);
        intent.putExtra("name", String.valueOf(editText.getText()) );
        //pass the background pic
        AddGestureActivity.setBackgroundPic(currentBackground);
        currentBackground = null;
        startActivity(intent);
    }



    public void saveGesture(View view){
        EditText editText =  (EditText) findViewById(R.id.gesture_name);
        currentName = String.valueOf(editText.getText());


        //check if the name exists first
        boolean isExist = db.isGestureExist(currentName);


        if(isExist){
            //if it exists, show alertDialog, let user change the gesture name
            new AlertDialog.Builder(this)
                    .setTitle("This name is already taken")
                    .setMessage("Do you want to overwrite this existing gesture?")
                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setPositiveButton("Yes",  new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            //if it does not exist, save the gesture to database and redirect user to the gesture list page
                            GestureData newGesture = new GestureData(currentName, currentPoints, currentBackground);
                            db.updateGesture(newGesture, currentName);
                            jumpToListPage();
                        }
                    })
                    .setNegativeButton("No", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }else{
            //if it does not exist, save the gesture to database and redirect user to the gesture list page
            GestureData newGesture = new GestureData(currentName, currentPoints, currentBackground);
            db.addGesture(newGesture);
            jumpToListPage();

        }

    }

    public void jumpToListPage(){
        //clear picture memory
        currentBackground = null;
        //redirect to gesture list page
        Intent intent = new Intent(this, GestureListActivity.class);
        startActivity(intent);
    }

    public void uploadBackgroundImg(View view){
        //startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Log.i("gallery","start");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),GET_FROM_GALLERY);

    }

    public void stopViewGesture(View view){
        viewGestureDialog.dismiss();
    }

    public Bitmap getResizedBitmap(Bitmap image) {
        int maxSize = 500;
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();

            try {
                //need to limit the file size to 100KB

                Bitmap bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                currentBackground = getResizedBitmap(bm);
                setBackGroundImg();

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void setBackGroundImg(){
        ImageView image = (ImageView) findViewById(R.id.thumbnail_background_pic);
        image.setImageBitmap(currentBackground);
    }

    public void clearBackGroundImg(View view){
        currentBackground = null;
        setBackGroundImg();
    }




    public List<Point> calcualteThumbnailPoints(List<Point> list){
        List<Point> result = new ArrayList<Point>();
        for(Point p: list){
            float x = p.x/2;
            float y = p.y/2;
            result.add(new Point(x,y));
        }
        return result;
    }




    public static void setBackgroundImage(Bitmap bm){
        currentBackground = bm;
    }


}
