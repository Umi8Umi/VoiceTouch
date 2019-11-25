package io.voxhub.accessibility.voicetouch.gesture;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PointsSerializer {

    public static List<Point> convertStrToPointlist(String str){
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


    public static String convertPointlistToStr(List<Point> points)  {

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

}
