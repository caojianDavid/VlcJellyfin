package com.example.vlcjfy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.chromium.mojo.system.Handle;
import org.json.JSONException;
import org.json.JSONObject;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends XWalkActivity {
    private String TAG = "XWalkActivity";
    private myXWalkView xwalkView;
    private Player player;
//     private VLCPlayer player;
//     private VideoController Controller;
//     private NativePlayer nativePlayer;

    @Override
    protected void onXWalkReady() {
        xwalkView.setResourceClient(new XWalkResourceClient(xwalkView) {
            @Override
            public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView view, XWalkWebResourceRequest request) {
                Uri uri = request.getUrl();
                String path = uri.getPath();
                //Log.d(TAG, "shouldInterceptLoadRequest: console :" + path);
                if(path.endsWith("exoplayer.js")){
                    InputStream is = null;
                    try {
                        is = getAssets().open("frontend/js/exoplayer.js");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    XWalkWebResourceResponse xwrr = createXWalkWebResourceResponse("text/javascript","UTF-8",is);
                    return xwrr;
                }
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

        nativePlayer = new NativePlayer(this);
        xwalkView.addJavascriptInterface(nativePlayer,"NativePlayer");
        xwalkView.addJavascriptInterface(new VLCCallBack(), "ExternalPlayer");
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

    public class VLCCallBack {

        @JavascriptInterface
        public void loadPlayer(String baseUrl,String accessToken,int startIndex,String args) {
            ArrayList<JYFMediaItem> medialist = new ArrayList<>();
            try {
                JSONObject mediaSource = new JSONObject(args);
                JSONArray js = mediaSource.getJSONArray("items");
                for (int i = 0; i < js.length(); i++) {
                    JSONObject jo = js.getJSONObject(i);
                    JYFMediaItem m = new JYFMediaItem();
                    m.Id = jo.getString("id");
                    m.url = baseUrl + "/videos/"+m.Id+"/stream.mp4?static=true";
                    m.name = jo.getString("name");
                    m.startPositionTicks = jo.getLong("startPositionTicks");
                    medialist.add(m);
                }
            } catch (Exception e) {
                Log.d(TAG, "loadPlayer: 异常" + e.toString());
            }
            if(medialist.size() > 0 ){
                player = new Player(getApplicationContext(),baseUrl,accessToken,startIndex,medialist);
            }
        }
        
//         @JavascriptInterface
//         public void toPlay(String videoUrl) {
//             runOnUiThread(new Runnable() {
//                 @Override
//                 public void run() {
//                     FrameLayout ParentView = ((FrameLayout) findViewById(R.id.paview));
//                     player = new VLCPlayer(getApplicationContext());
//                     player.setMedia(videoUrl);
//                     player.setIVLCPlayer(new IVLCPlayer() {
//                         @Override
//                         public void onPlayEnd() {
//                             Controller = null;
//                             player.release();
//                             ParentView.removeView(player);
//                             player = null;
//                             xwalkView.setVisibility(View.VISIBLE);
//                             xwalkView.evaluateJavascript("javascript:window.postmsg('notifyCanceled','')",null);
//                         }
//                     });

//                     FrameLayout.LayoutParams ll = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
//                     ParentView.addView(player, ll);
//                     Controller = new VideoController(getApplicationContext());
//                     Controller.setPlayer(player);
//                     player.setController(Controller);
//                     player.start();

//                     xwalkView.setVisibility(View.GONE);
//                 }
//             });
//         }

        @JavascriptInterface
        public void appExit() {
            Log.d(TAG, "appExit: 退出APP");
            if (player != null) player.release();
            System.exit(0);
        }

//         @JavascriptInterface
//         public String getPostion() {
//             if (player != null) {
//                 return String.valueOf(player.getCurrentPosition());
//             } else {
//                 return "0";
//             }
//         }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown播放器按键按下：" + keyCode);
        if(player != null){
            return player.onKeyDown(keyCode,event);
        }else{
            if (xwalkView.getVisibility() == View.VISIBLE) {
                xwalkView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE));
            }
        }
//         if (nativePlayer.player != null) {
//             int currPostion = nativePlayer.player.getCurrentPosition();
//             if (!nativePlayer.Controller.isShowing()) {
//                 switch (keyCode) {
//                     case KeyEvent.KEYCODE_DPAD_UP:
//                     case KeyEvent.KEYCODE_DPAD_DOWN:
//                         nativePlayer.Controller.show();
//                         return true;
//                     case KeyEvent.KEYCODE_DPAD_RIGHT:
//                         nativePlayer.player.seekTo(currPostion + (30 * 1000));
//                         return true;
//                     case KeyEvent.KEYCODE_DPAD_LEFT:
//                         nativePlayer.player.seekTo(currPostion - (10 * 1000));
//                         return true;
//                     case KeyEvent.KEYCODE_ENTER:
//                     case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
//                         nativePlayer.Controller.play();
//                         return true;
//                     case KeyEvent.KEYCODE_ESCAPE:
//                     case KeyEvent.KEYCODE_BACK:
//                         nativePlayer.Controller.stop();
//                         return true;
//                 }
//             } else {
//                 switch (keyCode) {
//                     case KeyEvent.KEYCODE_ESCAPE:
//                     case KeyEvent.KEYCODE_BACK:
//                         nativePlayer.Controller.hide();
//                         return true;
//                 }
//             }
//             return super.onKeyDown(keyCode, event);
//         } else {
//             switch (keyCode) {
//                 case KeyEvent.KEYCODE_ESCAPE:
//                 case KeyEvent.KEYCODE_BACK:
//                     if (xwalkView.getVisibility() == View.VISIBLE) {
//                         xwalkView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE));
//                     }
//             }
//             return true;
//         }
        //return super.onKeyDown(keyCode, event);
    }
}
