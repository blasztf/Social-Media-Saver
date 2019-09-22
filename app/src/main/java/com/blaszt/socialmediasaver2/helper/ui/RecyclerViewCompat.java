package com.blaszt.socialmediasaver2.helper.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerViewCompat {

    public static class SimpleOnItemTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector mGestureDetector;
        private RecyclerView mRecyclerView;

        float touchX;
        int gridX;

        /**
         * Just override {@link RecyclerViewCompat.OnItemClickListener#onItemClick(View, int)} & {@link RecyclerViewCompat.OnItemClickListener#onItemLongClick(View, int)} to add any action.
         * @param recyclerView
         */
        public SimpleOnItemTouchListener(RecyclerView recyclerView) {
            // TODO Auto-generated constructor stub
            mRecyclerView = recyclerView;
            mGestureDetector = new GestureDetector(mRecyclerView.getContext(), new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    View itemView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());

                    if (itemView != null) {
                        onItemClick(itemView, mRecyclerView.getChildAdapterPosition(itemView));
                    }
                    return super.onSingleTapConfirmed(e);
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    // TODO Auto-generated method stub
                    View itemView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());

                    if (itemView != null) {
                        onItemLongClick(itemView, mRecyclerView.getChildAdapterPosition(itemView));
                    }
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    return onScrollOrFling(mRecyclerView, e1) || super.onFling(e1, e2, velocityX, velocityY);
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    return onScrollOrFling(mRecyclerView, e1) || super.onScroll(e1, e2, distanceX, distanceY);
                }

                private boolean onScrollOrFling(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                    touchX = rv.getX() + e.getRawX();
                    gridX = rv.getWidth() / 4;
                    return (touchX > gridX) && (touchX < (rv.getWidth() - gridX));
                }
            });
        }

        /**
         * Override this method to add any action when item clicked.
         * @param view item.
         * @param position item.
         */
        public void onItemClick(View view, int position) {}

        /**
         * Override this method to add any action when item long clicked.
         * @param view item.
         * @param position item.
         */
        public void onItemLongClick(View view, int position) {}

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
            return mGestureDetector.onTouchEvent(motionEvent);
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean b) {

        }
    }

    /**
     * Interface definition for a callback to be invoked when a child view on supplied <b>recyclerView</b> is clicked.
     * @author Navers
     *
     */
    public static class OnItemClickListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector mGestureDetector;
        private RecyclerView mRecyclerView;

        /**
         * Just override {@link RecyclerViewCompat.OnItemClickListener#onItemClick(View, int)} & {@link RecyclerViewCompat.OnItemClickListener#onItemLongClick(View, int)} to add any action.
         * @param recyclerView
         */
        public OnItemClickListener(RecyclerView recyclerView) {
            // TODO Auto-generated constructor stub
            mRecyclerView = recyclerView;
            mGestureDetector = new GestureDetector(mRecyclerView.getContext(), new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    // TODO Auto-generated method stub
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    // TODO Auto-generated method stub
                    View itemView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());

                    if (itemView != null) {
                        onItemLongClick(itemView, mRecyclerView.getChildAdapterPosition(itemView));
                    }
                }
            });
        }

        /**
         * Override this method to add any action when item clicked.
         * @param view item.
         * @param position item.
         */
        public void onItemClick(View view, int position) {}

        /**
         * Override this method to add any action when item long clicked.
         * @param view item.
         * @param position item.
         */
        public void onItemLongClick(View view, int position) {}

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            // TODO Auto-generated method stub
            View itemView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());

            if (itemView != null && mGestureDetector.onTouchEvent(e)) {
                onItemClick(itemView, mRecyclerView.getChildAdapterPosition(itemView));
            }

            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(
                boolean disallowIntercept) {
            // TODO Auto-generated method stub

        }


    }
}
