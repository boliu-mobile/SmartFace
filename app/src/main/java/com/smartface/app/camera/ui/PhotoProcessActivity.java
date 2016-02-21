package com.smartface.app.camera.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.common.util.FileUtils;
import com.common.util.ImageUtils;
import com.common.util.NetWorkUtils;
import com.common.util.StringUtils;
import com.common.util.TimeUtils;
import com.facepp.error.FaceppParseException;
import com.github.boliu.smartface.R;
import com.smartface.app.camera.CameraBaseActivity;
import com.smartface.app.camera.CameraManager;
import com.smartface.app.model.FeedItem;
import com.smartface.app.model.TagItem;
import com.smartface.app.util.FaceDetect;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

public class PhotoProcessActivity extends CameraBaseActivity {

    //滤镜图片
    @InjectView(R.id.mphoto)
    ImageView mPhoto;
    //绘图区域
    @InjectView(R.id.drawing_view_container)
    ViewGroup drawArea;
    //底部按钮
    @InjectView(R.id.process_btn)
    TextView processBtn;
    @InjectView(R.id.recommend_btn)
    TextView recommendBtn;

    //当前选择底部按钮
    private TextView currentBtn;
    //当前图片
    private Bitmap currentBitmap;

    List<Recommender> listRecommender;

    //标签区域
    private View commonLabelArea;

    private Paint mPaint;

    class Recommender {
        String json;
        String stuff = "default";

        public Recommender(JSONObject input) {
            this.json = input.toString();
            String gender = "";
            String age = "";
            String race= "";
            try {
                gender = input.getJSONObject("gender").getString("value");
                age = input.getJSONObject("age").getString("value");
                race = input.getJSONObject("race").getString("value");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            stuff = gender.equalsIgnoreCase("male") ? "razor" : "mascara";
            stuff = stuff + "_" + race + "_" + age;
        }

        @Override
        public String toString() {
            return json.toString() + "\n\n" + "We recommend " + stuff + " for you~";
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_process);
        ButterKnife.inject(this);
        initView();
        initEvent();
        mPaint = new Paint();
        listRecommender = new ArrayList<Recommender>();
        //frameLayout.setVisibility(View.VISIBLE);

        ImageUtils.asyncLoadImage(this, getIntent().getData(), new ImageUtils.LoadImageCallback() {
            @Override
            public void callback(Bitmap result) {
                currentBitmap = result;
                mPhoto.setImageBitmap(currentBitmap);
            }
        });

    }
    private void initView() {
    }

    private static final int MSG_SUCCESS = 1;
    private static final int MSG_ERROR=0;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg == null || msg.obj == null)
                return;
            switch (msg.what){
                case MSG_SUCCESS:
                    JSONObject jso = (JSONObject) msg.obj;
                    prePareBitmap(jso);
                    mPhoto.setImageBitmap(currentBitmap);
                    break;
                case MSG_ERROR:
                    toast(msg.obj.toString(), Toast.LENGTH_SHORT);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void prePareBitmap(JSONObject jso) {
        int num_male = 0;
        int num_female = 0;

        Bitmap bitmap = Bitmap.createBitmap(currentBitmap.getWidth(),
                currentBitmap.getHeight(),currentBitmap.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(currentBitmap,0,0,null);
        try {
            JSONArray jsonArray = null;
            try {
                jsonArray = jso.getJSONArray("face");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            int faceCount = jsonArray.length();

            for(int i = 0; i < faceCount; i++){
                JSONObject face = jsonArray.getJSONObject(i);
                JSONObject position = face.getJSONObject("position");
                float x = (float) position.getJSONObject("center").getDouble("x");
                float y = (float) position.getJSONObject("center").getDouble("y");
                float w = (float) position.getDouble("width");
                float h = (float) position.getDouble("height");
                x = x / 100 * bitmap.getWidth();
                y = y / 100 * bitmap.getHeight();
                w = w / 100 * bitmap.getWidth();
                h = h / 100 * bitmap.getHeight();
                mPaint.setColor(0xffffffff);
                mPaint.setStrokeWidth(2);
                canvas.drawLine(x - w/2,y - h/2,x - w/2,y + h/2,mPaint);
                canvas.drawLine(x - w/2,y - h/2,x + w/2,y - h/2,mPaint);
                canvas.drawLine(x + w/2,y - h/2,x + w/2,y + h/2,mPaint);
                canvas.drawLine(x - w / 2, y + h / 2, x + w / 2, y + h / 2, mPaint);
                listRecommender.add(new Recommender(face.getJSONObject("attribute")));
                int age = face.getJSONObject("attribute").getJSONObject("age").getInt("value");
                String gender = face.getJSONObject("attribute").getJSONObject("gender").getString("value");

                if(gender.equalsIgnoreCase("male")) {
                    num_male ++;
                } else {
                    num_female ++;
                }

                Bitmap ageBitmap = buildAgeBitmap(age, "Male".equals(gender));
                int ageWidth = ageBitmap.getWidth();
                int ageHeight = ageBitmap.getHeight();
                Log.e("agewidth","age" + ageWidth + "w=" + w + "h=" + h);
                Log.e("ageheight","age"+ageHeight);
                if((bitmap.getWidth() < mPhoto.getWidth()) && (bitmap.getHeight() < mPhoto.getHeight())){
                    float ratio = Math.max(bitmap.getWidth() * 1.0f / mPhoto.getWidth(),
                            bitmap.getHeight() * 1.0f / mPhoto.getHeight());
                    Log.e("Ratio","ratio="+ratio);
                    Log.e("Ratio","ratio="+(int)(ageWidth * ratio));
                    ageBitmap = Bitmap.createScaledBitmap(ageBitmap,
                            (int)(ageWidth * ratio), (int)(ageHeight * ratio), true);
                }
                Log.e("agewidth","age"+ageBitmap.getWidth());
                Log.e("ageheight","age"+ageBitmap.getHeight());

                canvas.drawBitmap(ageBitmap, x - ageBitmap.getWidth() / 2, y - h / 2 - ageBitmap.getHeight(), null);

                currentBitmap = bitmap;
            }

            if (faceCount == 0) {
                toast("Sorry, no face detected...", Toast.LENGTH_LONG);
            } else {
                toast("Detected " + (num_male == 0 ? "" : num_male + " male ")
                                + (num_female == 0 ? "" : num_female + " female"), Toast.LENGTH_LONG);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private Bitmap buildAgeBitmap(int age, boolean isMale) {
        ImageView imageView = new ImageView(this);

        if(isMale){
            //tv.setCompoundDrawablesWithIntrinsicBounds(
            //       getResources().getDrawable(R.drawable.male),null,null,null);
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.male));
        }else{
            //tv.setCompoundDrawablesWithIntrinsicBounds(
            //       getResources().getDrawable(R.drawable.female),null,null,null);
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.female));
        }
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        return  drawable.getBitmap();
    }

    private void initEvent() {

        processBtn.setOnClickListener(v -> {

            if (!NetWorkUtils.isConnected(this)) {
              toast("Please connect network to continue", Toast.LENGTH_SHORT);
                return;
            }

            new FaceCheckTask(new FaceDetect.CallBack() {
                @Override
                public void success(JSONObject result) {
                    Message msg = Message.obtain();
                    msg.what = MSG_SUCCESS;
                    msg.obj = result;
                    handler.sendMessage(msg);
                    dismissProgressDialog();
                }

                @Override
                public void error(FaceppParseException e) {
                    Message msg = Message.obtain();
                    msg.what = MSG_ERROR;
                    msg.obj = e.getErrorMessage();
                    handler.sendMessage(msg);
                    dismissProgressDialog();
                }
            }).execute(currentBitmap);
        });

        recommendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast(listRecommender.toString(), Toast.LENGTH_LONG);
            }
        });

