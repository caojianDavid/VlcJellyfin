package com.example.vlcjfy;

import android.app.Activity;
import android.media.session.PlaybackState;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.vlcjfy.IVLCPlayer;
import com.example.vlcjfy.VLCPlayer;
import com.example.vlcjfy.VideoController;

import org.json.JSONArray;
import org.json.JSONObject;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.xwalk.core.JavascriptInterface;
import org.xwalk.core.XWalkView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class NativePlayer {
    private final String TAG = "NativePlayer";
    private Activity mainactivity;
    public VLCPlayer player;
    public VideoController Controller;
    public XWalkView xWalkView;
    public ArrayList<JYFMediaItem> medialist;
    public int currentItem = 0;
    public String BaseUrl = "";
    public String token = "";
    public Timer timer = new Timer();

    public NativePlayer(Activity mainactivity) {
        this.mainactivity = mainactivity;
        xWalkView = mainactivity.getWindow().findViewById(R.id.xwalkview);
    }

    @JavascriptInterface
    public boolean isEnabled() {
        return true;
    }

    @JavascriptInterface
    public void loadPlayer(String baseUrl,String accessToken,String args) {
        BaseUrl =  baseUrl;
        token = accessToken;
        Log.d(TAG, "loadPlayer: " + baseUrl + " args:" + args);
        JSONObject mediaSource = null;
        medialist = new ArrayList<>();
        try {
            mediaSource = new JSONObject(args);
            currentItem = mediaSource.getInt("startIndex");
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
        if(medialist.size() > 0 && currentItem < medialist.size()){
            createPlayer(medialist.get(currentItem));
        }

    }

    @JavascriptInterface
    public void createPlayer(JYFMediaItem mediaItem) {
        player = new VLCPlayer(mainactivity.getApplicationContext());
        player.setMedia(mediaItem.url);
        FrameLayout.LayoutParams ll = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mainactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                player.setIVLCPlayer(new IVLCPlayer() {
                    @Override
                    public void onPlayEnd() {
                        timer.cancel();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ReportPlayback.ReportPlaybackStop(BaseUrl, mediaItem.Id, player.getCurrentPosition(),token);
                            }
                        }).start();
                        destroyPlayer();
                        xWalkView.setVisibility(View.VISIBLE);
                    }
                });
                mainactivity.addContentView(player, ll);
                Controller = new VideoController(player.getContext());
                Controller.setPlayer(player);
                player.setController(Controller);
                xWalkView.setVisibility(View.GONE);
            }
        });
        player.start();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(player != null) {
                    ReportPlayback.ReportPlaybackProgress(BaseUrl,
                            mediaItem.Id,
                            player.state == PlaybackState.STATE_PAUSED,
                            player.getCurrentPosition(),
                            token
                    );
                }
            }
        },10000,10000);
    }

    @JavascriptInterface
    public void pausePlayer() {
        Log.d(TAG, "pausePlayer: 暂停播放！");
        if (player.isPlaying()) {
            Controller.play();
        }
    }

    @JavascriptInterface
    public void resumePlayer() {
        Log.d(TAG, "resumePlayer: 恢复播放！");
        if (!player.isPlaying()) {
            Controller.play();
        }
    }

    @JavascriptInterface
    public void stopPlayer() {
        Log.d(TAG, "stopPlayer: 停止播放！");
        if (Controller != null) {
            Controller.stop();
        }
    }

    @JavascriptInterface
    public void destroyPlayer() {
        Log.d(TAG, "destroyPlayer: 销毁!");
        Controller = null;
        player.release();
        //ParentView.removeView(player);
        ((ViewGroup) player.getParent()).removeView(player);
        player = null;
    }

    @JavascriptInterface
    public void seek(long ticks) {
        Log.d(TAG, "seek: " + ticks);
        if (player != null) {
            player.seekTo((int) ticks);
        }
    }

    @JavascriptInterface
    public void seekMs(long ms) {
        Log.d(TAG, "seekMs: " + ms);
        if (player != null) {
            player.seekTo((int) ms);
        }
    }

    @JavascriptInterface
    public void setVolume(int volume) {
        Log.d(TAG, "setVolume: " + volume);
        if (player != null) {

        }
    }
}
