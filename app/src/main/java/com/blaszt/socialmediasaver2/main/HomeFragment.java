package com.blaszt.socialmediasaver2.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.blaszt.socialmediasaver2.R;
import com.blaszt.socialmediasaver2.TestAct;
import com.blaszt.socialmediasaver2.services.URLHandler;
import com.blaszt.socialmediasaver2.services.URLService;

public final class HomeFragment extends BaseFragment implements View.OnClickListener, View.OnLongClickListener {
    private TextInputEditText urltext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupView();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.download_media:
                downloadMedia();
                break;
            case R.id.find_media:
                startMediaFinder();
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case  R.id.find_media:
                startTest();
                return true;
                default:
                    return false;
        }
    }

    private void setupView() {
        View view = getView();
        if (view != null) {
            urltext = view.findViewById(R.id.url_text);
            Button downloadmedia = view.findViewById(R.id.download_media);
            FloatingActionButton findmedia = view.findViewById(R.id.find_media);

            downloadmedia.setOnClickListener(this);
            findmedia.setOnClickListener(this);
            findmedia.setOnLongClickListener(this);
        }
    }

    private void downloadMedia() {
        Editable text = urltext.getText();
        if (text != null) {
            String url = text.toString();
            Intent intent = new Intent(getSupportActivity(), URLHandler.class);
            intent.setAction(URLHandler.ACTION_HANDLE_URL);
            intent.putExtra(URLHandler.EXTRA_URL, url);
            getSupportActivity().startService(intent);
        }
    }

    private void startMediaFinder() {
        Intent intent = new Intent(getSupportActivity(), URLService.class);
        getSupportActivity().startService(intent);
    }

    private void startTest() {
        Intent intent = new Intent(getSupportActivity(), TestAct.class);
        getSupportActivity().startActivity(intent);
    }

}
