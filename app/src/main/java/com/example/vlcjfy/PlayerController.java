package com.example.vlcjfy;

import static java.lang.Math.abs;

import android.content.Context;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.RendererItem;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.interfaces.IMediaList;
import org.videolan.libvlc.interfaces.IVLCVout;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;

public class PlayerController extends VLCVideoLayout implements IVLCVout.Callback , MediaPlayer.EventListener {
    private String TAG = "PlayerController";

    private Context context = null;
    private MediaPlayer mediaplayer = newMediaPlayer();
    private LibVLC mLibVLC = null;
    private boolean switchToVideo = false;
    private boolean seekable = false;
    private boolean pausable = false;
    IMedia.Stats previousMediaStats = null;
    private boolean hasRenderer = false;

    private Progress progress = new Progress(); // = Progress();
    private float speed = 1.0f;

    public PlayerController(@NonNull Context context) {
        super(context);
    }

    public PlayerController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PlayerController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public IVLCVout getVout(){
        return mediaplayer.getVLCVout();
    }

    public boolean canDoPassthrough(){
        return mediaplayer.hasMedia() && !mediaplayer.isReleased() && mediaplayer.canDoPassthrough();
    }

    public IMedia getMedia() {
        return mediaplayer.getMedia();
    }

    public void play() {
        if (mediaplayer.hasMedia() && !mediaplayer.isReleased()) mediaplayer.play();
    }

    public boolean pause(){
        if (isPlaying() && mediaplayer.hasMedia() && pausable) {
            mediaplayer.pause();
            return true;
        }
        return false;
    }

    public void stop() {
        if (mediaplayer.hasMedia() && !mediaplayer.isReleased()) mediaplayer.stop();
        setPlaybackStopped();
    }

    public void releaseMedia() {
        IMedia it = getMedia();
        it.setEventListener(null);
        it.release();
    }

    public void resetPlaybackState(long time,long duration) {
        seekable = true;
        pausable = true;
        lastTime = time;
        updateProgress(time, duration);
    }

    @MainThread
    public void restart() {
        MediaPlayer mp = mediaplayer;
        int volume = !mp.isReleased() ? mp.getVolume() : 0;
        mediaplayer = newMediaPlayer();
        if (volume > 100) {
            mediaplayer.setVolume(volume);
        }
        release(mp);
    }

    public void setPosition(float position) {
        if (seekable && mediaplayer.hasMedia() && !mediaplayer.isReleased()) mediaplayer.setPosition(position);
    }

    public void setTime(long time , boolean fast) {
        if (seekable && mediaplayer.hasMedia() && !mediaplayer.isReleased()) mediaplayer.setTime(time, fast);
    }

    public boolean isPlaying() {
        return playbackState == PlaybackState.STATE_PLAYING;
    }

    public boolean isVideoPlaying() {
        return !mediaplayer.isReleased() && mediaplayer.getVLCVout().areViewsAttached();
    }

    public boolean canSwitchToVideo() {
        return getVideoTracksCount() > 0;
    }

    public int getVideoTracksCount() {
        if (!mediaplayer.isReleased() && mediaplayer.hasMedia()) return mediaplayer.getVideoTracksCount();
        return 0;
    }

    public MediaPlayer.TrackDescription[] getVideoTracks() {
        MediaPlayer.TrackDescription[] vts = new MediaPlayer.TrackDescription[]{};
        if (!mediaplayer.isReleased() && mediaplayer.hasMedia()) vts =  mediaplayer.getVideoTracks();
        return vts;
    }

    public int getVideoTrack() {
        if (!mediaplayer.isReleased() && mediaplayer.hasMedia()) return mediaplayer.getVideoTrack();
        return -1;
    }

    public IMedia.VideoTrack getCurrentVideoTrack() {
        IMedia.VideoTrack vt = null;
        if (!mediaplayer.isReleased() && mediaplayer.hasMedia()) vt = mediaplayer.getCurrentVideoTrack();
        return null;
    }

    public int getAudioTracksCount() {
        if (!mediaplayer.isReleased() && mediaplayer.hasMedia()) return mediaplayer.getAudioTracksCount();
        return 0;
    }

