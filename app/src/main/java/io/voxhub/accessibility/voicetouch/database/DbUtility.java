package io.voxhub.accessibility.voicetouch.database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.commons.codec.binary.StringUtils;

import java.io.ByteArrayOutputStream;

public class DbUtility {
    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }


    //convert array of string to string
    //spliter: ,
    public static String getSingleStr(String[] array){
        String res = "";
        if(array.length == 0)
            return res;

        int len = array.length;
        for(int i=1;i<=len-1;i++){
            res = res + array[i-1] + ",";
        }
        res = res + array[array.length - 1];
        return res;

    }

    //convert string to list of string
    //spliter: ,
    public String[] getStrArray(String str){
        return str.split(",");
    }


}
