package com.yazidmf.streamingradioislami;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


public class AboutApp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void btn_main(View view) {
        Intent main = new Intent(AboutApp.this, MainActivity.class);
        startActivity(main);
    }
}
