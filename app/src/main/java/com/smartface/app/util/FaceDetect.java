package com.smartface.app.util;

import android.graphics.Bitmap;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class FaceDetect {
    public static final String KEY = "64a6cca7ff1f8b8c3e0bdf4c91358aee";
    public static final String SECRET = "gYwrVJacKnQ24GuGq7UIlV9GJDEQf01H";

    public interface CallBack{
        void success(JSONObject result);
        void error(FaceppParseException e);
    }

    public static void detect(final Bitmap bitmap, final CallBack callBack){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpRequests httpRequests = new HttpRequests(KEY, SECRET, true, true);
                    Bitmap btm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    btm.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] bytes = byteArrayOutputStream.toByteArray();
                    PostParameters postParameters = new PostParameters();
                    postParameters.setImg(bytes);
                    JSONObject jsonObject = httpRequests.detectionDetect(postParameters);
                    Log.e("TAG", jsonObject.toString());
                    if(callBack != null){
                        callBack.success(jsonObject);
                    }
                } catch (FaceppParseException e) {
                    e.printStackTrace();
                    if(callBack != null){
                        callBack.error(e);
                    }
                }
            }
        }).start();
    }
}
