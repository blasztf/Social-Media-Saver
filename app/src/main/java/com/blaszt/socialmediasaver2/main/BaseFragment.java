package com.blaszt.socialmediasaver2.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;

abstract class BaseFragment extends Fragment {

    public AppCompatActivity getSupportActivity() {
        return (AppCompatActivity) getActivity();
    }

    /**
     * Called when the activity has detected the user's press of the back key. The default implementation is handled by this activity.
     * @return true if handle the event. false otherwise.
     */
    public boolean onBackPressed() {
        return false;
    }

    void setBarDisplayHomeAsUp(boolean enable) {
        AppCompatActivity activity = getSupportActivity();
        if (activity.getActionBar() != null) {
            activity.getActionBar().setDisplayHomeAsUpEnabled(enable);
        }
        else if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(enable);
        }
    }

    void setNavigationVisibility(boolean visible) {
        ((MainActivity) getSupportActivity()).setNavigationVisibility(visible);
    }

    void setBarVisibility(boolean visible) {
        AppCompatActivity activity = getSupportActivity();
        if (activity.getActionBar() != null) {
            if (visible) {
                activity.getActionBar().show();
            }
            else {
                activity.getActionBar().hide();
            }
        }
        else if (activity.getSupportActionBar() != null) {
            if (visible) {
                activity.getSupportActionBar().show();
            }
            else {
                activity.getSupportActionBar().hide();
            }
        }
    }
}

