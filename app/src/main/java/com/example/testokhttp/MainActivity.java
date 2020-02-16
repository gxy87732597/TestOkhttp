package com.example.testokhttp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView tv;

    //创建OkHttpClient
    //OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    //OkHttpClient okHttpClient = new OkHttpClient();

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(30,TimeUnit.SECONDS)
            .readTimeout(30,TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.tv);

    }


    public void testGet(View view) {
        //安卓不能再主线程访问网络
        new Thread() {
            @Override
            public void run() {
                super.run();
                //创建一个Request，他里面包括的你要请求的网址等信息
                Request request = new Request.Builder().url("https://github.com/gxy87732597").build();

                try {
                    //调用newCall方法，传入刚刚创建的Request对象
                    // 然后调用execute方法来执行这个请求
                    Response response = okHttpClient.newCall(request).execute();
                    //通过调用response的body上的string方法可以得到流的字符串，不能放进主线程

                    if(response.isSuccessful()){
                        final String result = response.body().string();
                        showThreadInfo(result);
                    }else{
                        showThreadInfo("请求失败");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    private void showThreadInfo(String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //主线程
                tv.setText(result);
            }
        });
    }

    public void testAsyncGet(View view) {

        Request request = new Request.Builder().url("https://github.com/gxy87732597").build();

        //调用newCall方法，传入刚刚创建的Request对象
        // 然后调用enqueue入栈方法来执行这个请求
        this.okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                showThreadInfo(e.getLocalizedMessage());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if(response.isSuccessful()){
                    String result = response.body().string();

                    showThreadInfo(result);
                }else{
                    showThreadInfo("请求失败");
                }
            }
        });

    }

    //设置单个请求配置
    public void setOnRequestConfing(View view) {

        OkHttpClient copy = okHttpClient.newBuilder()
                .readTimeout(50, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder().url("https://github.com/gxy87732597").build();

        copy.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showThreadInfo(e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                showThreadInfo(result);
            }
        });

    }
}
