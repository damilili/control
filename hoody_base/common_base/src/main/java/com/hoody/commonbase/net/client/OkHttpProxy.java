package com.hoody.commonbase.net.client;

import android.text.TextUtils;
import android.util.Log;

import com.hoody.commonbase.net.IHttpRequestClient;
import com.hoody.commonbase.net.FileAboutCallBack;
import com.hoody.commonbase.net.RequestCallBack;
import com.hoody.commonbase.net.ProgressCallBack;
import com.hoody.commonbase.net.ReqeuestHeader;
import com.hoody.commonbase.net.ReqeuestParam;
import com.hoody.commonbase.net.RequestControl;
import com.hoody.commonbase.net.ok.ExMultipartBody;
import com.qiniu.android.dns.DnsManager;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.local.Resolver;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dns;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

class OkHttpProxy implements IHttpRequestClient {
    private static OkHttpProxy HttpProxy = null;
    private Dns dns = null;
    private OkHttpProxy() {
        try {
            IResolver[] resolvers = new IResolver[1];
            InetAddress byName = InetAddress.getByName("119.29.29.29");
            resolvers[0] = new Resolver(byName);
            final DnsManager dnsManager = new DnsManager(NetworkInfo.normal, resolvers);
            dns = new Dns() {
                @NotNull
                @Override
                public List<InetAddress> lookup(@NotNull String s) throws UnknownHostException {
                    List<InetAddress> lookup = new ArrayList<>();
                    try {
                        String[] ips = dnsManager.query(s);
                        for (String ip : ips) {  //将ip地址数组转换成所需要的对象列表
                            lookup.addAll(Arrays.asList(InetAddress.getAllByName(ip)));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (lookup.size() == 0) {
                        lookup = Dns.SYSTEM.lookup(s);
                    }
                    return lookup;
                }
            };
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (dns != null) {
            builder.dns(dns);
        }
        mClient = builder.build();
    }

    public static OkHttpProxy getInstance() {
        if (HttpProxy == null) {
            synchronized (OkHttpProxy.class) {
                if (HttpProxy == null) {
                    HttpProxy = new OkHttpProxy();
                }
            }
        }
        return HttpProxy;
    }

    private OkHttpClient mClient;

    @Override
    public RequestControl get(String url, ReqeuestHeader header, ReqeuestParam param, final RequestCallBack requestCallBack) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (header != null) {
            for (String key : header.keySet()) {
                String value = header.get(key);
                if (!TextUtils.isEmpty(value)) {
                    builder.addHeader(key, value);
                }
            }
        }
        OkHttpClient client = getOkHttpClient(header);
        final Call call = client.newCall(builder.build());
        RequestControl requestControl = new RequestControl() {
            @Override
            public void cancel() {
                if (!call.isCanceled()) {
                    call.cancel();
                }
            }
        };
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (requestCallBack != null) {
                    requestCallBack.onRequestFailed(-1, "网络请求异常");
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (requestCallBack != null) {
                    requestCallBack.onRequestSuccess(response.body().string());
                }
            }
        });
        return requestControl;
    }

    static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    @Override
    public RequestControl postForm(String url, ReqeuestHeader header, ReqeuestParam params, RequestCallBack requestCallBack) {
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null) {
            for (String key : params.keySet()) {
                String value = params.get(key);
                builder.add(key, value);
            }
        }

