package com.hoody.commonbase.net.client;

import android.os.Handler;
import android.os.Looper;

import com.hoody.commonbase.net.FileAboutCallBack;
import com.hoody.commonbase.net.IHttpRequestClient;
import com.hoody.commonbase.net.RequestCallBack;
import com.hoody.commonbase.net.ReqeuestHeader;
import com.hoody.commonbase.net.ReqeuestParam;
import com.hoody.commonbase.net.RequestControl;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by cdm on 2021/11/25.
 */
class HttpClientProxy implements IHttpRequestClient {
    private HttpClient mHttpCient = new DefaultHttpClient();
    private ScheduledExecutorService mScheduledExecutorService = Executors.newScheduledThreadPool(4);
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public RequestControl get(String url, ReqeuestHeader header, ReqeuestParam param, RequestCallBack requestCallBack) {
        Callable<Runnable> callable = new Callable<Runnable>() {
            @Override
            public Runnable call() throws Exception {
                HttpGet httpGet = new HttpGet(url);
                try {
                    for (Map.Entry<String, String> stringStringEntry : header.entrySet()) {
                        httpGet.addHeader(stringStringEntry.getKey(), stringStringEntry.getValue());
                    }
                    HttpResponse httpResponse = mHttpCient.execute(httpGet);
                    String response = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                                requestCallBack.onRequestSuccess(response);
                            } else {
                                requestCallBack.onRequestFailed(-1, "网络请求异常");
                            }
                        }
                    };
                    mHandler.post(runnable);
                    return runnable;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        Future<Runnable> submit = mScheduledExecutorService.submit(callable);
        return new RequestControl() {
            @Override
            public void cancel() {
                if (submit.isCancelled() || submit.isDone()) {
                    return;
                }
                submit.cancel(true);
                try {
                    Runnable runnable = submit.get();
                    if (runnable != null && mHandler.hasCallbacks(runnable)) {
                        mHandler.removeCallbacks(runnable);
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public RequestControl post(String url, ReqeuestHeader header, ReqeuestParam params, RequestCallBack requestCallBack) {
        return null;
    }

    @Override
    public RequestControl postForm(String url, ReqeuestHeader header, ReqeuestParam params, RequestCallBack requestCallBack) {
        return null;
    }

    @Override
    public RequestControl uploadFile(String url, String filePath, String fileName, ReqeuestParam params, FileAboutCallBack netRequestCallBack) {
        return null;
    }

    @Override
    public RequestControl downloadFile(String url, String filePath, String fileName, FileAboutCallBack callBack) {
        return null;
    }
}
