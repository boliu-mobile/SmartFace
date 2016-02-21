package com.smartface.base;

import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.customview.CommonTitleBar;

public class BaseFragmentActivity extends FragmentActivity {

    protected CommonTitleBar titleBar;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        //titleBar = (CommonTitleBar) findViewById(R.id.title_layout);
        if (titleBar != null)
            titleBar.setLeftBtnOnclickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
    }
}
