package com.example.vlcjfy;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.MediaPlayer;

import java.util.Formatter;
import java.util.Locale;


public class VideoController implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private String TAG = "VideoController";
    private ViewGroup ParentView = null;
    private boolean mShowing = false;
    private VLCPlayer player;
    private View ControllerView;

    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int sDefaultTimeout = 5000;

    private LinearLayout subtitleView;
    private LinearLayout layout_bottom;
    private ImageView mPauseBtn, mSubtitleBtn, mAudioBtn, mStopBtn;
    private TextView tv_aspect, tv_speed;
    private SeekBar seekBar;
    private TextView mEndTime, mCurrentTime;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSE = 2;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FADE_OUT:
                    hide();
                    break;
                case SHOW_PROGRESS:
                    long pos = setProgress();
                    if (player != null) {
                        if (mShowing && player.isPlaying()) {
                            msg = obtainMessage(SHOW_PROGRESS);
                            sendMessageDelayed(msg, 1000 - (pos % 1000));
                        }
                    }
                    break;
            }
            //super.handleMessage(msg);
        }
    };

    VideoController(ViewGroup parentView) {
        this.ParentView = parentView;
        CreateController();
    }

    private void CreateController() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
        );
        LayoutInflater inflate = (LayoutInflater) ParentView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ControllerView = inflate.inflate(R.layout.player_control_view, null);
        ParentView.addView(ControllerView, layoutParams);
        //ControllerView.setVisibility(GONE);

        subtitleView = ControllerView.findViewById(R.id.subtitleView);
        layout_bottom = ControllerView.findViewById(R.id.layout_bottom);
        mStopBtn = ControllerView.findViewById(R.id.tv_stop);
        mPauseBtn = ControllerView.findViewById(R.id.play_pause);
        mSubtitleBtn = ControllerView.findViewById(R.id.tv_subtrack);
        mAudioBtn = ControllerView.findViewById(R.id.tv_audiotrack);
        tv_aspect = ControllerView.findViewById(R.id.tv_aspect);
        tv_speed = ControllerView.findViewById(R.id.tv_speed);
        seekBar = ControllerView.findViewById(R.id.bottom_seek_progress);
        seekBar.setMax(1000);
        mEndTime = ControllerView.findViewById(R.id.total);
        mCurrentTime = ControllerView.findViewById(R.id.current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        seekBar.setOnSeekBarChangeListener(this);
        mStopBtn.setOnClickListener(this);
        mPauseBtn.setOnClickListener(this);
        mSubtitleBtn.setOnClickListener(this);
        mAudioBtn.setOnClickListener(this);
        tv_aspect.setOnClickListener(this);
        tv_speed.setOnClickListener(this);

        setListener();
    }

    public void setPlayer(VLCPlayer player){
        this.player = player;
    }

    private long setProgress() {
        if (player == null) {
            return 0;
        }

        long position = player.getCurrentPosition();
        long duration = player.getDuration();
        if (seekBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                seekBar.setProgress((int) pos);
            }
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime((int) duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime((int) position));

        return position;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public void show() {
        show(sDefaultTimeout);
    }

    public void show(int ShowTime) {
        if (!mShowing) {
            setProgress();
            layout_bottom.setVisibility(VISIBLE);
            mShowing = true;
        }

        handler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = handler.obtainMessage(FADE_OUT);
        if (ShowTime != 0) {
            handler.removeMessages(FADE_OUT);
            handler.sendMessageDelayed(msg, ShowTime);
        }
        mPauseBtn.requestFocus();
    }

    public void hide() {
        try {
            layout_bottom.setVisibility(GONE);
            handler.removeMessages(SHOW_PROGRESS);
        } catch (IllegalArgumentException ex) {
            Log.w("MediaController", "already removed");
        }
        mShowing = false;
    }

    public boolean isShowing() {
        return mShowing;
    }

    public void showTrackWindow(View view, int Type) {
        PopupMenu popupMenu = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            popupMenu = new PopupMenu(this.ControllerView.getContext(), view, Gravity.CENTER);
        }
        if (player != null) { // 判断播放器

//            ITrackInfo[] mIjkTrackInfo = player.getTrackInfo(); //这里可以获得所有的轨道信息
//            for (int i = 0; i < mIjkTrackInfo.length; i++) {
//                ITrackInfo trackInfo = mIjkTrackInfo[i];
//                Log.d(TAG, "showTrackWindow: 轨：" + trackInfo.getInfoInline());
//                if(trackInfo.getTrackType() == Type){
//                    popupMenu.getMenu().add(Menu.NONE,i,i,trackInfo.getInfoInline());
//                }
//                if (trackInfo.getTrackType() == ITrackInfo.MEDIA_TRACK_TYPE_AUDIO) { //判断是否是音轨信息
//
//                } else if (trackInfo.getTrackType() == ITrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) { //判断是否是内嵌字幕信息
//                    Log.d("checkSubTitle: ", trackInfo.getInfoInline());
//                }
//            }
        }
//        if(popupMenu  != null){
//            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem menuItem) {
//                    player.setTrackStream(menuItem.getItemId());
//                    Log.d(TAG, "onMenuItemClick: 设置轨道：" + menuItem.getTitle() + " ID: " + menuItem.getItemId());
//                    return true;
//                }
//            });
//            popupMenu.show();
//        }
    }

    public void showSubTitle(String text) {
        TextView textView = new TextView(subtitleView.getContext());
        LinearLayout.LayoutParams tll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        textView.setLayoutParams(tll);
        textView.setTextColor(player.getResources().getColor(R.color.white));
        textView.setTextSize(32);
        textView.setText(text);
        subtitleView.removeAllViews();
        subtitleView.addView(textView);
    }

    public void playerStateChanged(int state) {
        switch (state) {
            case STATE_PLAYING:
                mPauseBtn.setImageDrawable(player.getResources().getDrawable(R.drawable.ic_baseline_pause_48));
                break;
            case STATE_PAUSE:
                mPauseBtn.setImageDrawable(player.getResources().getDrawable(R.drawable.ic_baseline_play_arrow_48));
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_pause:
                player.start();
                break;
            case R.id.tv_stop:
                player.onKeyDown(KeyEvent.KEYCODE_BACK, new KeyEvent(KeyEvent.KEYCODE_BACK, KeyEvent.ACTION_DOWN));
                break;
            case R.id.tv_subtrack:
                //showTrackWindow(view,IjkTrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT);
                break;
            case R.id.tv_audiotrack:
                //showTrackWindow(view,IjkTrackInfo.MEDIA_TRACK_TYPE_AUDIO);
                break;
            case R.id.tv_aspect:
                break;
            case R.id.tv_speed:
                setSpeed();
                break;
        }
    }

    public void setSpeed() {
//        float spe = player.getSpeed();
//        spe += 1;
//        if(spe > 3) spe = 1;
//        tv_speed.setText(String.valueOf(spe));
//        player.setSpeed(spe);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (b) {
            double bfb = (double) i / 1000;
            int pos = (int) (bfb * player.getDuration());
            player.seekTo(pos);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void setListener() {
    }
}
