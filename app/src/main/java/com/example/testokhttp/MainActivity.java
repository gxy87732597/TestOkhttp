package com.example.testokhttp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import domain.User;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;

public class MainActivity extends AppCompatActivity {

    private TextView tv;
    private ImageView iv;

    //创建OkHttpClient
    //OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
    //OkHttpClient okHttpClient = new OkHttpClient();

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.tv);
        iv = findViewById(R.id.iv);


    }

    //get同步请求
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

                    if (response.isSuccessful()) {
                        final String result = response.body().string();
                        showThreadInfo(result);
                    } else {
                        showThreadInfo("请求失败");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //显示文本
    private void showThreadInfo(String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //主线程
                tv.setText(result);
            }
        });
    }

    //get异步请求
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

                if (response.isSuccessful()) {
                    String result = response.body().string();

                    showThreadInfo(result);
                } else {
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

    //handler更新UI
    //这种使用handler会存在内存泄露的问题，但这里不考虑这些，只是测试
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            tv.setText(msg.obj.toString());
        }
    };

    public void testHandlerUpdataUI(View view) {

        Request request = new Request.Builder().url("https://github.com/gxy87732597").build();

        this.okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showThreadInfo(e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    handler.obtainMessage(0, response.body().string()).sendToTarget();
                } else {
                    showThreadInfo("请求失败");
                }
            }
        });
    }

    //runOnUiThread更新UI
    public void textRunOnUiThreadUpdataUI(View view) {
        Request request = new Request.Builder().url("https://github.com/gxy87732597").build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showThreadInfo(e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv.setText(result);
                        }
                    });
                }

            }
        });
    }

    //View.Post更新UI
    public void testViewPostUpdataUI(View view) {
        Request request = new Request.Builder().url("https://github.com/gxy87732597").build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showThreadInfo(e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                tv.post(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(result);
                    }
                });
            }
        });

    }

    //get传递参数
    public void testGetQueryParams(View view) {
        //将要传递的参数添加到Map中，比如：用户登录名，密码
        HashMap<String, Object> params = new HashMap<>();
        params.put("s", "Android");
        params.put("order", 0);

        //然后调用一个方法格式化参数
        //https://github.com/search?q=image+android
        //http://me.woblog.cn/?order=0&s=Android&
        String url = formatParams("http://me.woblog.cn/", params);
        Request request = new Request.Builder()
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
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

    //格式化参数
    private String formatParams(String url, HashMap<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        sb.append("?");
        for (Map.Entry<String, Object> p : params.entrySet()) {
            sb.append(p.getKey());
            sb.append("=");
            try {
                sb.append(URLEncoder.encode(p.getValue().toString(), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            }
            sb.append("&"); //这个&符号，最好在最后移除，因为末尾多一个
        }
        return sb.toString();

    }

    //post传递Markdown参数
    //这个类型要和服务端协定
    final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");

    public void testPostQueryMarkdownParams(View view) {

        //新建一段markdown文本
        String postBody = ""
                + "Releases\\n"
                + "--------\\n"
                + "\\n"
                + " * _1.0_ May 6, 2013\\n"
                + " * _1.1_ June 15, 2013\\n"
                + " * _1.2_ August 11, 2013\\n";
        //创建一个RequestBody
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_MARKDOWN, postBody);
        //通过post方法传入requestBody
        new Request.Builder().url("https://api.github.com/markdown/raw")
                .post(requestBody)
                .build();

    }

    //上传文件
    //根据File创建一个请求体
    public static final MediaType MEDIA_TYPE_IMAGE_JPG = MediaType.parse("image/jpeg: charset=utf-8");

    public void testUploadFile(View view) {
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_IMAGE_JPG, new File("/sdcard/a.jpg"));
        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(requestBody).build();
    }

    //提交表单
    public void testSubmitForm(View view) {
        FormBody formBody = new FormBody.Builder()
                .add("username", "slimee")
                .addEncoded("password", "123").build();

        Request request = new Request.Builder().url("https://api.github.com/markdown/raw")
                .post(formBody).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
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

    //上传文件
    public void testSubmitFileForm(View view) {
        //File类型的RequestBody
        RequestBody imageRequestBody = RequestBody.create(MediaType.parse("application/octet-stream"), new File("/sdcard/a.jpg"));

        //创建MultipartBody，通过addFormDataPart方法添加每一个表单
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("nickname", "测试")
                .addFormDataPart("image", "test.jpg", imageRequestBody)
                .build();

        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(multipartBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
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

    public void testPutPatchForm(View view) {
        Request getRequest = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .get()
                .build();

        Request postRequest = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, "这是一段Markdown代码"))
                .build();

        Request headRequest = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .head()
                .build();

        Request putRequest = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .put(RequestBody.create(MEDIA_TYPE_MARKDOWN, "这是一段Markdown代码"))
                .build();

        Request patchRequest = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .patch(RequestBody.create(MEDIA_TYPE_MARKDOWN, "这是一段Markdown代码"))
                .build();

        okHttpClient.newCall(getRequest).enqueue(new Callback() {
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

    //解析JSON
    public void testParseObject(View view) {
        Request request = new Request.Builder().url("https://api.github.com/users/lifengsofts").build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(e.getLocalizedMessage());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Reader reader = response.body().charStream();
                    Gson gson = new Gson();
                    User user = gson.fromJson(reader, User.class);
                    showThreadInfo(user.toString());

                } else {
                    showThreadInfo("请求失败");
                }

            }
        });
    }

    //只要一个线程去执行任务
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    //取消请求
    public void testAutoCancelRequest(View view) {

        new Thread() {

            @Override
            public void run() {
                super.run();

                Request request = new Request.Builder().url("http://httpbin.org/delay/2").build();

                final long startNanos = System.nanoTime();

                //监听请求的执行时间
                Call call = okHttpClient.newCall(request);

                //这里提交一个异步任务，他在一秒后取消
                executorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("TAG", String.format("%2f cancel call .%n",
                                (System.nanoTime() - startNanos) / 1e9f));

                        call.cancel();

                        Log.d("TAG", String.format("%2f cancel call .%n",
                                (System.nanoTime() - startNanos) / 1e9f));

                    }
                }, 1, TimeUnit.SECONDS);

                try {
                    Log.d("TAG", String.format("%2f Executing call .%n",
                            (System.nanoTime() - startNanos) / 1e9f));

                    Response response = call.execute();

                    Log.d("TAG", String.format("%2f call was expected to fail, but completed: %s%n",
                            (System.nanoTime() - startNanos) / 1e9f, response));

                } catch (IOException e) {
                    //如果一个线程正在写请求或读响应，他蹦出IOException异常
                    Log.d("TAG", String.format("%2f call failed as expected:%s%n",
                            (System.nanoTime() - startNanos) / 1e9f), e);
//                    e.printStackTrace();
                }
            }
        }.start();

    }

    //取消异步请求
    public void testAutoCancelAsyncRequest(View view) {

        Request request = new Request.Builder().url("http://httpbin.org/delay/2").build();

        final long startNanos = System.nanoTime();

        //监听请求的执行时间
        Call call = okHttpClient.newCall(request);

        //这里提交一个异步任务，他在一秒后取消
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                Log.d("TAG", String.format("%2f cancel call .%n",
                        (System.nanoTime() - startNanos) / 1e9f));

                call.cancel();

                Log.d("TAG", String.format("%2f cancel call .%n",
                        (System.nanoTime() - startNanos) / 1e9f));

            }
        }, 1, TimeUnit.SECONDS);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("TAG", "onFailure:"+e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TAG","onResponse:"+response.body().string());
            }
        });

    }

    //下载文件
    public void testDownloadFile(View view) {
        Request request = new Request.Builder().url("https://www.jianshu.com/u/9eaf6e16b822").build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showThreadInfo(e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    InputStream inputStream = response.body().byteStream();

                    FileOutputStream fileOutputStream = new FileOutputStream("/sdcard/a.html");

                    byte[] buffer=new byte[4096];
                    int len = -1;
                    while((len =inputStream.read(buffer)) != -1){
                        fileOutputStream.write(buffer,0,len);
                    }

                    fileOutputStream.close();
                    inputStream.close();
//                    Util.closeQuietly(fileOutputStream);
//                    Util.closeQuietly(inputStream);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        });


       /* 同步方式
       new Thread(){

            private FileOutputStream fileOutputStream;
            private InputStream inputStream;
            Request request = new Request.Builder()
                    .url("https://www.jianshu.com/u/9eaf6e16b822").build();

            @Override
            public void run() {
                super.run();

                try {
                    Response response = okHttpClient.newCall(request).execute();
                    byte[] buffer=new byte[4096];
                    int len = -1;
                    if(response.isSuccessful()){
                        inputStream = response.body().byteStream();
                        fileOutputStream = new FileOutputStream("/sdcard/a.html");
                        while((len= inputStream.read(buffer)) != -1){
                            fileOutputStream.write(buffer,0,len);
                        }

                        Util.closeQuietly(inputStream);
                        Util.closeQuietly(fileOutputStream);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally{
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
*/




    }

    //下载图片
    public void testShowNetworkIamgeFile(View view) {
        new Thread(){

            Request request = new Request.Builder()
                    .url("http://mat1.gtimg.com/www/images/qq2012/qqlogo_2x.png").build();

            @Override
            public void run() {
                super.run();

                try {
                    Response response = okHttpClient.newCall(request).execute();
                    byte[] buffer=new byte[4096];
                    int len = -1;
                    if(response.isSuccessful()){
                        InputStream inputStream = response.body().byteStream();

                        if((len=inputStream.read(buffer))!= -1){
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    iv.setImageBitmap(bitmap);
                                }
                            });

                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();

    }
}
