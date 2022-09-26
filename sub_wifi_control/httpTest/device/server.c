#include <ESP8266WebServer.h>
#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <string.h>

#include <ArduinoJson.h>
#define Code_ok 0
#define Code_no_regist 1        //未注册
#define Code_token_err 2        //令牌错误
#define Code_wifi_no_set 3      //未设置wifi
#define Code_wifi_err 4         //WiFi连接失败
#define Code_pass_err 5         //密码错误
#define Code_muti_regist 6      //重复注册
#define Code_pass_format_err 7  //秘密格式错误

String myWifiName = "ESP8266";
String myWifiPass = "ESP8266";

String accessPass = "";
String token;

int tokenExpir;
String wifiName;
String wifiPass;

StaticJsonDocument<200> jsonBuffer;

ESP8266WebServer srv(80);


void refreshToken() {
  token = "";
  for (char i = 0; i < 10; i++) {
    char randomChar = random(65, 90);
    token = token + randomChar;
  }
  tokenExpir = millis() + 1000 * 60 * 60;
}

/**
路径： device/modifypass
入参： oldPass 旧密码
      newPass 新密码
返回： code
      token 令牌过期时会返回新的
*/
void handleModifyPass() {
  jsonBuffer.clear();
  String output;
  if (accessPass == "") {
    //没有设置管理员密码
    jsonBuffer["code"] = Code_no_regist;
    serializeJson(jsonBuffer, output);
    srv.send(200, "text/plain", output);
    return;
  }
  String cOldPass;
  String cNewPass;
  for (int i = 0; i < srv.args(); i++) {
    if (srv.argName(i).equals("oldPass")) {
      cOldPass = srv.arg(i);
    } else if (srv.argName(i).equals("newPass")) {
      cNewPass = srv.arg(i);
    }
  }
  if (accessPass.equals(cOldPass)) {
    accessPass = cNewPass;
    jsonBuffer["code"] = Code_ok;
  } else {
    jsonBuffer["code"] = Code_pass_err;
  }
  if (millis() - tokenExpir > 0) {
    jsonBuffer["token"] = token;
  }
  serializeJson(jsonBuffer, output);
  srv.send(200, "text/plain", output);
}

/**
路径： device/wifi
入参： wifiName wifi名称
      pass   wifi的密码
      token  令牌
返回： code
      token 令牌过期时会返回新的
*/
void handleWifi() {
  jsonBuffer.clear();
  String output;
  if (accessPass == "") {
    //没有设置管理员密码
    jsonBuffer["code"] = Code_no_regist;
    serializeJson(jsonBuffer, output);
    srv.send(200, "text/plain", output);
    return;
  }

  String cToken;
  String cWifiName;
  String cPass;
  for (int i = 0; i < srv.args(); i++) {
    if (srv.argName(i).equals("token")) {
      cToken = srv.arg(i);
    } else if (srv.argName(i).equals("wifiName")) {
      cWifiName = srv.arg(i);
    } else if (srv.argName(i).equals("pass")) {
      cPass = srv.arg(i);
    }
  }
  if (token.length() > 0 && token != cToken) {
    jsonBuffer["code"] = Code_token_err;
    serializeJson(jsonBuffer, output);
    //token无效
    srv.send(200, "text/plain", output);
    return;
  }
  // 连接wifi
  WiFi.begin(cWifiName.c_str(), cPass.c_str());
  int32_t startMillis = millis();
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
    if (millis() - startMillis > 10000) {
      jsonBuffer["code"] = Code_wifi_err;
      serializeJson(jsonBuffer, output);
      // 连接wifi超时
      srv.send(200, "text/plain", output);
      return;
    }
  }
  // 成功连接wifi
  wifiName = cWifiName;
  wifiPass = cPass;
  if (millis() - tokenExpir > 0) {
    jsonBuffer["token"] = token;
  }
  jsonBuffer["code"] = Code_ok;
  jsonBuffer["wifiIp"] = WiFi.localIP();
  serializeJson(jsonBuffer, output);
  srv.send(200, "text/plain", output);
}
/*
路径： device/login
入参： pass
返回： code
      token 成功时传递
*/
void handleLogin() {
  jsonBuffer.clear();
  String output;
  if (accessPass == "") {
    //没有设置管理员密码
    jsonBuffer["code"] = Code_no_regist;
    serializeJson(jsonBuffer, output);
    srv.send(200, "text/plain", output);
    return;
  }

  String cPass;
  for (int i = 0; i < srv.args(); i++) {
    if (srv.argName(i).equals("pass")) {
      cPass = srv.arg(i);
      break;
    }
  }
  if (accessPass.equals(cPass)) {
    refreshToken();
    jsonBuffer["code"] = Code_ok;
    jsonBuffer["token"] = token;
  } else {
    jsonBuffer["code"] = Code_pass_err;
  }
  serializeJson(jsonBuffer, output);
  srv.send(200, "text/plain", output);
}

/*
路径： device/regist 只能调用一次
入参： pass 管理员密码
返回： code
      token
*/
void handleRegist() {
  jsonBuffer.clear();
  String output;
  if (accessPass != "") {
    jsonBuffer["code"] = Code_muti_regist;
    serializeJson(jsonBuffer, output);
    srv.send(200, "text/plain", output);
    return;
  }
  String cPass;
  for (int i = 0; i < srv.args(); i++) {
    if (srv.argName(i).equals("pass")) {
      cPass = srv.arg(i);
      break;
    }
  }
  if (cPass.length() < 8) {
    jsonBuffer["code"] = Code_pass_format_err;
  } else {
    accessPass = cPass;
    refreshToken();
    jsonBuffer["code"] = 0;
    jsonBuffer["token"] = token;
  }
  serializeJson(jsonBuffer, output);
  srv.send(200, "text/plain", output);
}

int checkStatus() {
  if (accessPass == "") {
    return Code_no_regist;
  }
  String cToken;
  for (int i = 0; i < srv.args(); i++) {
    if (srv.argName(i).equals("token")) {
      cToken = srv.arg(i);
      break;
    }
  }
  if (token.length() > 0 && token != cToken) {
    return Code_token_err;
  }
  if (wifiName == "") {
    return Code_wifi_no_set;
  }
  return Code_ok;
}
/*
路径： device/status
入参： token 可选

*/
void handleStatus() {
  jsonBuffer.clear();
  String output;
  int code = checkStatus();
  jsonBuffer["code"] = code;
  serializeJson(jsonBuffer, output);
  srv.send(200, "text/plain", output);
}

void setup() {
  Serial.begin(9600);
  randomSeed(analogRead(5));

  Serial.println();
  Serial.println();
  Serial.println();
  WiFi.mode(WIFI_AP_STA);
  WiFi.softAP(myWifiName, myWifiPass);
  //  IrSender.begin();
  Serial.println("wifi ip:");
  Serial.println(WiFi.softAPIP());

  // srv.on("/", handleRoot);
  // srv.on("/post/", handleData);
  // srv.on("/device/study", handleData);
  // srv.on("/device/getStudyResult", handleData);
  srv.on("/device/modifypass", handleModifyPass);
  srv.on("/device/wifi", handleWifi);
  srv.on("/device/login", handleLogin);
  srv.on("/device/regist", handleRegist);
  srv.on("/device/status", handleStatus);
  srv.begin();
}

void loop() {
  srv.handleClient();
}