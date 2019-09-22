package com.blaszt.socialmediasaver2.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;
import android.widget.ViewSwitcher;

import com.blaszt.socialmediasaver2.R;

@SuppressLint("ClickableViewAccessibility")
public class GIFVideoView extends VideoView {
//    private static final int ID_GIF_VIDEOVIEW = 0xF1F0F1F4;

    private MediaController mMediaController;

    private MediaPlayer mMediaPlayer;
    private ImageSwitcher mSpeaker;

    private String mPath;
    private boolean mIsStopped = false, mIsMute = true, mIsInitialized = false, mHasAudio = true;

    public GIFVideoView(Context context) {
        super(context);
        main();
    }

    public GIFVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        main();
    }

    public GIFVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        main();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GIFVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        main();
    }

    private void main() {
//        setId(ID_GIF_VIDEOVIEW);
        setOnClickListener(null);
        setOnTouchListener(null);
    }

    @Override
    public void setVideoPath(String path) {
        // TODO Auto-generated method stub
        super.setVideoPath(path);
        mPath = path;
        mIsStopped = false;
        mIsInitialized = false;
        setOnPreparedListener(null);
        setupMetadata(path);
    }

    @Override
    public void stopPlayback() {
        // TODO Auto-generated method stub
        super.stopPlayback();
        mIsStopped = true;
        if (mMediaController != null) {
            mMediaController.hide();
        }
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub
        if (isStopped()) {
            setVideoPath(mPath);
        }
        super.start();
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        // TODO Auto-generated method stub
        final OnTouchListener newL = l;
        OnTouchListener listener = new OnTouchListener() {
            private static final long MAX_CLICK_DURATION = 300L;

            private long pressStartTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (newL != null) {
                    newL.onTouch(v, event);
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pressStartTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        long pressDuration = System.currentTimeMillis() - pressStartTime;
                        if (pressDuration < MAX_CLICK_DURATION) {
                            v.performClick();
                        }
                        break;
                }
                return true;
            }
        };
        super.setOnTouchListener(listener);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        // TODO Auto-generated method stub
        final OnClickListener newL = l;
        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mMediaPlayer != null) {
                    if (mMediaController != null && !mMediaController.isShowing()) {
                        setMute(false);
                    } else {
                        setMute(!isMute());
                    }
                }

                if (mMediaController != null) {
                    if (!mMediaController.isShowing()) {
                        mMediaController.show();
                    } else {
                        mMediaController.hide();
                    }
                }

                if (newL != null) {
                    newL.onClick(v);
                }
            }
        };
        super.setOnClickListener(listener);
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener l) {
        // TODO Auto-generated method stub
        final OnPreparedListener newL = l;
        OnPreparedListener listener = new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                // TODO Auto-generated method stub
                if (mMediaController == null) {
                    setController();
                }
                mMediaPlayer = mp;
                mMediaPlayer.setLooping(true);
                setMute(true);
                setupSpeakerView();

                if (newL != null) {
                    newL.onPrepared(mp);
                }

                mIsInitialized = true;
            }
        };
        super.setOnPreparedListener(listener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (mMediaPlayer != null) {
                    setMute(false);
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setController() {
        mMediaController = new MediaController(getContext());
        mMediaController.setAnchorView(this);
        setMediaController(mMediaController);
        mMediaController.hide();
    }

    public void setMute(boolean mute) {
        mute = mHasAudio && mute;
        if (mMediaPlayer != null) {
            float volume = mute ? 0f : 1f;
            mMediaPlayer.setVolume(volume, volume);
            if (mSpeaker != null) {
                mSpeaker.setImageResource(mute ? R.drawable.ic_volume_off : R.drawable.ic_volume_on);
            }
            mIsMute = mute;
        }
    }

    public boolean isMute() {
        return mIsMute;
    }

    public boolean isStopped() {
        return mIsStopped;
    }

    public boolean isInitialized() { return mIsInitialized; }

    private void setupMetadata(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        mHasAudio = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) != null;
        retriever.release();
    }

    @SuppressLint("InlinedApi")
    public void setupSpeakerView() {
//	    int speakerId = Math.abs("mSpeaker".hashCode());
        ViewGroup parent = (ViewGroup) getParent();

        if (mSpeaker != null) { //if (parent.findViewById(speakerId) != null) {
            mSpeaker.invalidate();
        } else {
            mSpeaker = new ImageSwitcher(getContext());
            mSpeaker.setInAnimation(getContext(), android.R.anim.fade_in);
            mSpeaker.setOutAnimation(getContext(), android.R.anim.fade_out);
            mSpeaker.setFactory(new ViewSwitcher.ViewFactory() {

                @Override
                public View makeView() {
                    // TODO Auto-generated method stub
                    ImageView speakerView = new ImageView(getContext());
                    speakerView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    return speakerView;
                }
            });
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 10;
            params.bottomMargin = 10;
            if (parent instanceof RelativeLayout) {
                params = new RelativeLayout.LayoutParams(params);
                ((RelativeLayout.LayoutParams) params).addRule(RelativeLayout.ALIGN_BOTTOM, getId());
                ((RelativeLayout.LayoutParams) params).addRule(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 ? RelativeLayout.ALIGN_LEFT : RelativeLayout.ALIGN_START, getId());
            } else if (parent instanceof LinearLayout) {
                params = new LinearLayout.LayoutParams(params);
                ((LinearLayout.LayoutParams) params).gravity = Gravity.BOTTOM | Gravity.START;
            } else if (parent instanceof FrameLayout) {
                params = new FrameLayout.LayoutParams(params);
                ((FrameLayout.LayoutParams) params).gravity = Gravity.BOTTOM | Gravity.START;
            } else if (parent instanceof ConstraintLayout) {
                params = new ConstraintLayout.LayoutParams(params);
                ((ConstraintLayout.LayoutParams) params).bottomToBottom = getId();
                ((ConstraintLayout.LayoutParams) params).startToStart = getId();
            }
            mSpeaker.setLayoutParams(params);
            parent.addView(mSpeaker);
        }
    }

}
