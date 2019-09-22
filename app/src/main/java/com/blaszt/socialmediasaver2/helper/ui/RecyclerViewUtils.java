package com.blaszt.socialmediasaver2.helper.ui;

import android.support.v7.widget.RecyclerView;

public class RecyclerViewUtils {
    public static <T extends RecyclerView.Adapter> T getAdapter(RecyclerView recyclerView) {
        return (T) recyclerView.getAdapter();
    }
}