    public MediaPlayer.TrackDescription[] getAudioTracks() {
        MediaPlayer.TrackDescription[] td = new MediaPlayer.TrackDescription[]{};
        if (!mediaplayer.isReleased() && mediaplayer.hasMedia()) td = mediaplayer.getAudioTracks();
        return td;
    }

    public int getAudioTrack() {
        if (!mediaplayer.isReleased() && mediaplayer.hasMedia()) return mediaplayer.getAudioTrack();
        return -1;
    }

    public void setVideoTrack(int index) {
        if(!mediaplayer.isReleased() && mediaplayer.hasMedia()){
            mediaplayer.setVideoTrack(index);
        }
    }

    public void setAudioTrack(int index) {
        if(!mediaplayer.isReleased() && mediaplayer.hasMedia()){
            mediaplayer.setAudioTrack(index);
        }
    }

    public void setAudioDigitalOutputEnabled(boolean enabled) {
        if (!mediaplayer.isReleased()) {
            mediaplayer.setAudioDigitalOutputEnabled(enabled);
        }
    }

    public long getAudioDelay() {
        if (mediaplayer.hasMedia() && !mediaplayer.isReleased()) return mediaplayer.getAudioDelay();
        return 0L;
    }

    public long getSpuDelay() {
        if (mediaplayer.hasMedia() && !mediaplayer.isReleased()) return mediaplayer.getSpuDelay();
        return 0L;
    }

    public float getRate(){
        if (mediaplayer.hasMedia() && !mediaplayer.isReleased() && playbackState != PlaybackState.STATE_STOPPED){
            return mediaplayer.getRate();
        }
        return 1.0f;
    }

    public void setSpuDelay(long delay) {
        mediaplayer.setSpuDelay(delay);
    }

    public void setVideoTrackEnabled(boolean enabled) {
        mediaplayer.setVideoTrackEnabled(enabled);
    }

    public void addSubtitleTrack(String path,boolean select) {
        mediaplayer.addSlave(IMedia.Slave.Type.Subtitle,path,select);
    }

    public void addSubtitleTrack(Uri uri,boolean select){
        mediaplayer.addSlave(IMedia.Slave.Type.Subtitle, uri, select);
    }

    public MediaPlayer.TrackDescription[] getSpuTracks(){
        MediaPlayer.TrackDescription[] td = new MediaPlayer.TrackDescription[]{};
        td = mediaplayer.getSpuTracks();
        return td;
    }

    public int getSpuTrack() {
        return mediaplayer.getSpuTrack();
    }

    public void setSpuTrack(int index) {
        mediaplayer.setSpuTrack(index);
    }

    public int getSpuTracksCount() {
        return mediaplayer.getSpuTracksCount();
    }

    public void setAudioDelay(long delay) {
        mediaplayer.setAudioDelay(delay);
    }

    public void setEqualizer(MediaPlayer.Equalizer equalizer) {
        mediaplayer.setEqualizer(equalizer);
    }

    @MainThread
    public void setVideoScale(float scale) {
        mediaplayer.setScale(scale);
    }

    public void setVideoAspectRatio(String aspect) {
        mediaplayer.setAspectRatio(aspect);
    }

    public void setRenderer(RendererItem renderer) {
        if (!mediaplayer.isReleased()) mediaplayer.setRenderer(renderer);
        hasRenderer = (renderer != null);
    }

    public void release(MediaPlayer player) {
        if(player == null) player = mediaplayer;
        player.setEventListener(null);
        if (isVideoPlaying()) player.getVLCVout().detachViews();
        releaseMedia();
        player.release();
        setPlaybackStopped();
    }

    private MediaPlayer newMediaPlayer(){
        mLibVLC = new LibVLC(context,null);
        MediaPlayer MP = new MediaPlayer(mLibVLC);
        MP.attachViews(this,null,true,false);
        MP.getVLCVout().addCallback(this);
        return MP;
    }

    public long getCurrentTime() {
        return mediaplayer.getTime();
    }

    public long getLength() {
        return mediaplayer.getLength();
    }

    public void setRate(float rate, boolean save) {
        if (mediaplayer.isReleased()) return;
        mediaplayer.setRate(rate);
        speed = rate;
    }

