package com.blaszt.socialmediasaver2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestAct extends Activity {
    private EditText editTextU;
    private Button buttonU;
    private TextView textViewU;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_test);
        editTextU = findViewById(R.id.editTextU);
        buttonU = findViewById(R.id.buttonU);
        textViewU = findViewById(R.id.textViewU);

        buttonU.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                String url = editTextU.getText().toString();
//                String response = Responder.with(null).getResponse(url);
                editTextU.setText("Loading...");
            }
        });

    }
}
