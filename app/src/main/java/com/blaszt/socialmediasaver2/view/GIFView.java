package com.blaszt.socialmediasaver2.view;

import android.content.Context;
import android.graphics.Movie;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.widget.MediaController;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Deprecated
public class GIFView extends TextureView implements TextureView.SurfaceTextureListener, MediaController.MediaPlayerControl {
    private static final int STATE_MEDIA_PLAYER_READY = 1;
    private static final int STATE_MEDIA_PLAYER_RELEASED = 2;

    private Surface mSurface;
    private MediaPlayer mMediaPlayer;
    private Uri mUri;
    private Map<String, String> mHeaders;

    private MediaController mMediaController;

    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private MediaPlayer.OnCompletionListener mOnCompletionListener;

    private boolean mHasAudio;
    private boolean mIsVideo;

    private int mState;
    private int mBufferPercentage;

    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            MediaMetadataRetriever metadata = new MediaMetadataRetriever();
            mHasAudio = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) != null;
            mIsVideo = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) != null;

            if (mOnPreparedListener != null) mOnPreparedListener.onPrepared(mediaPlayer);
            mState = STATE_MEDIA_PLAYER_READY;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
            mBufferPercentage = percent;
            if (mOnBufferingUpdateListener != null) mOnBufferingUpdateListener.onBufferingUpdate(mediaPlayer, percent);
        }
    };

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.reset();
            if (mOnCompletionListener != null) mOnCompletionListener.onCompletion(mediaPlayer);
        }
    };

    public GIFView(Context context) {
        super(context);
        intialize();
    }

    public GIFView(Context context, AttributeSet attrs) {
        super(context, attrs);
        intialize();
    }

    public GIFView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        intialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GIFView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        intialize();
    }

    private void intialize() {
        setSurfaceTextureListener(this);
        setupMediaPlayer();
    }

    private void setupMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(onPreparedListener);
        mMediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    private boolean isMediaPlayerReady() {
        return mState == STATE_MEDIA_PLAYER_READY;
    }

    private boolean isMediaPlayerReleased() {
        return mState == STATE_MEDIA_PLAYER_RELEASED;
    }

    private void release() {
        if (mMediaPlayer != null) {
            if (isMediaPlayerReady()) {
                mMediaPlayer.stop();
                mMediaPlayer.setSurface(null);
            }
            mMediaPlayer.release();
            mState = STATE_MEDIA_PLAYER_RELEASED;
        }
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
    }

    private void openVideo() {
        if (mSurface != null && isMediaPlayerReady()) {
            try {
                mMediaPlayer.setDataSource(getContext(), mUri, mHeaders);
                mMediaPlayer.prepare();
                mMediaPlayer.setSurface(mSurface);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setVideoSource(String path) {
        if (path.matches("^(https?:)?//.+")) {
            setVideoSource(Uri.parse(path));
        } else {
            setVideoSource(Uri.fromFile(new File(path)));
        }
    }

    public void setVideoSource(Uri uri) {
        setVideoSource(uri, null);
    }

    public void setVideoSource(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    public void setOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener listener) {
        mOnBufferingUpdateListener = listener;
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        mSurface = new Surface(surfaceTexture);
        // try starting video
        openVideo();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    public void start() {
        if (mMediaPlayer != null && isMediaPlayerReady()) {
            mMediaPlayer.start();
        }
    }

    @Override
    public void pause() {
        if (canPause()) {
            mMediaPlayer.pause();
        }
    }

    @Override
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int msec) {
        if (isMediaPlayerReady()) {
            mMediaPlayer.seekTo(msec);
        }
    }

    @Override
    public boolean isPlaying() {
        return isMediaPlayerReady() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return mBufferPercentage;
    }

    @Override
    public boolean canPause() {
        return isMediaPlayerReady() && mMediaPlayer.isPlaying();
    }

    @Override
    public boolean canSeekBackward() {
        return mMediaPlayer.getCurrentPosition() > 0;
    }

    @Override
    public boolean canSeekForward() {
        return mMediaPlayer.getCurrentPosition() < mMediaPlayer.getDuration();
    }

    @Override
    public int getAudioSessionId() {
        return mMediaPlayer.getAudioSessionId();
    }


}
