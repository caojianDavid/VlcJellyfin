package com.example.vlcjfy;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;

import org.xwalk.core.XWalkView;

public class myXWalkView extends XWalkView {
    private String TAG = "自定义XwalkView";
    
    public myXWalkView(Context context) {
        super(context);
    }

    public myXWalkView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public myXWalkView(Context context, Activity activity) {
        super(context, activity);
    }

    @Override
    public void onHide() {
        Log.d(TAG, "onHide: " );
        //super.onHide();
    }

    @Override
    public void onShow() {
        Log.d(TAG, "onShow: ");
        //super.onShow();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            return false;
        }
        return super.dispatchKeyEvent(event);
    }
}
