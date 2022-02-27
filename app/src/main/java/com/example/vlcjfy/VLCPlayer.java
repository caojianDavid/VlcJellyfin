package com.example.vlcjfy;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.ILibVLC;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;

public class VLCPlayer extends VLCVideoLayout implements MediaController.MediaPlayerControl {
    private String TAG = "VLCPlayer";
    final static Boolean SUBTITLE = true;
    final static Boolean USE_TEXTUREVIEW = false;

    private Context context = null;
    private VLCVideoLayout mVideoLayout = null;
    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    private VideoController Controller = null;
    private IVLCPlayer ivlcPlayer=null;

    private int Buffering = 0;
    private int CurrentPostion = 0;

    public VLCPlayer(@NonNull Context context) {
        super(context);
        this.context = context;
        init();
    }

    public VLCPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VLCPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VLCPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(){
        ArrayList<String> args = new ArrayList<>();
        args.add("-v");
        mLibVLC = new LibVLC(this.context,args);
        mMediaPlayer = new MediaPlayer(mLibVLC);
        mMediaPlayer.attachViews(this,null,SUBTITLE,USE_TEXTUREVIEW);

        setListener();
    }

    public void setMedia(String url){
        Uri uri = Uri.parse(url);
        Media media = new Media(mLibVLC,uri);
        setMedia(media);
        media.release();
    }

    public void setMedia(Media media){
        mMediaPlayer.setMedia(media);
    }

    public void setController(VideoController controller){
        this.Controller = controller;
    }

    public void setIVLCPlayer(IVLCPlayer ivlcPlayer){
        this.ivlcPlayer = ivlcPlayer;
    }

    public void setListener(){
        mMediaPlayer.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                switch (event.type){
                    case MediaPlayer.Event.Opening:  //媒体打开
                        Log.d(TAG, "onEvent: 媒体打开");
                        break;
                    case MediaPlayer.Event.Buffering: //媒体加载public float getBuffering() 获取加载视频流的进度0-100
                        Buffering = (int) event.getBuffering();
                        Log.d(TAG, "onEvent: 加载中" + Buffering);
                        Controller.showLoading(Buffering);
                        break;
                    case MediaPlayer.Event.Playing: //媒体打开成功
                        Log.d(TAG, "onEvent: 媒体打开成功");
                        Controller.hide();
                        Controller.hidePauseImage();
                        break;
                    case MediaPlayer.Event.Paused://媒体暂停
                        Log.d(TAG, "onEvent: 媒体暂停");
                        Controller.showPauseImage();
                        break;
                    case MediaPlayer.Event.Stopped://媒体结束、中断
                        Log.d(TAG, "onEvent: 媒体结束、中断Stopped");
                        if(ivlcPlayer != null){
                            ivlcPlayer.onPlayEnd();
                        }
                        break;
                    case MediaPlayer.Event.EndReached://媒体播放结束
                        Log.d(TAG, "onEvent: 媒体播放结束 EndReached");
                        break;
                    case MediaPlayer.Event.EncounteredError://媒体播放错误
                        Log.d(TAG, "onEvent: 媒体播放错误");
                        break;
                    case MediaPlayer.Event.TimeChanged://视频时间变化
                        // public long getTimeChanged(); 获取当前播放视频的时间
                        // public native long getLength();//获取视频总时间
                        CurrentPostion = (int) event.getTimeChanged();
                        if(ivlcPlayer != null){
                            ivlcPlayer.onPostionChenged();
                        }
                        break;
                    case MediaPlayer.Event.PositionChanged://视频总时长的百分比public float getPositionChanged()//获取当前视频总时长的百分比0-1
                        //public native void setPosition(float var1);//让视频跳到指定位置
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

    @Override
    public void start() {
        if(mMediaPlayer != null) {
            mMediaPlayer.play();
        }
    }

    @Override
    public void pause() {
        if(mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    @Override
    public int getDuration() {
        if(mMediaPlayer != null) {
            return (int) mMediaPlayer.getLength();
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(mMediaPlayer != null) {
            return CurrentPostion;
        }
        return 0;
    }

    @Override
    public void seekTo(int i) {
        if(mMediaPlayer != null) {
            mMediaPlayer.setTime(i);
        }
    }

    @Override
    public boolean isPlaying() {
        if(mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        if(mMediaPlayer != null) {
            return Buffering;
        }
        return 0;
    }

    @Override
    public boolean canPause() {
        if(mMediaPlayer != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        if(mMediaPlayer != null) {
            return mMediaPlayer.isSeekable();
        }
        return false;
    }

    @Override
    public boolean canSeekForward() {
        if(mMediaPlayer != null) {
            return mMediaPlayer.isSeekable();
        }
        return false;
    }

    @Override
    public int getAudioSessionId() {
        if(mMediaPlayer != null){
            mMediaPlayer.getAudioTrack();
        }
        return 0;
    }

    public void stop(){
        if(mMediaPlayer != null){
            mMediaPlayer.stop();
        }
    }

    public void release(){
        if(mMediaPlayer != null){
            mMediaPlayer.release();
            mLibVLC.release();
        }
    }

    public void setAspect(String aspect){
        if(mMediaPlayer != null){
            mMediaPlayer.setAspectRatio(aspect);
        }
    }

    public void setSpeed(float v){
        if(mMediaPlayer != null){
            mMediaPlayer.setRate(v);
        }
    }

    public MediaPlayer.TrackDescription[] getAudioTracks(){
        if(mMediaPlayer != null) {
            return mMediaPlayer.getAudioTracks();
        }
        return null;
    }

    public void setAudioTrack(int i){
        if(mMediaPlayer != null){
            mMediaPlayer.setAudioTrack(i);
        }
    }

    public MediaPlayer.TrackDescription[] getSpuTracks(){
        if(mMediaPlayer != null) {
            return mMediaPlayer.getSpuTracks();
        }
        return null;
    }

    public void setSpuTrack(int i){
        if(mMediaPlayer != null){
            mMediaPlayer.setSpuTrack(i);
        }
    }
}
