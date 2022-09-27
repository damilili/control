#include <ESP8266WebServer.h>
#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <string.h>
#include <FS.h>

#include <ArduinoJson.h>
#define Code_ok 0
#define Code_no_regist 1        //未注册
#define Code_token_err 2        //令牌错误
#define Code_wifi_no_set 3      //未设置wifi
#define Code_wifi_err 4         //WiFi连接失败
#define Code_pass_err 5         //密码错误
#define Code_muti_regist 6      //重复注册
#define Code_pass_format_err 7  //秘密格式错误

String myWifiName = "WifiControl";
String myWifiPass = "WifiControl";

String accessPass = "";
String token = "";
int32_t tokenExpir;

String wifiName = "";
String wifiPass = "";
bool SPIFFS_ok = false;

StaticJsonDocument<200> jsonBuffer;

ESP8266WebServer srv(80);


void savaProperties() {
  if (!SPIFFS_ok) {
    return;
  }
  if (SPIFFS.exists("/properties")) {
    SPIFFS.remove("/properties");
  }
  File propertiesFile = SPIFFS.open("/properties", "r");
  if (propertiesFile) {
    if (propertiesFile.available()) {
      String properties;
      jsonBuffer.clear();
      jsonBuffer["pass"] = accessPass;
      jsonBuffer["token"] = token;
      jsonBuffer["wifiName"] = wifiName;
      jsonBuffer["wifiPass"] = wifiPass;
      jsonBuffer["tokenExpir"] = tokenExpir;
      jsonBuffer["myWifiPass"] = myWifiPass;
      serializeJson(jsonBuffer, properties);
      propertiesFile.write(properties.c_str(), properties.length());
      propertiesFile.flush();
    }
    propertiesFile.close();
  } else {
    Serial.println("file open failed");
  }
}

