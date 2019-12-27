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
public class VoiceTouchDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "GestureReader.db";


    public static final String TABLE_GESTURE = "Gesture";
    public static final String TABLE_COMMAND = "Command";
    

    //column names for table gesture
    //gesture name
    public static final String TABLE_GESTURE_COLUMN_NAME_GESTURE_NAME ="name";
    //json array of gesture points
    public static final String TABLE_GESTURE_COLUMN_NAME_GESTURE_POINTS = "points";
    //bitmap data of the background
    public static final String TABLE_GESTURE_COLUMN_NAME_GESTURE_BACKGROUND = "background";


    //column names for table command
    //command name
    public static final String TABLE_COMMAND_COLUMN_NAME_COMMAND_NAME ="name";
    //command call name
    public static final String TABLE_COMMAND_COLUMN_NAME_COMMAND_CALL_NAME = "callname";
    //gestures this command contains
    public static final String TABLE_COMMAND_COLUMN_NAME_GESTURES_FOR_COMMAND = "gestures";

    private static final String TABLE_GESTURE_SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_GESTURE + " (" +
                    TABLE_GESTURE_COLUMN_NAME_GESTURE_NAME + " TEXT PRIMARY KEY," +
                    TABLE_GESTURE_COLUMN_NAME_GESTURE_POINTS + " TEXT," +
                    TABLE_GESTURE_COLUMN_NAME_GESTURE_BACKGROUND + " BLOB" + ")";

    private static final String TABLE_COMMAND_SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_COMMAND + " (" +
                    TABLE_COMMAND_COLUMN_NAME_COMMAND_NAME + " TEXT PRIMARY KEY," +
                    TABLE_COMMAND_COLUMN_NAME_COMMAND_CALL_NAME + " TEXT," +
                    TABLE_COMMAND_COLUMN_NAME_GESTURES_FOR_COMMAND + " TEXT" + ")";



    public VoiceTouchDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating table
        db.execSQL(TABLE_GESTURE_SQL_CREATE_ENTRIES);
        db.execSQL(TABLE_COMMAND_SQL_CREATE_ENTRIES);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GESTURE );
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMAND );

        // create new table
        onCreate(db);
    }


    public void addGesture(GestureData data) throws SQLiteException {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TABLE_GESTURE_COLUMN_NAME_GESTURE_NAME,   data.getName());
        if(data.getPoints() == null){
            cv.put(TABLE_GESTURE_COLUMN_NAME_GESTURE_POINTS,   "NULL");
        }else
            cv.put(TABLE_GESTURE_COLUMN_NAME_GESTURE_POINTS,   data.getPoints());


        if(data.getBackground() == null){
            cv.put(TABLE_GESTURE_COLUMN_NAME_GESTURE_BACKGROUND, "NULL");
            Log.i("db", "background is null");
        }else{
            cv.put(TABLE_GESTURE_COLUMN_NAME_GESTURE_BACKGROUND, DbUtility.getBytes(data.getBackground()));
            Log.i("db", "deal with the gesture background");
        }


        database.insert(TABLE_GESTURE, null, cv );

    }


    public void updateGesture(GestureData data, String name) throws SQLiteException {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(TABLE_GESTURE_COLUMN_NAME_GESTURE_NAME,   data.getName());
        if(data.getPoints() == null){
            cv.put(TABLE_GESTURE_COLUMN_NAME_GESTURE_POINTS,   "NULL");
        }else
            cv.put(TABLE_GESTURE_COLUMN_NAME_GESTURE_POINTS,   data.getPoints());


        if(data.getBackground() == null){
            cv.put(TABLE_GESTURE_COLUMN_NAME_GESTURE_BACKGROUND, "NULL");
            Log.i("db", "background is null");
        }else{
            cv.put(TABLE_GESTURE_COLUMN_NAME_GESTURE_BACKGROUND, DbUtility.getBytes(data.getBackground()));
            Log.i("db", "deal with the gesture background");
        }


        database.update(TABLE_GESTURE, cv,"name = '" + name+"'", null);
    }


    public GestureData getGesture(String name) throws SQLiteException {
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(TABLE_GESTURE, new String[] {TABLE_GESTURE_COLUMN_NAME_GESTURE_POINTS, TABLE_GESTURE_COLUMN_NAME_GESTURE_BACKGROUND},
                "name = '" + name+"'", null, null, null, null);
        cursor.moveToFirst();
        String points = cursor.getString(0);
        byte[] background_images = cursor.getBlob(1);
        GestureData res = new GestureData(name, points, DbUtility.getImage(background_images));
        cursor.close();
        return res;
    }


    public String getGesturePoints(String name) throws SQLiteException {
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(TABLE_GESTURE, new String[] {TABLE_GESTURE_COLUMN_NAME_GESTURE_POINTS},
                "name = '" + name+"'", null, null, null, null);
        cursor.moveToFirst();
        String points = cursor.getString(0);
        cursor.close();
        return points;
    }

    public int deleteGesture(String name) throws SQLiteException {
        SQLiteDatabase database = this.getWritableDatabase();
        Log.i("db","gesture with name "+name+" is deleted");
        int lines = database.delete(TABLE_GESTURE, "name = '" + name+"'", null);

        return lines;
    }


    public List<String> getAllGestureNames() throws SQLiteException {
        SQLiteDatabase database = this.getReadableDatabase();
        List<String> result = new ArrayList<String>();

        Cursor cursor = database.query(TABLE_GESTURE, new String[] {TABLE_GESTURE_COLUMN_NAME_GESTURE_NAME},
                null, null, null, null, null);

        if (cursor.moveToFirst()){
            do{
                String data = cursor.getString(cursor.getColumnIndex(TABLE_GESTURE_COLUMN_NAME_GESTURE_NAME));
                result.add(data);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public boolean isGestureExist(String name) throws SQLiteException {
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(TABLE_GESTURE, new String[] {TABLE_GESTURE_COLUMN_NAME_GESTURE_POINTS},
                "name = '" + name+"'", null, null, null, null);
        int count = cursor.getCount();
        cursor.close();

        if(count == 0)
            return false;
        return true;
    }



    public int deleteCommand(String name) throws SQLiteException {
        SQLiteDatabase database = this.getWritableDatabase();
        Log.i("db","command with name "+name+" is deleted");
        int lines = database.delete(TABLE_COMMAND, "name = '" + name+"'", null);
        return lines;
    }

    public CommandData getCommand(String name) throws SQLiteException {

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(TABLE_COMMAND, new String[] {TABLE_COMMAND_COLUMN_NAME_COMMAND_CALL_NAME, TABLE_COMMAND_COLUMN_NAME_GESTURES_FOR_COMMAND},
                "name = '" + name+"'", null, null, null, null);
        cursor.moveToFirst();
        String callname = cursor.getString(0);
        String gestures = cursor.getString(1);
        String[] gestureArray = gestures.split(",");

        CommandData res = new CommandData(name, callname, gestureArray);
        cursor.close();
        return res;
    }

    public boolean isCommandExist(String name)  throws SQLiteException {
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(TABLE_COMMAND, new String[] {TABLE_COMMAND_COLUMN_NAME_COMMAND_CALL_NAME},
                "name = '" + name+"'", null, null, null, null);
        int count = cursor.getCount();
        cursor.close();

        if(count == 0)
            return false;
        return true;
    }


    public void updateCommand(CommandData data, String name) throws SQLiteException {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TABLE_COMMAND_COLUMN_NAME_COMMAND_NAME,   data.getName());
        cv.put(TABLE_COMMAND_COLUMN_NAME_COMMAND_CALL_NAME , data.getAlias());
        cv.put(TABLE_COMMAND_COLUMN_NAME_GESTURES_FOR_COMMAND, DbUtility.getSingleStr(data.getGestureArray()));
        database.update(TABLE_COMMAND, cv,"name = '" + name+"'", null);
    }

    public void createCommand(CommandData data, String name) throws SQLiteException {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TABLE_COMMAND_COLUMN_NAME_COMMAND_NAME,   data.getName());
        cv.put(TABLE_COMMAND_COLUMN_NAME_COMMAND_CALL_NAME , data.getAlias());
        cv.put(TABLE_COMMAND_COLUMN_NAME_GESTURES_FOR_COMMAND, DbUtility.getSingleStr(data.getGestureArray()));
        database.insert(TABLE_COMMAND, null, cv );
    }


    public List<String> getAllCommandNames() throws SQLiteException {
        SQLiteDatabase database = this.getReadableDatabase();
        List<String> result = new ArrayList<String>();

        Cursor cursor = database.query(TABLE_COMMAND, new String[] {TABLE_COMMAND_COLUMN_NAME_COMMAND_NAME},
                null, null, null, null, null);

        if (cursor.moveToFirst()){
            do{
                String data = cursor.getString(cursor.getColumnIndex(TABLE_COMMAND_COLUMN_NAME_COMMAND_NAME));
                result.add(data);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return result;
    }
}
