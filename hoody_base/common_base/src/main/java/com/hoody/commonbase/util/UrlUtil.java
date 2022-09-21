package com.hoody.commonbase.util;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * url拼接工具类
 */
public final class UrlUtil {
    public enum Methord {
        GET, POST
    }

    public final static String PROTOCOL_HTTP = "http://";
    public final static String PROTOCOL_HTTPS = "https://";

    /**
     * @param protocol 协议：http {@link UrlUtil#PROTOCOL_HTTP } | https{@link UrlUtil#PROTOCOL_HTTPS }
     * @param host     要访问的服务器
     * @param path     所在服务器的路径
     * @param paramMap 需要携带的参数
     * @param method   请求方法 {@link Methord }
     * @return 最终的访问路径
     */
    public static String buildUrl(String protocol, String host, String path, Methord method, Map<String, String> paramMap) {
        HashMap<String, String> temParamMap = new HashMap<>();
        if (paramMap != null) {
            temParamMap.putAll(paramMap);
        }
        String result = "";
        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        StringBuilder temUrl = new StringBuilder(protocol);
        temUrl.append(host);
        if (!TextUtils.isEmpty(path)) {
            if (path.startsWith("/") && path.length() > 1) {
                path = path.substring(1);
            }
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            temUrl.append("/").append(path);
        }
        switch (method) {
            case GET:
                //get
                if (temParamMap.size() > 0) {
                    temUrl.append('?');
                }
                for (String key : temParamMap.keySet()) {
                    String value = temParamMap.get(key);
                    if (value == null) {
                        continue;
                    }
                    temUrl.append(key);
                    temUrl.append('=');
                    temUrl.append(value);
                    temUrl.append('&');
                }
                result = temUrl.toString();
                result = result.endsWith("&") ? result.substring(0, result.length() - 1) : result;
                break;
            case POST:
                //post
                result = temUrl.toString();
                break;
        }
        return result;
    }

    public static String getHttp_Url(String host, String path, Map<String, String> paramMap) {
        return buildUrl(PROTOCOL_HTTP, host, path, Methord.GET, paramMap);
    }

    public static String postHttp_Url(String host, String path) {
        return buildUrl(PROTOCOL_HTTP, host, path, Methord.POST, null);
    }

    public static String getHttpS_Url(String host, String path, Map<String, String> paramMap) {
        return buildUrl(PROTOCOL_HTTPS, host, path, Methord.GET, paramMap);
    }

    public static String postHttpS_Url(String host, String path) {
        return buildUrl(PROTOCOL_HTTPS, host, path, Methord.POST, null);
    }
}

