package com.example.testokhttp.interceptor;

import android.util.Log;

import com.example.testokhttp.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class loggingInterceptor implements Interceptor {

    private static final String TAG = "loggingInterceptor";

    //当网络请求时就会调用该方法
    @Override
    public Response intercept(Chain chain) throws IOException {
        //获取到原来的request，注意这是还没真正的请求服务端
        Request request = chain.request();
        //获取当前时间，并打印日志说要发起请求了
        //同时使用了headers打印请求头
        long t1 = System.nanoTime();
        //这里打印可以使用自己的日志框架
        Log.d(TAG, String.format("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()));

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        //还是根据是否是调试模式打印不同信息
        //如果是调试模式，可以打印更多的信息
        if (BuildConfig.DEBUG) {
            //调试模式，可以打印更多的信息
            //但是注意不能打印请求，不然后面就不会再获取了，因为流只能读取一次
            Log.d(TAG, String.format("Received response for %s in %.1fms Status %d %n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.code(),response.headers()));
        } else {
            //非调试模式，只打印时间，和请求头
            Log.d(TAG, String.format("Received response for %s in %.1fm  Status %d s%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.code(),response.headers()));
        }

        return response;
    }
}
