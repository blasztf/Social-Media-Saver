package com.blaszt.socialmediasaver2.helper.ui;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

public class LayoutManagerUtils {

    public static int getFirstVisiblePosition(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] firstCompletelyVisible = staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(new int[staggeredGridLayoutManager.getSpanCount()]);
            int firstVisiblePosition = firstCompletelyVisible[0];
            for (int i = 1, l = firstCompletelyVisible.length; i < l; i++) {
                firstVisiblePosition = Math.min(firstVisiblePosition, firstCompletelyVisible[i]);
            }
            return firstVisiblePosition;
        }
        else { // Don't know what to do.
            return -1;
        }
    }

    public static int getSpanCount(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof GridLayoutManager) {
            return ((GridLayoutManager) layoutManager).getSpanCount();
        }
        else if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }
        else { // Don't know what to do.
            return -1;
        }
    }
}
