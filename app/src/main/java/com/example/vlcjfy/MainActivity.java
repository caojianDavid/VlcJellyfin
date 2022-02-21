package com.example.vlcjfy;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.media.VideoView;
import org.videolan.libvlc.util.VLCVideoLayout;
import org.xwalk.core.JavascriptInterface;
import org.xwalk.core.XWalkActivity;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import org.xwalk.core.XWalkWebResourceResponse;

import java.util.ArrayList;

public class MainActivity extends XWalkActivity {
    private String TAG = "XWalkActivity";
    private myXWalkView xwalkView;
    private VLCPlayer player;
    private VideoController Controller;

    @Override
    protected void onXWalkReady() {
        xwalkView.setResourceClient(new XWalkResourceClient(xwalkView) {
            @Override
            public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView view, XWalkWebResourceRequest request) {
                return super.shouldInterceptLoadRequest(view, request);
            }
        });
        XWalkSettings settings = xwalkView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(XWalkSettings.LOAD_NO_CACHE);
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        xwalkView.addJavascriptInterface(new VLCCallBack(), "NativeInterface");
        String url = "file:///android_asset/frontend/index.html";
        xwalkView.loadUrl(url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xwalkView = findViewById(R.id.xwalkview);
        // turn on debugging
        //XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
    }

//    @Override
//    public void onBackPressed() {
//        if(player != null){
//            player.stop();
//            return;
//        }
//        super.onBackPressed();
//    }

    public class VLCCallBack {

        @JavascriptInterface
        public void toPlay(String videoUrl) {
            runOnUiThread(() -> {
                FrameLayout ParentView = ((FrameLayout) findViewById(R.id.paview));
                player = new VLCPlayer(getApplicationContext());
                player.setMedia(videoUrl);
                player.setIVLCPlayer(new IVLCPlayer() {
                    @Override
                    public void onPlayEnd() {
                        ViewGroup vg = (ViewGroup) player.getParent();
                        if(vg != null){
                            Controller = null;
                            player.release();
                            ParentView.removeView(player);
                            player = null;
                            xwalkView.setVisibility(View.VISIBLE);
                        }
                    }
                });

                FrameLayout.LayoutParams ll = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
                ParentView.addView(player,ll);
                //player.requestFocus();
                Controller = new VideoController(ParentView);
                Controller.setPlayer(player);
                player.setController(Controller);
                player.start();

                xwalkView.setVisibility(View.GONE);
            });
        }

        @JavascriptInterface
        public void appExit() {
            Log.d(TAG, "appExit: 退出APP");
            if(player != null) player.release();
            System.exit(0);
        }

        @JavascriptInterface
        public String getPostion() {
            if (player != null) {
                return String.valueOf(player.getCurrentPosition());
            } else {
                return "0";
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "播放器按键按下：" + keyCode);
        if (player != null) {
            int currPostion = player.getCurrentPosition();
            if(!Controller.isShowing()){
                switch (keyCode){
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        Controller.show();
                        return true;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        player.seekTo(currPostion + (30 * 1000));
                        return true;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        player.seekTo(currPostion - (10 * 1000));
                        return true;
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        Controller.play();
                        return true;
                    case KeyEvent.KEYCODE_ESCAPE:
                    case KeyEvent.KEYCODE_BACK:
                        Controller.stop();
                        return true;
                }
            }else{
                switch (keyCode){
                    case KeyEvent.KEYCODE_ESCAPE:
                    case KeyEvent.KEYCODE_BACK:
                        Controller.hide();
                        return true;
                }
            }
            return super.onKeyDown(keyCode, event);
        }else {
            switch (keyCode){
                case KeyEvent.KEYCODE_ESCAPE:
                case KeyEvent.KEYCODE_BACK:
                    if(xwalkView.getVisibility() == View.VISIBLE){
                        xwalkView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE));
                    }
                    return true;
            }
            return true;
        }
        //return super.onKeyDown(keyCode, event);
    }
}