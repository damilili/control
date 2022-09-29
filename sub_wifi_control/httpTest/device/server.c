#include <ESP8266WebServer.h>
#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <string.h>
#include <FS.h>

#include <ArduinoJson.h>
#define Code_ok 0
#define Code_no_regist 1          //未注册
#define Code_token_err 2          //令牌错误
#define Code_wifi_no_set 3        //未设置wifi
#define Code_wifi_err 4           //WiFi连接失败
#define Code_pass_err 5           //密码错误
#define Code_muti_regist 6        //重复注册
#define Code_pass_format_err 7    //秘密格式错误
#define Code_study_key_err 8      //学习的按键id不能是空
#define Code_being_study_other 9  //正在学习其他按键中
#define Code_study_outtime 10     //学习超时

String myWifiName = "WifiControl";
String myWifiPass = "WifiControl";

String accessPass = "";
String token = "";
int32_t tokenStart;

String wifiName = "";
String wifiPass = "";
bool SPIFFS_ok = false;

String studyKeyId = "";
int32_t studyTime = 0;  // 开始学习时间

StaticJsonDocument<200> jsonBuffer;

ESP8266WebServer srv(80);

//学习到的数据
unsigned char studyDataArr[3];

void savaProperties() {
  if (!SPIFFS_ok) {
    return;
  }
  if (SPIFFS.exists("/properties")) {
    SPIFFS.remove("/properties");
  }
  File propertiesFile = SPIFFS.open("/properties", "w");
  if (propertiesFile) {
    if (true || propertiesFile.available()) {
      String properties;
      jsonBuffer.clear();
      jsonBuffer["pass"] = accessPass;
      jsonBuffer["token"] = token;
      jsonBuffer["wifiName"] = wifiName;
      jsonBuffer["wifiPass"] = wifiPass;
      jsonBuffer["tokenStart"] = tokenStart;
      jsonBuffer["myWifiPass"] = myWifiPass;
      serializeJson(jsonBuffer, properties);
      jsonBuffer.clear();
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
  tokenStart = millis();
  savaProperties();
}

bool checkToken(String cToken) {
  if (!checkAccessPass()) {
    return false;
  }
  if (token.length() > 0 && token != cToken) {
    jsonBuffer.clear();
    String output;
    jsonBuffer["code"] = Code_token_err;
    serializeJson(jsonBuffer, output);
    //token无效
    srv.send(200, "text/plain", output);
    return false;
  }
  return true;
}

bool checkAccessPass() {
  if (accessPass == "") {
    //没有设置管理员密码
    jsonBuffer.clear();
    String output;
    jsonBuffer["code"] = Code_no_regist;
    serializeJson(jsonBuffer, output);
    srv.send(200, "text/plain", output);
    return false;
  }
  return true;
}
void handleSendSign() {
  jsonBuffer.clear();
  String output;
  if (!checkAccessPass) {
    return;
  }
  String cToken;
  String cKeycode = "";
  if (srv.args() > 0) {
    deserializeJson(jsonBuffer, srv.arg(0));
    cToken.concat((const char *)jsonBuffer["token"]);
    cKeycode.concat((const char *)jsonBuffer["keycode"]);
    jsonBuffer.clear();
  }
  if (!checkToken(cToken)) {
    return;
  }
  if (cKeycode.length() != 24) {
    jsonBuffer["code"] = Code_study_key_err;
    serializeJson(jsonBuffer, output);
    //按键id无效
    srv.send(200, "text/plain", output);
    return;
  }
  unsigned char preCode = 0;
  unsigned char userCode = 0;
  unsigned char dataCode = 0;
  for (int i = 0; i < 24; i++) {
    if (cKeycode.charAt(i) == '1') {
      switch (i / 8) {
        case 0:
          preCode |= (0x01 << (7 - i % 8));
          break;
        case 1:
          userCode |= (0x01 << (7 - i % 8));
          break;
        case 2:
          dataCode |= (0x01 << (7 - i % 8));
          break;
      }
    }
  }
  Serial.print(">>");
  Serial.print((char)2);
  Serial.print((char)3);
  Serial.write(preCode);
  Serial.write(userCode);
  Serial.write(dataCode);
  //发射成功
  jsonBuffer["code"] = Code_ok;
  serializeJson(jsonBuffer, output);
  srv.send(200, "text/plain", output);
}
/*
路径： device/study
入参： token 必选
      keyid 必选
*/
void handleStudy() {
  jsonBuffer.clear();
  String output;
  if (!checkAccessPass) {
    return;
  }
  String cToken;
  String cStudyKeyId;
  if (srv.args() > 0) {
    deserializeJson(jsonBuffer, srv.arg(0));
    cToken.concat((const char *)jsonBuffer["token"]);
    cStudyKeyId.concat((const char *)jsonBuffer["keyid"]);
    jsonBuffer.clear();
  }
  if (!checkToken(cToken)) {
    return;
  }
  if (cStudyKeyId == "") {
    jsonBuffer["code"] = Code_study_key_err;
    serializeJson(jsonBuffer, output);
    //按键id无效
    srv.send(200, "text/plain", output);
    return;
  }
  if (studyKeyId != "") {
    if (studyKeyId.equals(cStudyKeyId)) {
      if (studyDataArr[0] == 0 && studyDataArr[1] == 0 && studyDataArr[2] == 0) {
        if (millis() - studyTime > 10000) {
          //学习超时
          jsonBuffer["code"] = Code_study_outtime;
          jsonBuffer["keyid"] = cStudyKeyId;
          serializeJson(jsonBuffer, output);
          srv.send(200, "text/plain", output);
          studyKeyId = "";
          //退出学习
          Serial.print(">>");
          Serial.print(0);
          Serial.print(0);
          return;
        } else {
          //正在学习中
          jsonBuffer["code"] = Code_ok;
          jsonBuffer["keyid"] = cStudyKeyId;
          serializeJson(jsonBuffer, output);
          srv.send(200, "text/plain", output);
          return;
        }
      } else {
        //学习成功
        String studyResult;
        for (byte i = 0; i < 3; i++) {
          for (byte j = 7; j >= 0; j--) {
            studyResult.concat(studyDataArr[i] & (0x01 << j) == 0 ? '0' : '1');
          }
        }
        jsonBuffer["data"] = studyResult;
        jsonBuffer["keyid"] = cStudyKeyId;
        jsonBuffer["code"] = Code_ok;
        serializeJson(jsonBuffer, output);
        srv.send(200, "text/plain", output);
        studyKeyId = "";
        //退出学习
        Serial.print(">>");
        Serial.print(0);
        Serial.print(0);
        return;
      }
    } else {
      if (millis() - studyTime < 10000) {
        //正在学习中,不再接收其他按键的学习
        jsonBuffer["code"] = Code_being_study_other;
        jsonBuffer["keyid"] = studyKeyId;
        serializeJson(jsonBuffer, output);
        srv.send(200, "text/plain", output);
        return;
      } else {
        //开始学习新的按键
        studyKeyId = cStudyKeyId;
        studyTime = millis();
        Serial.print(">>");
        Serial.print(1);
        Serial.print(0);
      }
    }
  } else {
    //开始学习
    studyKeyId = cStudyKeyId;
    studyTime = millis();
    Serial.print(">>");
    Serial.print(1);
    Serial.print(0);
  }
  jsonBuffer["code"] = Code_ok;
  jsonBuffer["keyid"] = cStudyKeyId;
  serializeJson(jsonBuffer, output);
  srv.send(200, "text/plain", output);
}

/*
路径： device/modifywifipass
入参： token 必选
      pass
*/
void handleModifyWifiPass() {
  jsonBuffer.clear();
  String output;
  if (!checkAccessPass) {
    return;
  }
  String cToken;
  String cPass;

  if (srv.args() > 0) {
    deserializeJson(jsonBuffer, srv.arg(0));
    cPass.concat((const char *)jsonBuffer["pass"]);
    cToken.concat((const char *)jsonBuffer["token"]);
    jsonBuffer.clear();
  }
  if (!checkToken(cToken)) {
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
  if (!checkAccessPass) {
    return;
  }
  String cOldPass;
  String cNewPass;
  if (srv.args() > 0) {
    deserializeJson(jsonBuffer, srv.arg(0));
    cOldPass.concat((const char *)jsonBuffer["oldPass"]);
    cNewPass.concat((const char *)jsonBuffer["newPass"]);
    jsonBuffer.clear();
  }
  if (cNewPass.length() < 8 || cNewPass.length() > 20) {
    jsonBuffer["code"] = Code_pass_format_err;
  } else if (accessPass.length() > 0 && accessPass.equals(cOldPass)) {
    refreshToken();
    jsonBuffer.clear();
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
  if (!checkAccessPass) {
    return;
  }

  String cToken;
  String cWifiName;
  String cPass;
  if (srv.args() > 0) {
    deserializeJson(jsonBuffer, srv.arg(0));
    cPass.concat((const char *)jsonBuffer["pass"]);
    cWifiName.concat((const char *)jsonBuffer["wifiName"]);
    cToken.concat((const char *)jsonBuffer["token"]);
    jsonBuffer.clear();
  }
  if (!checkToken(cToken)) {
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
  if (millis() - tokenStart > 1000 * 10) {
    refreshToken();
    jsonBuffer.clear();
    jsonBuffer["token"] = token;
  }
  jsonBuffer["code"] = 0;
  jsonBuffer["wifiIp"] = WiFi.localIP();
  serializeJson(jsonBuffer, output);
  Serial.println(output);
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
  if (!checkAccessPass) {
    return;
  }

  String cPass;
  if (srv.args() > 0) {
    deserializeJson(jsonBuffer, srv.arg(0));
    cPass.concat((const char *)jsonBuffer["pass"]);
    jsonBuffer.clear();
  }
  if (accessPass.length() > 0 && accessPass.equals(cPass)) {
    refreshToken();
    jsonBuffer.clear();
    jsonBuffer["code"] = Code_ok;
    jsonBuffer["token"] = token;
    jsonBuffer["wifiIp"] = WiFi.localIP();
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
    cPass.concat((const char *)jsonBuffer["pass"]);
    jsonBuffer.clear();
  }

  if (cPass.length() < 8 || cPass.length() > 20) {
    jsonBuffer["code"] = Code_pass_format_err;
  } else {
    accessPass = cPass;
    refreshToken();
    jsonBuffer.clear();
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
    cToken.concat((const char *)jsonBuffer["token"]);
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
  if (!checkAccessPass()) {
    return;
  }
  String cToken;
  if (srv.args() > 0) {
    deserializeJson(jsonBuffer, srv.arg(0));
    cToken.concat((const char *)jsonBuffer["token"]);
    jsonBuffer.clear();
  }
  if (!checkToken(cToken)) {
    return;
  }
  if (wifiName == "") {
    jsonBuffer["code"] = Code_wifi_no_set;
    serializeJson(jsonBuffer, output);
    srv.send(200, "text/plain", output);
    return;
  }
  jsonBuffer["code"] = Code_ok;
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
        Serial.println(properties);
        deserializeJson(jsonBuffer, properties);
        if (jsonBuffer.containsKey("pass")) {
          accessPass = (const char *)jsonBuffer["pass"];
        }
        if (jsonBuffer.containsKey("token")) {
          token = (const char *)jsonBuffer["token"];
        }
        if (jsonBuffer.containsKey("tokenStart")) {
          tokenStart = jsonBuffer["tokenStart"];
        }
        if (jsonBuffer.containsKey("wifiName")) {
          wifiName = (const char *)jsonBuffer["wifiName"];
        }
        if (jsonBuffer.containsKey("wifiPass")) {
          wifiPass = (const char *)jsonBuffer["wifiPass"];
        }
        if (jsonBuffer.containsKey("myWifiPass") && jsonBuffer["myWifiPass"].size() > 0) {
          myWifiPass = (const char *)jsonBuffer["myWifiPass"];
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
  srv.on("/device/sendsign", handleSendSign);
  srv.on("/device/study", handleStudy);
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
  if (Serial.available()) {
    unsigned char data;
    String info;
    while (data = Serial.read()) {
      info.concat(data);
    }
    unsigned char action;
    unsigned char dataLen;
    if (info.startsWith(">>")) {
      info.getBytes(&action, 1, 2);
      info.getBytes(&dataLen, 1, 3);
      if (action == 0x01) {
        if (dataLen == 3) {
          info.getBytes(studyDataArr, dataLen, 4);
        }
      }
    }
  }
}