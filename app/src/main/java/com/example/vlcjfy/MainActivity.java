package com.example.vlcjfy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import org.json.JSONArray;
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

    @Override
    protected void onXWalkReady() {
        xwalkView.setResourceClient(new XWalkResourceClient(xwalkView) {
            @Override
            public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView view, XWalkWebResourceRequest request) {
                Uri uri = request.getUrl();
                String path = uri.getPath();
                //Log.d(TAG, "shouldInterceptLoadRequest: console :" + path);
                if (path.endsWith("exoplayer.js")) {
                    InputStream is = null;
                    try {
                        is = getAssets().open("frontend/js/exoplayer.js");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    XWalkWebResourceResponse xwrr = createXWalkWebResourceResponse("text/javascript", "UTF-8", is);
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

        xwalkView.addJavascriptInterface(new VLCCallBack(), "NativePlayer");
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

    @Override
    public void onBackPressed() {
        xwalkView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE));
        //super.onBackPressed();
    }

    public class VLCCallBack {

        @JavascriptInterface
        public void loadPlayer(String baseUrl, String accessToken, int startIndex, String options) {
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            intent.putExtra("baseUrl", baseUrl);
            intent.putExtra("accessToken", accessToken);
            intent.putExtra("startIndex", accessToken);
            intent.putExtra("options", options);
            startActivity(intent);
        }

        @JavascriptInterface
        public void appExit() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("确认退出？");
            builder.setTitle("确认");
            builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    System.exit(0);
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
    }
}
