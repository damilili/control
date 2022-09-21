package com.hoody.commonbase.net;

/**
 */
public interface RequestCallBack {

    /**
     * 网络不可用、或者请求失败、或者结果解析异常
     * @param errCode          错误码
     * @param errDes 返回错误描述，此方法由UI线程调用
     */
    void onRequestFailed(int errCode, String errDes);


    /**
     * 成功得到请求结果（结果解析成功，包括有预期结果，和无预期结果），此方法由UI线程调用
     */
    void onRequestSuccess(String result);
}
