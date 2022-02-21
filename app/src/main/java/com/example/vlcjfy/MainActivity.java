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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private VLCPlayer player;
    private VideoController Controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String url = "https://cc.noqazx.cyou:33380/jyf/videos/5058a9ea-e4cc-2563-ff7d-2d4a798fc37a/stream.mp4?static=true";
        String url2 = "https://stream7.iqilu.com/10339/upload_transcode/202002/18/20200218114723HDu3hhxqIT.mp4";

        ViewGroup ParentView = (ViewGroup) this.getWindow().getDecorView();
        player = new VLCPlayer(this);
        player.setMedia(url);
        player.setIVLCPlayer(new IVLCPlayer() {
            @Override
            public void onPlayEnd() {
                ViewGroup vg = (ViewGroup) player.getParent();
                if(vg != null){
                    player.release();
                    vg.removeView(player);
                    player = null;
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
    }

    @Override
    public void onBackPressed() {
        if(player != null){
            player.stop();
            return;
        }
        super.onBackPressed();
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
                        player.start();
                        return true;
                    case KeyEvent.KEYCODE_ESCAPE:
                    case KeyEvent.KEYCODE_BACK:
                        player.stop();
                        return false;
                }
            }else{
                switch (keyCode){
                    case KeyEvent.KEYCODE_ESCAPE:
                    case KeyEvent.KEYCODE_BACK:
                        Controller.hide();
                        return true;
                    default:
                        return super.onKeyDown(keyCode, event);
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}