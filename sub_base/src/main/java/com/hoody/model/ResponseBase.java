package com.hoody.model;

import com.hoody.commonbase.net.RequestCallBack;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 网络请求响应数据处理基类
 */
public abstract class ResponseBase implements RequestCallBack {

    private final static String CODE_NAME = "code";
    private final static String DES_NAME = "des";

    public final static int CODE_OK = 0;
    public final static int CODE_NET_ERR = -1;
    public final static int CODE_PARSE_ERR = -2;

    @Override
    public final void onRequestFailed(int errCode, String errDes) {
        onRequestFail(-1, "网络请求异常");
    }

    @Override
    public final void onRequestSuccess(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            onRequestSuccess(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
            onRequestFail(-2, "数据解析异常");
        }
    }

    public abstract void onRequestSuccess(JSONObject result);

    public abstract void onRequestFail(int errCode, String errDes);
}
