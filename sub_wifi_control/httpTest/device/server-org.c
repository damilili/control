#include <ESP8266WebServer.h>
#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <string.h>

String myWifiName="ESP8266";
String passWord="5b8a085c";
String token;
int tokenUseTime;

String wifiName;
String wifiPass;
int myStatus = 0;

void saveProperties() {
}
void initProperties() {
  
}

String randomString() {
  String abc;
  return abc;
}
ESP8266WebServer srv(80);
#define Roothtml "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>设置</title></head><style>body label,button{font-size:16px;font-weight:bold;position:center;display:block;margin-left:auto;margin-right:auto;margin-top:auto;width:50%}</style><script>function send(){var ssid=document.getElementById(\"ssid\").value;var password=document.getElementById(\"password\").value;var addr=document.getElementById(\"addr\").value;if(ssid===\"\"||password===\"\"||addr===\"\"){alert(\"请输入完整信息\");return}if(addr.startsWith(\"http://\")){}else{addr=\"http://\"+addr}if(addr.endsWith(\"/device\")){}else{addr=addr+\"/device\"}var xmlhttp=new XMLHttpRequest();xmlhttp.onreadystatechange=function(){if(xmlhttp.readyState===4&&xmlhttp.status===200){document.getElementById(\"result\").innerHTML=xmlhttp.responseText}};xmlhttp.open(\"POST\",\"/post/\",true);xmlhttp.setRequestHeader(\"Content-type\",\"application/x-www-form-urlencoded\");xmlhttp.send(\"ssid=\"+ssid+\"&password=\"+password+\"&addr=\"+addr)}</script><body><label>WiFi名称<input id=\"ssid\"/></label><br><label>WiFi密码<input id=\"password\"/></label><br><label>服务器地址<input id=\"addr\"/></label><br><button onclick=\"send()\">确定</button><br><div id=\"result\"></div></body></html>"
String cloudip;
int status = 0;
/*
  status annotations
  0 -> I don't know WiFi SSID , password and server IP
  1 -> ordinary working
*/
void handleRoot()
{
  srv.send(200, "text/html", Roothtml);
}
void handleData()
{
  Serial.println("recv data");
  if (srv.method() != HTTP_POST)
  {
    srv.send(405, "text/plain", "Not Allowed");
  }
  else
  {
    String ssids = srv.arg(0);
    String pwds = srv.arg(1);
    cloudip = srv.arg(2);
    int32_t startMillis = millis();
    bool flag = 0; // This is a timed out flag
    WiFi.begin(ssids.c_str(), pwds.c_str());
    while (WiFi.status() != WL_CONNECTED)
    {
      delay(500);
      Serial.print(".");
      if (millis() - startMillis > 10000)
      {
        flag = 1;
        break;
      }
    }
    if (flag)
    {
      srv.send(200, "text/plain", "WiFi connection failed");
      return;
    }
    if (WiFi.status() == WL_CONNECTED)
    {
      WiFiClient cli;
      HTTPClient http;
      http.begin(cli, cloudip);
      http.addHeader("Content-Type", "application/json");
      // 这里我以发送mac地址为例，测试HTTP连接是否正常，可按需更改

      int httpCode = http.POST("{\"mac\":\"" + WiFi.macAddress() + "\"}");
      if (httpCode > 0)
      {
        srv.send(200, "text/plain", "ALL SUCCESS");
        status = 1;
      }
      else
      {
        srv.send(200, "text/plain", "WiFi connection succeed, but connection to cloud failed");
      }
    }
    else
    {
      srv.send(200, "text/plain", "WiFi connection failed");
    }
  }
}

void handleStatus(){
myStatus
}
void setup() {
  Serial.begin(9600);
  Serial.println();
  Serial.println();
  Serial.println();
  WiFi.mode(WIFI_AP_STA);
  WiFi.softAP(myWifiName, passWord);
  //  IrSender.begin();
  Serial.println("");
  srv.on("/", handleRoot);
  srv.on("/post/", handleData);
  srv.on("/device/study", handleData);
  srv.on("/device/getStudyResult", handleData);
  srv.on("/device/modifypass", handleData);
  srv.on("/device/wifi", handleData);
  srv.on("/device/login", handleData);
  srv.on("/device/setpass", handleData);
  srv.on("/device/status", handleStatus);
  srv.begin();
}

void loop() {
 srv.handleClient();
  switch (status)
  {
    case 0:
      srv.handleClient();
      break;
    case 1:
      // 这里写连上了网络以后的操作，比如与服务器通信

      break;
  }
}