        titleBar.setRightBtnOnclickListener(v -> {
            savePicture();
        });
    }

    private class FaceCheckTask extends AsyncTask<Bitmap, Void, Void> {

        private FaceDetect.CallBack callback;

        public FaceCheckTask(FaceDetect.CallBack callback) {
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog("Detecting...");
        }

        @Override
        protected Void doInBackground(Bitmap... params) {
            FaceDetect.detect(params[0], callback);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    //保存图片
    private void savePicture(){
        //加滤镜
//        final Bitmap newBitmap = Bitmap.createBitmap(mImageView.getWidth(), mImageView.getHeight(),
//                Bitmap.Config.ARGB_8888);
//        Canvas cv = new Canvas(newBitmap);
//        RectF dst = new RectF(0, 0, mImageView.getWidth(), mImageView.getHeight());
//        try {
//            cv.drawBitmap(mPhoto.capture(), null, dst, null);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            cv.drawBitmap(currentBitmap, null, dst, null);
//        }
//        //加贴纸水印
//        EffectUtil.applyOnSave(cv, mImageView);
//
        new SavePicToFileTask().execute(currentBitmap);
    }

    private class SavePicToFileTask extends AsyncTask<Bitmap,Void,String>{
        Bitmap bitmap;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog("image processing...");
        }

        @Override
        protected String doInBackground(Bitmap... params) {
            String fileName = null;
            try {
                bitmap = params[0];

                String picName = TimeUtils.dtFormat(new Date(), "yyyyMMddHHmmss");
                 fileName = ImageUtils.saveToFile(FileUtils.getInst().getPhotoSavedPath() + "/"+ picName, false, bitmap);

            } catch (Exception e) {
                e.printStackTrace();
                toast("图片处理错误，请退出相机并重试", Toast.LENGTH_LONG);
            }
            return fileName;
        }

        @Override
        protected void onPostExecute(String fileName) {
            super.onPostExecute(fileName);
            dismissProgressDialog();
            if (StringUtils.isEmpty(fileName)) {
                return;
            }


            //将照片信息保存至sharedPreference
            //保存标签信息
            List<TagItem> tagInfoList = new ArrayList<TagItem>();
            tagInfoList.add(new TagItem());


            //将图片信息通过EventBus发送到MainActivity
            FeedItem feedItem = new FeedItem(tagInfoList,fileName);
            EventBus.getDefault().post(feedItem);
            CameraManager.getInst().close();
        }
    }

    private boolean setCurrentBtn(TextView btn) {
        if (currentBtn == null) {
            currentBtn = btn;
        } else if (currentBtn.equals(btn)) {
            return false;
        } else {
            currentBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
        Drawable myImage = getResources().getDrawable(R.drawable.select_icon);
        btn.setCompoundDrawablesWithIntrinsicBounds(null, null, null, myImage);
        currentBtn = btn;
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        labelSelector.hide();
//        super.onActivityResult(requestCode, resultCode, data);
//        if (AppConstants.ACTION_EDIT_LABEL== requestCode && data != null) {
//            String text = data.getStringExtra(AppConstants.PARAM_EDIT_TEXT);
//            if(StringUtils.isNotEmpty(text)){
//                TagItem tagItem = new TagItem(AppConstants.POST_TYPE_TAG,text);
//                addLabel(tagItem);
//            }
//        }else if(AppConstants.ACTION_EDIT_LABEL_POI== requestCode && data != null){
//            String text = data.getStringExtra(AppConstants.PARAM_EDIT_TEXT);
//            if(StringUtils.isNotEmpty(text)){
//                TagItem tagItem = new TagItem(AppConstants.POST_TYPE_POI,text);
//                addLabel(tagItem);
//            }
//        }
    }
}
