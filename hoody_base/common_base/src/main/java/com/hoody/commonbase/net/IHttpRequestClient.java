package com.hoody.commonbase.net;

public interface IHttpRequestClient {
    RequestControl get(String url, ReqeuestHeader header, ReqeuestParam param, RequestCallBack requestCallBack);

    RequestControl post(String url, ReqeuestHeader header, ReqeuestParam params, RequestCallBack requestCallBack);

    RequestControl postForm(String url, ReqeuestHeader header, ReqeuestParam params, RequestCallBack requestCallBack);

    /**
     * 上传文件
     */
    RequestControl uploadFile(String url, String filePath, String fileName, ReqeuestParam params, FileAboutCallBack netRequestCallBack);

    /**
     * 下载文件
     */
    RequestControl downloadFile(String url, String filePath, String fileName, FileAboutCallBack callBack);
}
