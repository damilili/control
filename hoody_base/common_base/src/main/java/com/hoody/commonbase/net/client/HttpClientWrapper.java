package com.hoody.commonbase.net.client;


import com.hoody.commonbase.net.IHttpRequestClient;
import com.hoody.commonbase.net.ReqeuestParam;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;


public class HttpClientWrapper {
    private static IHttpRequestClient mHttpRequestProxy;

    private HttpClientWrapper() {
    }

    public static IHttpRequestClient getClient() {
        if (mHttpRequestProxy == null) {
            //这里配置真正的请求客户端
            IHttpRequestClient realClient = OkHttpProxy.getInstance();

            mHttpRequestProxy = (IHttpRequestClient) Proxy.newProxyInstance(HttpClientWrapper.class.getClassLoader(), new Class[]{IHttpRequestClient.class}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    preProcessGetRequest(method, args);
                    return method.invoke(realClient, args);
                }
            });
        }
        return mHttpRequestProxy;
    }

    private static void preProcessGetRequest(Method method, Object[] args) {
        if (method.getName().equals("get")) {
            //处理get请求
            Class<?>[] parameterTypes = method.getParameterTypes();
            Map<String, String> param = null;
            String url = null;
            int urlPos = 0;
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i].equals(String.class)) {
                    url = (String) args[i];
                    urlPos = i;
                }
                if (parameterTypes[i].equals(ReqeuestParam.class)) {
                    param = (ReqeuestParam) args[i];
                }
            }
            if (param != null && param.size() > 0) {
                StringBuilder urlBuilder = new StringBuilder(url);
                urlBuilder.append('?');
                for (String key : param.keySet()) {
                    String value = param.get(key);
                    if (value == null) {
                        continue;
                    }
                    urlBuilder.append(key);
                    urlBuilder.append('=');
                    urlBuilder.append(value);
                    urlBuilder.append('&');
                }
                url = urlBuilder.toString();
                url = url.endsWith("&") ? url.substring(0, url.length() - 1) : url;
                args[urlPos] = url;
            }
        }
    }

}