void refreshToken() {
  token = "";
  for (char i = 0; i < 10; i++) {
    char randomChar = random(65, 90);
    token = token + randomChar;
  }
  tokenExpir = millis() + 1000 * 60 * 60;
  savaProperties();
}
/*
路径： device/modifywifipass
入参： token 必选
      pass
*/
void handleModifyWifiPass() {
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
  String cPass;

  if (srv.args() > 0) {
    deserializeJson(jsonBuffer, srv.arg(0));
    cPass.concat((const char*)jsonBuffer["pass"]);
    cToken.concat((const char*)jsonBuffer["token"]);
    jsonBuffer.clear();
  }
  if (token.length() > 0 && token != cToken) {
    jsonBuffer["code"] = Code_token_err;
    serializeJson(jsonBuffer, output);
    //token无效
    srv.send(200, "text/plain", output);
    return;
  }
  if (cPass.length() < 8 || cPass.length() > 20) {
    jsonBuffer["code"] = Code_pass_format_err;
  } else {
    myWifiPass = cPass;
    jsonBuffer["code"] = Code_ok;
  }
  serializeJson(jsonBuffer, output);
  srv.send(200, "text/plain", output);
  savaProperties();
  ESP.restart();
}
/**
路径： device/modifypass
入参： oldPass 旧密码
      newPass 新密码
返回： code
      token 令牌过期时会返回新的
*/
void handleModifyManagerPass() {
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
  if (srv.args() > 0) {
    deserializeJson(jsonBuffer, srv.arg(0));
    cOldPass.concat((const char*)jsonBuffer["oldPass"]);
    cNewPass.concat((const char*)jsonBuffer["newPass"]);
    jsonBuffer.clear();
  }
  if (cNewPass.length() < 8 || cNewPass.length() > 20) {
    jsonBuffer["code"] = Code_pass_format_err;
  } else if (accessPass.length() > 0 && accessPass.equals(cOldPass)) {
    refreshToken();
    accessPass = cNewPass;
    jsonBuffer["code"] = Code_ok;
    jsonBuffer["token"] = token;
  } else {
    jsonBuffer["code"] = Code_pass_err;
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
  if (srv.args() > 0) {
    deserializeJson(jsonBuffer, srv.arg(0));
    cPass.concat((const char*)jsonBuffer["pass"]);
    cWifiName.concat((const char*)jsonBuffer["wifiName"]);
    cToken.concat((const char*)jsonBuffer["token"]);
    jsonBuffer.clear();
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
       Serial.print("-");
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
  savaProperties();
  if (millis() - tokenExpir > 0) {
    jsonBuffer["token"] = token;
  }
  jsonBuffer["code"] = 0;
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
  if (srv.args() > 0) {
    deserializeJson(jsonBuffer, srv.arg(0));
    cPass.concat((const char*)jsonBuffer["pass"]);
    jsonBuffer.clear();
  }
  if (accessPass.length() > 0 && accessPass.equals(cPass)) {
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
  if (accessPass.length() > 0) {
    jsonBuffer["code"] = Code_muti_regist;
    serializeJson(jsonBuffer, output);
    srv.send(200, "text/plain", output);
    return;
  }
  String cPass;
  if (srv.args() > 0) {
    deserializeJson(jsonBuffer, srv.arg(0));
    cPass.concat((const char*)jsonBuffer["pass"]);
    jsonBuffer.clear();
  }

  if (cPass.length() < 8 || cPass.length() > 20) {
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
  if (srv.args() > 0) {
    deserializeJson(jsonBuffer, srv.arg(0));
    cToken.concat((const char*)jsonBuffer["token"]);
    jsonBuffer.clear();
  }
  if (token.length() > 0 && !token.equals(cToken)) {
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

void initProperties() {
  SPIFFS_ok = SPIFFS.begin();
  if (!SPIFFS_ok) {
    return;
  }
  if (SPIFFS.exists("/properties")) {
    File propertiesFile = SPIFFS.open("/properties", "r");
    if (propertiesFile) {
      if (propertiesFile.available()) {
        String properties = propertiesFile.readString();
        deserializeJson(jsonBuffer, properties);
        if (jsonBuffer.containsKey("pass")) {
          accessPass.concat(jsonBuffer["pass"], jsonBuffer["pass"].size());
        }
        if (jsonBuffer.containsKey("token")) {
          token.concat(jsonBuffer["token"], jsonBuffer["token"].size());
        }
        if (jsonBuffer.containsKey("tokenExpir")) {
          tokenExpir = jsonBuffer["tokenExpir"];
        }
        if (jsonBuffer.containsKey("wifiName")) {
          wifiName.concat(jsonBuffer["wifiName"], jsonBuffer["wifiName"].size());
        }
        if (jsonBuffer.containsKey("wifiPass")) {
          wifiPass.concat(jsonBuffer["wifiPass"], jsonBuffer["wifiPass"].size());
        }
        if (jsonBuffer.containsKey("myWifiPass") && jsonBuffer["myWifiPass"].size() > 0) {
          myWifiPass = "";
          myWifiPass.concat(jsonBuffer["myWifiPass"], jsonBuffer["myWifiPass"].size());
        }
        jsonBuffer.clear();
      }
      propertiesFile.close();
    } else {
      Serial.println("file open failed");
    }
  }
}

void setup() {
  Serial.begin(9600);

  randomSeed(analogRead(5));
  Serial.println();
  Serial.println();
  Serial.println();
  WiFi.mode(WIFI_AP_STA);

  initProperties();
  WiFi.softAP(myWifiName, myWifiPass);
  //  IrSender.begin();
  Serial.println("wifi ip:");
  Serial.println(WiFi.softAPIP());

  if (wifiName.length() > 0) {
    int32_t startMillis = millis();
    WiFi.begin(wifiName.c_str(), wifiPass.c_str());
    while (WiFi.status() != WL_CONNECTED) {
      delay(500);
      Serial.print(".");
      if (millis() - startMillis > 10000) {
        wifiName = "";
        wifiPass = "";
        break;
      }
    }
  }
  srv.keepAlive(true);
  // srv.on("/", handleRoot);
  // srv.on("/post/", handleData);
  // srv.on("/device/study", handleData);
  // srv.on("/device/getStudyResult", handleData);
  srv.on("/device/modifywifipass", handleModifyWifiPass);
  srv.on("/device/modifymanagerpass", handleModifyManagerPass);
  srv.on("/device/wifi", handleWifi);
  srv.on("/device/login", handleLogin);
  srv.on("/device/regist", handleRegist);
  srv.on("/device/status", handleStatus);
  srv.begin();
}

void loop() {
  srv.handleClient();
}