        RequestBody body = builder.build();
        return sendPost(url, header, requestCallBack, body);
    }

    @Override
    public RequestControl post(String url, ReqeuestHeader header, ReqeuestParam params, RequestCallBack requestCallBack) {
        JSONObject jsonObject = null;
        if (params != null) {
            jsonObject = new JSONObject(params);
        } else {
            jsonObject = new JSONObject();
        }
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        return sendPost(url, header, requestCallBack, body);
    }

    @NotNull
    private RequestControl sendPost(String url, Map<String, String> header, final RequestCallBack requestCallBack, RequestBody body) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(body);
        if (header != null) {
            for (String key : header.keySet()) {
                String value = header.get(key);
                if (!TextUtils.isEmpty(value)) {
                    builder.addHeader(key, value);
                }
            }
        }

        OkHttpClient client = getOkHttpClient(header);
        final Call call = client.newCall(builder.build());
        RequestControl requestControl = new RequestControl() {
            @Override
            public void cancel() {
                if (!call.isCanceled()) {
                    call.cancel();
                }
            }
        };
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (requestCallBack != null) {
                    requestCallBack.onRequestFailed(-1, "网络请求异常");
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (requestCallBack != null) {
                    requestCallBack.onRequestSuccess(response.body() == null ? null : response.body().string());
                }
            }
        });
        return requestControl;
    }

    private OkHttpClient getOkHttpClient(Map<String, String> header) {
        OkHttpClient client = mClient;
        if (header != null) {
            OkHttpClient.Builder builder1 = new OkHttpClient.Builder();
            int time;
            if (header.containsKey("readTimeout")) {
                String readTimeout = header.get("readTimeout");
                time = Integer.parseInt(readTimeout);
                if (time > 0) {
                    builder1.readTimeout(time, TimeUnit.SECONDS);
                }
                header.remove("readTimeout");
            }
            if (header.containsKey("connectTimeout")) {
                String connectTimeout = header.get("connectTimeout");
                time = Integer.parseInt(connectTimeout);
                if (time > 0) {
                    builder1.connectTimeout(time, TimeUnit.SECONDS);
                }
                header.remove("connectTimeout");
            }
            if (header.containsKey("writeTimeout")) {
                String writeTimeout = header.get("writeTimeout");
                time = Integer.parseInt(writeTimeout);
                if (time > 0) {
                    builder1.writeTimeout(time, TimeUnit.SECONDS);
                }
                header.remove("writeTimeout");
            }
            builder1.dns(dns);
            client = builder1.build();
        }
        return client;
    }

    @Override
    public RequestControl uploadFile(String url, final String filePath, final String fileName, ReqeuestParam params, final FileAboutCallBack fileAboutCallBack) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, RequestBody.create(MediaType.parse("multipart/form-data"), new File(filePath, fileName)));
        if (params != null) {
            for (String key : params.keySet()) {
                String value = params.get(key);
                builder.addFormDataPart(key, value);
            }
        }
        MultipartBody multipartBody = builder.build();
        RequestBody
                requestBody = new ExMultipartBody(multipartBody, new ProgressCallBack() {
            @Override
            public void onProgress(long totalLength, long currentLength) {
                if (fileAboutCallBack != null) {
                    fileAboutCallBack.onProgress(totalLength, currentLength);
                }
            }
        });
        Request request = new Request.Builder()
                .header("Authorization", "Client-ID " + UUID.randomUUID())
                .url(url)
                .post(requestBody)
                .build();
        final Call call = mClient.newCall(request);
        RequestControl requestControl = new RequestControl() {
            @Override
            public void cancel() {
                if (!call.isCanceled()) {
                    call.cancel();
                }
            }
        };
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (fileAboutCallBack != null) {
                    fileAboutCallBack.onRequestFailed(-1, "网络请求异常");
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (fileAboutCallBack != null) {

                    String string = null;
                    ResponseBody body = response.body();
                    if (body != null) {
                        string = body.string();
                    }
                    if (TextUtils.isEmpty(string)) {
                        fileAboutCallBack.onRequestSuccess(new File(filePath, fileName).getAbsolutePath());
                    } else {
                        fileAboutCallBack.onRequestSuccess(string);
                    }
                }
            }
        });
        return requestControl;
    }

    @Override
    public RequestControl downloadFile(String url, final String fileDir, final String fileName, final FileAboutCallBack callBack) {
        final File temFile = new File(fileDir, System.currentTimeMillis() + ".temp");
        if (!temFile.exists()) {
            try {
                temFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                if (callBack != null) {
                    callBack.onRequestFailed(-1, "网络请求异常");
                }
            }
        }
//        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
//            @Override
//            public Response intercept(Chain chain) throws IOException {
//                //拦截
//                Response originalResponse = chain.proceed(chain.request());
//                //包装响应体并返回
//                return originalResponse.newBuilder()
//                        .body(new ProgressResponseBody(originalResponse.body(), new UploadProgressListener() {
//                            @Override
//                            public void onProgress(long totalLength, long currentLength) {
//                                if (callBack != null) {
//                                    callBack.onProgress(totalLength, currentLength);
//                                }
//                            }
//                        }))
//                        .build();
//            }
//        }).build();
        Request request = new Request.Builder()
                .header("Authorization", "Client-ID " + UUID.randomUUID())
                .url(url)
                .build();
        final Call call = mClient.newCall(request);
        RequestControl requestControl = new RequestControl() {
            @Override
            public void cancel() {
                if (!call.isCanceled()) {
                    call.cancel();
                }
            }
        };
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (callBack != null) {
                    callBack.onRequestFailed(-1, "网络请求异常");
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    long total = response.body().contentLength();
                    if (true) {
                        Log.d("onResponseonResponse", "onResponse() called with: total = [" + total);
                    }
                    long current = 0;
                    is = response.body().byteStream();
                    fos = new FileOutputStream(temFile);
                    while ((len = is.read(buf)) != -1) {
                        current += len;
                        fos.write(buf, 0, len);
                        if (callBack != null) {
                            callBack.onProgress(total, current);
                        }
                    }
                    fos.flush();
                    temFile.renameTo(new File(fileDir, fileName));
                    if (callBack != null) {
                        callBack.onRequestSuccess(temFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    if (callBack != null) {
                        callBack.onRequestFailed(-1, "网络请求异常");
                    }
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return requestControl;
    }
}
