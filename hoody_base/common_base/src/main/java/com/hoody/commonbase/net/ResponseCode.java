package com.hoody.commonbase.net;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cdm on 2021/11/26.
 */
public class ResponseCode {
    public static Map<String, String> CodeDes = new HashMap<>();

    static {
        CodeDes.put("200", "成功");
        CodeDes.put("201", "成功，紧接POST命令");
        CodeDes.put("202", "成功,已接受用于处理，但处理尚未完成");
        CodeDes.put("203", "成功，返回的信息只是一部分");
        CodeDes.put("204", "成功,不存在要回送的信息");

        CodeDes.put("301", "请求的数据具有新的位置");
        CodeDes.put("302", "请求的数据临时具有不同 URI");
        CodeDes.put("303", "可在另一 URI 下找到对请求的响应");
        CodeDes.put("304", "未按预期修改文档");
        CodeDes.put("305", "必须通过位置字段中提供的代理来访问请求的资源");
        CodeDes.put("306", "不再使用；保留此代码以便将来使用");

        CodeDes.put("400", "请求中有语法问题，或不能满足请求");
        CodeDes.put("401", "未授权客户机访问数据");
        CodeDes.put("402", "表示计费系统已有效");
        CodeDes.put("403", "禁止访问");
        CodeDes.put("404", "服务器找不到给定的资源");
        CodeDes.put("407", "客户机首先必须使用代理认证自身");
        CodeDes.put("415", "不支持请求实体的格式");

        CodeDes.put("500", "因为意外情况，服务器不能完成请求");
        CodeDes.put("501", "服务器不支持请求的工具");
        CodeDes.put("502", "错误网关");
        CodeDes.put("503", "无法获得服务");
    }
}