    /**
     * When changing current media, setPreviousStats is called to store statistics related to the
     * media. SetCurrentStats is called in the case where repeating is set to
     * PlaybackStateCompat.REPEAT_MODE_ONE, and the current media should not be released, as
     * it is still in use.
     */
    public void setCurrentStats() {
        IMedia media = mediaplayer.getMedia();
        if(media == null) return;
        previousMediaStats = media.getStats();
    }

    public void setPreviousStats() {
        IMedia media = mediaplayer.getMedia();
        if(media == null) return;
        previousMediaStats = media.getStats();
        media.release();
    }

    public boolean updateViewpoint(float yaw, float pitch,float roll,float fov,boolean absolute) {
        return mediaplayer.updateViewpoint(yaw, pitch, roll, fov, absolute);
    }

    public void navigate(int where) {
        mediaplayer.navigate(where);
    }

    public MediaPlayer.Chapter[] getChapters(int title){
        MediaPlayer.Chapter[] chapters = new MediaPlayer.Chapter[]{};
        if(!mediaplayer.isReleased()) chapters = mediaplayer.getChapters(title);
        return chapters;
    }

    public MediaPlayer.Title[] getTitles() {
        MediaPlayer.Title[] titles = new MediaPlayer.Title[]{};
        if (!mediaplayer.isReleased()) titles = mediaplayer.getTitles();
        return titles;
    }

    public int getChapterIdx() {
        return !mediaplayer.isReleased() ? mediaplayer.getChapter() : -1;
    }

    public void setChapterIdx(int chapter) {
        if (!mediaplayer.isReleased()) mediaplayer.setChapter(chapter);
    }

    public int getTitleIdx() {
        return (!mediaplayer.isReleased()) ? mediaplayer.getTitle() : -1;
    }

    public void setTitleIdx(int title) {
        if (!mediaplayer.isReleased())  mediaplayer.setTitle(title);
    }

    public int getVolume() {
        return (!mediaplayer.isReleased()) ? mediaplayer.getVolume() : 100;
    }

    public int setVolume(int volume) {
        return  (!mediaplayer.isReleased()) ? mediaplayer.setVolume(volume) : -1;
    }

    public IMediaList expand(){
        IMediaList iml = null;
        IMedia media = mediaplayer.getMedia();
        if(media != null){
            mediaplayer.setEventListener(null);
            iml = media.subItems();
            media.release();
            mediaplayer.setEventListener(this);
        }
        return iml;
    }

    private int playbackState = PlaybackState.STATE_NONE;
    private long lastTime = 0L;
    private float lastPosition = 0F;

    public void updateProgress(long newTime,long length) {
        lastTime = newTime;
        progress.time = newTime;
        progress.length = length;
    }

    private void setPlaybackStopped() {
        playbackState = PlaybackState.STATE_STOPPED;
        updateProgress(0L,0L);
        lastTime = 0L;
    }


    @Override
    public void onSurfacesCreated(IVLCVout ivlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout ivlcVout) {
        switchToVideo = false;
    }

    @Override
    public void onEvent(MediaPlayer.Event event) {
        switch (event.type) {
            case MediaPlayer.Event.Opening:
                playbackState = PlaybackState.STATE_NONE;
                break;
            case MediaPlayer.Event.Playing:
                playbackState = PlaybackState.STATE_PLAYING;
                break;
            case MediaPlayer.Event.Paused:
                playbackState = PlaybackState.STATE_PAUSED;
                break;
            case MediaPlayer.Event.EncounteredError:
                setPlaybackStopped();
                break;
            case MediaPlayer.Event.PausableChanged:
                pausable = event.getPausable();
                break;
            case MediaPlayer.Event.SeekableChanged:
                seekable = event.getSeekable();
                break;
            case MediaPlayer.Event.LengthChanged:
                progress.length = event.getLengthChanged();
                break;
            case MediaPlayer.Event.TimeChanged:
                long time = event.getTimeChanged();
                if (abs(time - lastTime) > 950L) {
                    progress.time = time;
                    lastTime = time;
                }
                break;
            case MediaPlayer.Event.PositionChanged:
                lastPosition = event.getPositionChanged();
                break;
        }
    }

    private static final int NO_LENGTH_PROGRESS_MAX = 1000;
    public class Progress{
        long time = 0L;
        long length = 0L;
    }
}
