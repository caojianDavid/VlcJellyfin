package com.example.vlcjfy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private final String TAG = "PlayerActivity";

    public VLCVideoLayout vlcOut;
    public LibVLC libVLC;
    public MediaPlayer player;
    private String[] Aspects = {"1:1", "4:3", "16:9", "16:10", "221:100", "5:4"};
    private Float[] rates = {0.5f, 1.0f, 1.5f, 2.0f, 2.5f, 3.0f};
    private LinearLayout layout_bottom;
    private ImageView mPauseBtn, mSubtitleBtn, mAudioBtn, mStopBtn;
    private ImageView mNext, mPrevious, mSubject;
    private TextView mAspect, mSpeed;
    private SeekBar mSeekBar;
    private ProgressBar mLoadingBar;
    private ImageView mPauseImage;
    private TextView mEndTime, mCurrentTime;

    public int PlayerState = PlaybackState.STATE_NONE;
    public boolean isShowing = false;

    public String baseUrl = "" ,accessToken = "";
    public ArrayList<JYFMediaItem> mediaList = null;
    public int currentItemIndex = 0;
    public long currentPostion = 0;

    private Timer progressTime = null;
    private Timer reportPlayBackTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_control_view);

        Intent intent = getIntent();
        this.baseUrl = intent.getStringExtra("baseUrl");
        this.accessToken = intent.getStringExtra("accessToken");
        this.currentItemIndex = intent.getIntExtra("startIndex",0);
        String options = intent.getStringExtra("options");
        mediaList = new ArrayList<>();
        try {
            JSONObject mediaSource = new JSONObject(options);
            JSONArray js = mediaSource.getJSONArray("items");
            for (int i = 0; i < js.length(); i++) {
                JSONObject jo = js.getJSONObject(i);
                JYFMediaItem m = new JYFMediaItem();
                m.Id = jo.getString("id");
                m.url = baseUrl + "/videos/"+m.Id+"/stream.mp4?static=true";
                m.name = jo.getString("name");
                m.startPositionTicks = jo.getLong("startPositionTicks");
                mediaList.add(m);
            }
        } catch (Exception e) {
            Log.d(TAG, "loadPlayer: 异常" + e.toString());
        }
        if(mediaList.size() > 0 ){
            initPlayer();
        }else{
            finish();
        }
    }

    private void setPlayerListener() {
        player.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                switch (event.type){
                    case MediaPlayer.Event.Opening:  //媒体打开
                        PlayerState = PlaybackState.STATE_STOPPED;
                        break;
                    case MediaPlayer.Event.Buffering: //媒体加载public float getBuffering() 获取加载视频流的进度0-100
                        int Buffering = (int) event.getBuffering();
                        PlayerState = PlaybackState.STATE_BUFFERING;
                        if(Buffering < 100){
                            mLoadingBar.setVisibility(View.VISIBLE);
                            mLoadingBar.setProgress(Buffering);
                        }else{
                            mLoadingBar.setVisibility(View.GONE);
                        }
                        break;
                    case MediaPlayer.Event.Playing: //媒体打开成功
                        PlayerState = PlaybackState.STATE_PLAYING;
                        HideController();
                        mPauseImage.setVisibility(View.GONE);
                        mPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_pause_48));
                        break;
                    case MediaPlayer.Event.Paused://媒体暂停
                        PlayerState = PlaybackState.STATE_PAUSED;
                        mPauseImage.setVisibility(View.VISIBLE);
                        mPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_play_arrow_48));
                        break;
                    case MediaPlayer.Event.Stopped://媒体结束、中断
                        Log.d(TAG, "onEvent: 播放结束！");
                        reportPlayBackTime.cancel();  //取消进度报告
                        reportPlayBackTime = null;
                        PlayerState = PlaybackState.STATE_STOPPED;
                        ReportPlayback.ReportPlaybackStop(      //报告状态结束
                                baseUrl,
                                mediaList.get(currentItemIndex).Id,
                                event.getTimeChanged(),
                                accessToken
                        );
                        PlayStop();
                        break;
                    case MediaPlayer.Event.EndReached://媒体播放结束
                        Log.d(TAG, "onEvent: EndReached");
                        break;
                    case MediaPlayer.Event.EncounteredError://媒体播放错误
                        Log.d(TAG, "onEvent: EncounteredError");
                        break;
                    case MediaPlayer.Event.TimeChanged://视频时间变化
                        currentPostion = event.getTimeChanged();
                        break;
                    case MediaPlayer.Event.PositionChanged://视频总时长的百分比
                        break;
                    case MediaPlayer.Event.SeekableChanged:
                        break;
                    case MediaPlayer.Event.PausableChanged:
                        Log.d(TAG, "onEvent: PausableChanged");
                        break;
                    case MediaPlayer.Event.LengthChanged:
                        Log.d(TAG, "onEvent: LengthChanged");
                        break;
                    case MediaPlayer.Event.Vout://当图像输出
                        Log.d(TAG, "onEvent: Vout");
                        break;
                    case MediaPlayer.Event.ESAdded:
                        Log.d(TAG, "onEvent: ESAdded");
                        break;
                    case MediaPlayer.Event.ESDeleted:
                        Log.d(TAG, "onEvent: ESDeleted");
                        break;
                    case MediaPlayer.Event.ESSelected:
                        Log.d(TAG, "onEvent: ESSelected");
                        break;
                    case MediaPlayer.Event.RecordChanged:
                        Log.d(TAG, "onEvent: RecordChanged");
                        break;
                }
            }
        });
    }

    private void initPlayer() {
        vlcOut = findViewById(R.id.vlcVideoLayout);
        layout_bottom = findViewById(R.id.layout_bottom);
        mStopBtn = findViewById(R.id.tv_stop);
        mPauseBtn = findViewById(R.id.play_pause);
        mSubtitleBtn = findViewById(R.id.tv_subtrack);
        mAudioBtn = findViewById(R.id.tv_audiotrack);
        mAspect = findViewById(R.id.tv_aspect);
        mSpeed = findViewById(R.id.tv_speed);
        mNext = findViewById(R.id.tv_next);
        mPrevious = findViewById(R.id.tv_previous);
        mSubject = findViewById(R.id.tv_subject);
        mLoadingBar = findViewById(R.id.pb_loading);
        mPauseImage = findViewById(R.id.pauseImage);
        mSeekBar = findViewById(R.id.bottom_seek_progress);
        mSeekBar.setMax(1000);
        mEndTime = findViewById(R.id.total);
        mCurrentTime = findViewById(R.id.current);

        mSeekBar.setOnSeekBarChangeListener(this);
        mStopBtn.setOnClickListener(this);
        mPauseBtn.setOnClickListener(this);
        mSubtitleBtn.setOnClickListener(this);
        mAudioBtn.setOnClickListener(this);
        mAspect.setOnClickListener(this);
        mSpeed.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mPrevious.setOnClickListener(this);
        mSubject.setOnClickListener(this);

        libVLC = new LibVLC(this, null);
        player = new MediaPlayer(libVLC);
        player.attachViews(vlcOut, null, true, false);
        setPlayerListener();

        PlayStart(currentItemIndex);
    }

    private PopupMenu CreatePopupMenu(View pview){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            return new PopupMenu(this, pview, Gravity.CENTER);
        } else {
            return new PopupMenu(this, pview);
        }
    }

    // type 1:播放速率，2:宽高比
    public void showPopupMenu(View pview, ArrayList list, int type) {
        PopupMenu popupMenu = CreatePopupMenu(pview);
        for (int i = 0; i < list.size(); i++) {
            popupMenu.getMenu().add(type, i,i, String.valueOf(list.get(i)));
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int itemid = menuItem.getItemId();
                int type = menuItem.getGroupId();
                Log.d(TAG, "onMenuItemClick: " + itemid + ":" + type);
                if (type == 1) {
                    Float rate = rates[itemid];
                    player.setRate(rate);
                    mSpeed.setText(String.valueOf(rate));
                } else if (type == 2) {
                    String aspect = Aspects[itemid];
                    player.setAspectRatio(aspect);
                    mAspect.setText(aspect);
                }
                return true;
            }
        });
        popupMenu.show();
    }

    // type 3:音轨，4：字幕
    public void showPopupMenu(View pview, MediaPlayer.TrackDescription[] tracks, int type) {
        if(tracks == null) return;
        PopupMenu popupMenu = CreatePopupMenu(pview);
        for (int i = 0; i < tracks.length; i++) {
            MediaPlayer.TrackDescription track = tracks[i];
            popupMenu.getMenu().add(type, track.id,i, track.name);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int itemid = menuItem.getItemId();
                int type = menuItem.getGroupId();
                Log.d(TAG, "onMenuItemClick: " + itemid + ":" + type);
                if (type == 3) {
                    player.setAudioTrack(itemid);
                } else if (type == 4) {
                    player.setSpuTrack(itemid);
                }
                return true;
            }
        });
        popupMenu.show();
    }

    public void ShowController(){
        layout_bottom.setVisibility(View.VISIBLE);
        isShowing = true;
        progressTime = new Timer();
        progressTime.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setProgress();
                    }
                });
            }
        },1000,1000);
    }

    public void HideController(){
        if(progressTime != null){
            progressTime.cancel();
            progressTime = null;
        }
        layout_bottom.setVisibility(View.GONE);
        isShowing = false;
    }

    private void setProgress() {
        if (player != null) {
            long position = player.getTime();
            long duration = player.getLength();
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mSeekBar.setProgress((int) pos);
            }
            mEndTime.setText(utils.TrickToTime(duration));
            mCurrentTime.setText(utils.TrickToTime(position));
        }
    }

    public void PlayStart(int mediaListIndex) {
        if(mediaListIndex >= 0 && mediaListIndex < mediaList.size()) {
            currentItemIndex = mediaListIndex;
            Uri uri = Uri.parse(mediaList.get(mediaListIndex).url);
            Media media = new Media(libVLC, uri);
            player.setMedia(media);
            media.release();
            //player.setTime(mediaList.get(mediaListIndex).startPositionTicks / 10000);
            player.play();

            reportPlayBackTime = new Timer();
            reportPlayBackTime.schedule(new TimerTask() {
                @Override
                public void run() {
                    ReportPlayback.ReportPlaybackProgress(baseUrl,
                            mediaList.get(currentItemIndex).Id,
                            !player.isPlaying(),
                            currentPostion,
                            accessToken
                    );
                }
            },10000,10000);
        }
    }

    public void play(){
        if(player.isPlaying()){
            player.pause();
        }else{
            player.play();
        }
    }

    public void PlayStop(){
        PlayStart(currentItemIndex + 1);
    }

    public void release(){
        if(reportPlayBackTime != null) reportPlayBackTime.cancel();
        if(progressTime != null) progressTime.cancel();
        player.release();
        libVLC.release();
        this.finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_pause:
                play();
                break;
            case R.id.tv_stop:
                player.stop();
                break;
            case R.id.tv_next:
                player.stop();
                PlayStart(currentItemIndex + 1);
                break;
            case R.id.tv_previous:
                player.stop();
                PlayStart(currentItemIndex -1);
                break;
            case R.id.tv_subtrack:
                showPopupMenu(view, player.getSpuTracks(),4);
                break;
            case R.id.tv_audiotrack:
                showPopupMenu(view, player.getAudioTracks(),3);
                break;
            case R.id.tv_aspect:
                showPopupMenu(view,new ArrayList<String>(Arrays.asList(Aspects)),2);
                break;
            case R.id.tv_speed:
                showPopupMenu(view,new ArrayList<Float>(Arrays.asList(rates)),1);
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (b) {
            double bfb = (double) i / 1000;
            int pos = (int) (bfb * player.getLength());
            player.setTime(pos);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(!isShowing){
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    ShowController();
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    player.setTime(currentPostion + 30000);
                    return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    player.setTime(currentPostion - 10000);
                    return true;
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    player.play();
                    return true;
                case KeyEvent.KEYCODE_ESCAPE:
                case KeyEvent.KEYCODE_BACK:
                    release();
                    return true;
                //退出
            }
        }else {
            switch (keyCode){
                case KeyEvent.KEYCODE_ESCAPE:
                case KeyEvent.KEYCODE_BACK:
                    HideController();
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}