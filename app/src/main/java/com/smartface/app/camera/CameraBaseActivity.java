package com.smartface.app.camera;

import android.os.Bundle;

import com.smartface.base.BaseActivity;


public class CameraBaseActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraManager.getInst().addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraManager.getInst().removeActivity(this);
    }
}
