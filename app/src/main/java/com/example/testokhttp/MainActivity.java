package com.example.testokhttp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    private TextView tv;

    //创建OkHttpClient
    //OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    OkHttpClient okHttpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.tv);

    }

    public void testGet(View view) {


    }
}
