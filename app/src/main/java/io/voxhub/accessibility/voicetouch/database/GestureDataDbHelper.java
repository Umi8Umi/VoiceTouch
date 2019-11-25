package io.voxhub.accessibility.voicetouch.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


//We need one table now(id: gesture_name)
//column: gesture_name, points, picture_name
//we use android built-in sqlite db service to do that
public class GestureDataDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "GestureReader.db";


    public static final String TABLE_NAME = "Gesture";

    //gesture name
    public static final String COLUMN_NAME_GESTURE_NAME ="name";

    //json array of gesture points
    public static final String COLUMN_NAME_GESTURE_POINTS = "points";



    //bitmap data of the background
    public static final String COLUMN_NAME_GESTURE_BACKGROUND = "background";


    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_GESTURE_NAME + " TEXT PRIMARY KEY," +
                    COLUMN_NAME_GESTURE_POINTS + " TEXT," +
                    COLUMN_NAME_GESTURE_BACKGROUND + " BLOB" + ")";


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;


    public GestureDataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating table
        db.execSQL(SQL_CREATE_ENTRIES);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME );

        // create new table
        onCreate(db);
    }


    public void addGesture(GestureData data) throws SQLiteException {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_GESTURE_NAME,   data.getName());
        if(data.getPoints() == null){
            cv.put(COLUMN_NAME_GESTURE_POINTS,   "NULL");
        }else
            cv.put(COLUMN_NAME_GESTURE_POINTS,   data.getPoints());


        if(data.getBackground() == null){
            cv.put(COLUMN_NAME_GESTURE_BACKGROUND, "NULL");
            Log.i("db", "background is null");
        }else{
            cv.put(COLUMN_NAME_GESTURE_BACKGROUND, DbBitmapUtility.getBytes(data.getBackground()));
            Log.i("db", "deal with the gesture background");
        }


        database.insert(TABLE_NAME, null, cv );

    }


    public void updateGesture(GestureData data, String name) throws SQLiteException {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_GESTURE_NAME,   data.getName());
        if(data.getPoints() == null){
            cv.put(COLUMN_NAME_GESTURE_POINTS,   "NULL");
        }else
            cv.put(COLUMN_NAME_GESTURE_POINTS,   data.getPoints());


        if(data.getBackground() == null){
            cv.put(COLUMN_NAME_GESTURE_BACKGROUND, "NULL");
            Log.i("db", "background is null");
        }else{
            cv.put(COLUMN_NAME_GESTURE_BACKGROUND, DbBitmapUtility.getBytes(data.getBackground()));
            Log.i("db", "deal with the gesture background");
        }


        database.update(TABLE_NAME, cv,"name = '" + name+"'", null);
    }


    public GestureData getGesture(String name) throws SQLiteException {
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(TABLE_NAME, new String[] {COLUMN_NAME_GESTURE_POINTS, COLUMN_NAME_GESTURE_BACKGROUND},
                "name = '" + name+"'", null, null, null, null);
        cursor.moveToFirst();
        String points = cursor.getString(0);
        byte[] background_images = cursor.getBlob(1);
        GestureData res = new GestureData(name, points, DbBitmapUtility.getImage(background_images));
        cursor.close();
        return res;
    }

    public int deleteGesture(String name) throws SQLiteException {
        SQLiteDatabase database = this.getWritableDatabase();
        Log.i("db","gesture with name "+name+" is deleted");
        int lines = database.delete(TABLE_NAME, "name = '" + name+"'", null);

        return lines;
    }


    public List<String> getAllGestureNames() throws SQLiteException {
        SQLiteDatabase database = this.getReadableDatabase();
        List<String> result = new ArrayList<String>();

        Cursor cursor = database.query(TABLE_NAME, new String[] {COLUMN_NAME_GESTURE_NAME},
                null, null, null, null, null);

        if (cursor.moveToFirst()){
            do{
                String data = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_GESTURE_NAME));
                result.add(data);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public boolean isExist(String name) throws SQLiteException {
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(TABLE_NAME, new String[] {COLUMN_NAME_GESTURE_POINTS},
                "name = '" + name+"'", null, null, null, null);
        int count = cursor.getCount();
        cursor.close();

        if(count == 0)
            return false;
        return true;
    }

}
