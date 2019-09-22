package com.blaszt.socialmediasaver2.helper.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import java.lang.ref.WeakReference;

public class ZoomUtils { // enlarge image code by google sample. make as utils so it can be easy to use.
    private static final int TAG_IS_ZOOM = 0x0912F23A;

    // Static instance
    private static ZoomUtils mInstance;

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration;

//    private WeakReference<View> mBackground;

    private WeakReference<Activity> mActivity;

    public interface OnZoomFinishedListener {
        void onZoomFinished();
    }

    public static synchronized ZoomUtils getInstance(Activity activity) {
        if (mInstance == null || mInstance.getActivity() == null) {
            mInstance = new ZoomUtils(activity);
        }

        return mInstance;
    }

    private ZoomUtils(Activity activity) {
        setActivity(activity);

        mShortAnimationDuration = getActivity().getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

//    private View getBackground(ViewGroup rootView) {
//        View background;
//        int backgroundId = Math.abs("mBackground".hashCode());
//        if (mBackground == null || mBackground.get() == null) {
//            background = rootView.findViewById(backgroundId);
//
//            if (background == null) {
//                background = new View(getActivity());
//                background.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//                background.setBackgroundColor(0xFF000000);
//                background.setId(backgroundId);
//                background.setAlpha(0f);
//                background.setVisibility(View.GONE);
//            }
//
//            mBackground = new WeakReference<>(background);
//        }
//
//        return mBackground.get();
//    }

    private void setActivity(Activity activity) {
        mActivity = new WeakReference<>(activity);
    }

    private Activity getActivity() {
        return mActivity.get();
    }

    public boolean isZoomed(View view) {
        Object isZoomed = view.getTag(TAG_IS_ZOOM);
        return isZoomed != null ? (Boolean) isZoomed : false;
    }

    /**
     * Zoom in thumbView to expandedView.
     *
     * @param thumbView    thumbnail view (usually the "small" one).
     * @param expandedView expanded view (usually the "big" one).
     */
    public void zoomIn(View thumbView, final View expandedView, final OnZoomFinishedListener listener) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        final OnZoomFinishedListener onZoomFinishedListener = new OnZoomFinishedListener() {
            @Override
            public void onZoomFinished() {
                mCurrentAnimator = null;
                expandedView.setTag(TAG_IS_ZOOM, true);
                if (listener != null) listener.onZoomFinished();
            }
        };
        expandedView.setVisibility(View.VISIBLE);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        getActivity().findViewById(android.R.id.content)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedView.setPivotX(0f);
        expandedView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedView, View.SCALE_Y,
                        startScale, 1f));
//                .with(ObjectAnimator.ofFloat(expandedView, View.ALPHA,
//                        0f, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onZoomFinishedListener.onZoomFinished();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onZoomFinishedListener.onZoomFinished();
            }
        });
        set.start();
        mCurrentAnimator = set;
    }

    /**
     * Zoom out expandedView to thumbView.
     *
     * @param thumbView    thumbnail view (usually the "small" one).
     * @param expandedView expanded view (usually the "big" one).
     */
    public void zoomOut(final View thumbView, final View expandedView, final OnZoomFinishedListener listener) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        final OnZoomFinishedListener onZoomFinishedListener = new OnZoomFinishedListener() {
            @Override
            public void onZoomFinished() {
                thumbView.setAlpha(1f);
                expandedView.setVisibility(View.GONE);
                mCurrentAnimator = null;

                expandedView.setTag(TAG_IS_ZOOM, false);
                if (listener != null) listener.onZoomFinished();
            }
        };

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        getActivity().findViewById(android.R.id.content)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator
                .ofFloat(expandedView, View.X, startBounds.left))
                .with(ObjectAnimator
                        .ofFloat(expandedView,
                                View.Y, startBounds.top))
                .with(ObjectAnimator
                        .ofFloat(expandedView,
                                View.SCALE_X, startScale))
                .with(ObjectAnimator
                        .ofFloat(expandedView,
                                View.SCALE_Y, startScale));
//                .with(ObjectAnimator
//                        .ofFloat(expandedView,
//                                View.ALPHA, 0f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onZoomFinishedListener.onZoomFinished();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onZoomFinishedListener.onZoomFinished();
            }
        });
        set.start();
        mCurrentAnimator = set;
    }

}