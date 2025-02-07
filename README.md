# cordova-plugin-aliyun-push-compat-capacitor
- 此插件为兼容CapacitorJS而修改的Cordova插件，目的是能在CapacitorJS下使用阿里云移动推送
- Android包含`小米`、`华为`、`荣耀`、`OPPO`、`VIVO`、`魅族` 厂商辅助通道，并支持多Channel，支持配置厂商通道列表

## 版本说明
- CapacitorJS: V5
- Gradle: V8
- xCode: V15.3
- Android Studio: Flamingo
- 阿里云推送SDK - Android: V3.9.0
- 阿里云推送SDK - iOS: V2.1.0

## 安装

```
npm install --save-dev cordova-plugin-aliyun-push-compat-capacitor
```

## 配置

`重要说明` ：

1、CapacitorJS不支持Cordova的Variable和Hook特性

2、CapacitorJS为兼容Cordova插件而禁止了Objective-C的AppDelegate分类

所以：

1、插件需要借助.env文件方式存放ID和KEY

2、插件内部不能使用Objective-C的分类(AppDelegate+AliyunPush.m)常用的swizzled method大法，『无痛』启动SDK和处理通知事件

因此相比一般的Cordova插件我们需要多一些配置步骤：

### CapacitorJS

步骤一：根目录下创建.env文件并添加Id和Key信息([参照.env章节](###-.env))

步骤二：相应业务逻辑适当时候调用AliyunPush API:
```
  Aliyun.onMessage = message => this.messageHandler(message);
  Aliyun.onError = err => this.errorHandler(err);
  await AliyunPush.boot();
```

### iOS

步骤一：配置读取.env（[参照.env章节 - 如何在xCode中读取.env文件？- 步骤一、二、三、四](###-.env)）

步骤二：AppDelegate.swift中相关的通知函数中添加发送自定义通知的代码：

```
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        NotificationCenter.default.post(name: Notification.Name(rawValue: "CDApplicationDidRegisterForRemoteNotificationsNotification"), object: deviceToken)
    }
    
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: any Error) {
        NotificationCenter.default.post(name: Notification.Name(rawValue: "CDApplicationDidFailToRegisterForRemoteNotificationsNotification"), object: error)
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        NotificationCenter.default.post(name: Notification.Name(rawValue: "CDApplicationDidReceiveRemoteNotificationNotification"), object: application, userInfo: userInfo)
    }
```

步骤三：同步插件到app:

```
npx cap sync
```

### Android

步骤一：配置读取.env（[参照.env章节 - 如何在Android Studio中读取.env文件？- 步骤一](###-.env)

步骤二：在项目Project下的variables.gradle添加aliyunPushDebug和aliyunPushChannelId配置：（channelId支持多个）
```
ext {
    ...
    aliyunPushDebug = false
    aliyunPushChannelId = 'CHANNEL1:应用公告,CHANNEL2:其他通知'
}
```

步骤三：res/values/strings.xml中添加如下内容：（cordova中的plugin.xml有写但是capacitor不知道什么原因没有插入相应内容）

```
<string name="aliyun_dialog_title">消息提醒需要通知权限</string>
<string name="aliyun_dialog_message">请前往设置打开应用通知权限。</string>
<string name="aliyun_dialog_negative_text">忽略</string>
<string name="aliyun_dialog_positive_text">设置</string>
```

步骤四：res/drawable/中添加通知小图标名字为：ic_notification_icon.png

[<img src="https://github.com/CyrusNG/cordova-plugin-aliyun-push-compat-capacitor/blob/main/screenshoot/android_add_notification_small_icon.png" width="300"/>](android_add_notification_small_icon.png)

简单生成通知小图标线上工具：https://romannurik.github.io/AndroidAssetStudio/icons-notification.html

步骤五：同步插件到app:

```
npx cap sync
```

## .env

`=== CapacitorJS、iOS、Android工程都使用了.env可以忽略此章节 ===`

此插件使用了.env文件方式存放必要的id和secret设置（因为CapacitorJS不支持Cordova的Variables特性）

请先在根目录下创建以下内容的.env文件：

```
# iOS
EMAS_IOS_APP_KEY=***
EMAS_IOS_APP_SECRET=***
# Android
EMAS_ANDROID_APP_KEY=***
EMAS_ANDROID_APP_SECRET=***
# Xiao Mi
EMAS_MI_APP_ID=***
EMAS_MI_APP_KEY=***
# Huawei
EMAS_HUAWEI_APP_ID=***
# Honor
EMAS_HONOR_APP_ID=***
# Vivo
EMAS_VIVO_APP_ID=***
EMAS_VIVO_APP_KEY=***
# Oppo
EMAS_OPPO_APP_KEY=***
EMAS_OPPO_APP_SECRET=***
# Meizu
EMAS_MEIZU_APP_ID=***
EMAS_MEIZU_APP_KEY=***
```

### 如何在CapacitorJS中读取.env文件？

步骤一：安装 [dotenv](https://www.npmjs.com/package/dotenv)

```
npm install --save-dev dotenv
```

步骤二：在capacitor.config.js文件内引用dotenv

```
require('dotenv').config();

const {
  EMAS_ANDROID_APP_KEY,
  EMAS_ANDROID_APP_SECRET,
  EMAS_MI_APP_ID,
  EMAS_MI_APP_KEY,
  ...
} = process.env;
```

### 如何在xCode中读取.env文件？

`思路：添加Build前脚本 -> 读取.evn文件 -> 写入Env-App.xcconfig -> Info.plist使用xcconfig变量 -> 代码读取Info.plist`

步骤一：创建App.debug.xcconfig / App.release.xcconfig / Env-App.xcconfig 文件

1、项目结构如下：

[<img src="https://github.com/CyrusNG/cordova-plugin-aliyun-push-compat-capacitor/blob/main/screenshoot/iOS_add_xcconfig.png" width="300"/>](iOS_add_xcconfig.png)

2、在App.debug.xconfig / App.release.xconfig内include相应的Pods和Env的xcconfig：

```
//根据不同的环境(debug/release)修改以下对应的Pods的xcconfig路径
#include? "Pods/Target Support Files/Pods-App/Pods-App.XXXXXXXX.xcconfig"
#include? "Env/Env-App.xcconfig"
```

[<img src="https://github.com/CyrusNG/cordova-plugin-aliyun-push-compat-capacitor/blob/main/screenshoot/iOS_xcconfig_content.png" height="50%"/>](iOS_xcconfig_content.png)

3、Env-App.xcconfig内容留空（因为脚本会读.env文件并写入此文件）

步骤二：在PROJECT -> App -> Info -> Configurations -> Debug / Release -> 选择刚创建的App.debug / App.release

[<img src="https://github.com/CyrusNG/cordova-plugin-aliyun-push-compat-capacitor/blob/main/screenshoot/iOS_update_config_setting.png"/>](iOS_update_config_setting.png)

步骤三：在Build Pre-action中添加脚本

1、打开：xCode菜单 -> Product -> Scheme -> Edit Scheme... -> Build -> Pre-actions -> +

2、在『Provide build settings from』选项里选择自己的App

3、加入如下脚本：

```
# Target Key Settings
ID_KEY="EMAS_IOS_APP_KEY"
SECRET_KEY="EMAS_IOS_APP_SECRET"
# write env variables into xcconfig file from .env
dotEnvFilePath="${SRCROOT}/../../.env"
envAppConfigPath="${SRCROOT}/Env/Env-App.xcconfig"
targetExp="/^(${ID_KEY}|${SECRET_KEY}).*/!d"
sed -E $targetExp $dotEnvFilePath > $envAppConfigPath
```

[<img src="https://github.com/CyrusNG/cordova-plugin-aliyun-push-compat-capacitor/blob/main/screenshoot/iOS_add_script_in_scheme.png" height="50%"/>](iOS_add_script_in_scheme.png)

步骤四：在Info.plist加入相应key和dict：

```
  <key>Aliyun Push Config</key>
  <dict>
    <key>Debug</key>
    <false/>
    <key>App Key</key>
    <string>$(EMAS_IOS_APP_KEY)</string>
    <key>App Secret</key>
    <string>$(EMAS_IOS_APP_SECRET)</string>
  </dict>
```

[<img src="https://github.com/CyrusNG/cordova-plugin-aliyun-push-compat-capacitor/blob/main/screenshoot/iOS_update_info_plist.png" height="50%"/>](iOS_update_info_plist.png)

步骤五：在代码中读取Info.plist获取相关Id和Key`（插件内部实现，无需手动设置）`

```
// 使用Info.plist中的Aliyun Push Config
NSDictionary *infoDictionary = [[NSBundle mainBundle] infoDictionary];
NSDictionary *aliyunPushConfig = [infoDictionary objectForKey:@"Aliyun Push Config"];

// 获取config信息
NSString *appKey = [aliyunPushConfig objectForKey:@"App Key"];
NSString *appSecret = [aliyunPushConfig objectForKey:@"App Secret"];
Boolean enableDebug = [[aliyunPushConfig objectForKey:@"Debug"] boolValue];
```

### 如何在Android Studio中读取.env文件？

`思路：Project下的build.gradle添加task -> 读取.evn文件 -> 写入System properties -> 用properties设置build.gradle的manifestPlaceholders -> 使用manifestPlaceholders设置AndroidManifest.xml的<meta-data> -> 代码读取<meta-data>`

步骤一：在Project下的build.gradle底部添加task读取.env到System.props：

```
task setPropsFromDotEnv(type: Exec) {
  file('../.env').readLines().each() {
    def (key, value) = it.tokenize('=')
    if (key.charAt(0) != "#") System.setProperty(key, value)
  }
//  println(System.props)
}
```

[<img src="https://github.com/CyrusNG/cordova-plugin-aliyun-push-compat-capacitor/blob/main/screenshoot/android_build_gradle_readenv_task.png"/>](android_build_gradle_readenv_task.png)

步骤二：Module的build.gradle中加入android.defaultConfig.manifestPlaceholders，并设置对应的System.props：`（插件内部实现，无需手动设置）`

```
android {
    defaultConfig {
        manifestPlaceholders.EMAS_ANDROID_APP_KEY = System.props.EMAS_ANDROID_APP_KEY
        manifestPlaceholders.EMAS_ANDROID_APP_SECRET = System.props.EMAS_ANDROID_APP_SECRET
        manifestPlaceholders.EMAS_MI_APP_ID = System.props.EMAS_MI_APP_ID
        manifestPlaceholders.EMAS_MI_APP_KEY = System.props.EMAS_MI_APP_KEY
        manifestPlaceholders.EMAS_HUAWEI_APP_ID = System.props.EMAS_HUAWEI_APP_ID
        manifestPlaceholders.EMAS_HONOR_APP_ID = System.props.EMAS_HONOR_APP_ID
        manifestPlaceholders.EMAS_VIVO_APP_ID = System.props.EMAS_VIVO_APP_ID
        manifestPlaceholders.EMAS_VIVO_APP_KEY = System.props.EMAS_VIVO_APP_KEY
        manifestPlaceholders.EMAS_OPPO_APP_KEY = System.props.EMAS_OPPO_APP_KEY
        manifestPlaceholders.EMAS_OPPO_APP_SECRET = System.props.EMAS_OPPO_APP_SECRET
        manifestPlaceholders.EMAS_MEIZU_APP_ID = System.props.EMAS_MEIZU_APP_ID
        manifestPlaceholders.EMAS_MEIZU_APP_KEY = System.props.EMAS_MEIZU_APP_KEY
    }
}
```

步骤三：在AndroidManifest.xml中<application>内加入<meta-data>读取manifestPlaceholders中的值：`（插件内部实现，无需手动设置）`

```
<application>
    ...
    <meta-data android:name="com.alibaba.app.appkey" android:value="${EMAS_ANDROID_APP_KEY}"/>
    <meta-data android:name="com.alibaba.app.appsecret" android:value="${EMAS_ANDROID_APP_SECRET}"/>
    <meta-data android:name="MI_PUSH_APP_ID" android:value="${EMAS_MI_APP_ID}"/>
    <meta-data android:name="MI_PUSH_APP_KEY" android:value="${EMAS_MI_APP_KEY}"/>
    <meta-data android:name="com.huawei.hms.client.appid" android:value="appid=${EMAS_HUAWEI_APP_ID}"/>
    <meta-data android:name="com.hihonor.push.app_id" android:value="appid=${EMAS_HONOR_APP_ID}"/>
    <meta-data android:name="com.vivo.push.app_id" android:value="${EMAS_VIVO_APP_ID}"/>
    <meta-data android:name="com.vivo.push.api_key" android:value="${EMAS_VIVO_APP_KEY}"/>
    <meta-data android:name="OPPO_PUSH_APP_KEY" android:value="${EMAS_OPPO_APP_KEY}"/>
    <meta-data android:name="OPPO_PUSH_APP_SECRET" android:value="${EMAS_OPPO_APP_SECRET}"/>
    <meta-data android:name="MZ_PUSH_APP_ID" android:value="${EMAS_MEIZU_APP_ID}"/>
    <meta-data android:name="MZ_PUSH_APP_KEY" android:value="${EMAS_MEIZU_APP_KEY}"/>
    ...
</application>
```

步骤四：在代码中读取<meta-data>获取相关Id和Key`（插件内部实现，无需手动设置）`

```
    ApplicationInfo appInfo = application.getPackageManager().getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA);
    boolean enableDebug = appInfo.metaData.getBoolean("ALIYUN_PUSH_DEBUG", false);
    String miPushAppId  = appInfo.metaData.getString("MI_PUSH_APP_ID", "").trim();
    String miPushAppKey  = appInfo.metaData.getString("MI_PUSH_APP_KEY", "").trim();
```

## JS API

### 初始化API
```
  Aliyun.onMessage = message => this.messageHandler(message);
  Aliyun.onError = err => this.errorHandler(err);
  await AliyunPush.boot("xiaomi,huawei,honor,vivo,oppo,meizu");
```

返回消息Message结构：

{

  type:string 消息类型,

  title:string '阿里云推送',

  body:string '推送的内容',

  params:string | Object<k,v> 外健,

  url:路由（后台发送推送时，在ExtParameters参数里写入url如{url:'demoapp://...'}）

  id: url中的id

}

消息Type类型：

{

  message:透传消息，

  notification:通知接收，

  notificationOpened:通知点击，

  notificationReceived：通知到达，

  notificationRemoved：通知移除，

  notificationClickedWithNoAction：通知到达，

  notificationReceivedInApp：通知到达打开 app

}

### 其他API
```
  /**
   * 启动推送
   * @param  {string} androidVendors Android厂商通道列表: xiaomi,huawei,honor,vivo,oppo,meizu
   * @return {void}
   */
  boot: async function (androidVendors) -> void

  /**
   * 检查通知的权限
   * @param  {boolean} force 检查到没权限时直接申请权限
   * @return {object} flags -> granted, denied, asked, neverAsked, unknown (iOS only)
   */
  checkPermission: async function (force) -> object

  /**
   * 打开App设置页
   * @return {void}
   */
  openAppSettings: async function () -> void

  /**
   * 没有权限时，请求开通通知权限
   * @return {void}
   */
  requestPermission: async function () -> void
  
  /**
   * 获取设备唯一标识deviceId，deviceId为阿里云移动推送过程中对设备的唯一标识（并不是设备UUID/UDID）
   * @return {string} 设备注册码
   */
  getRegisterId: async function () -> string

  /**
   * 阿里云推送绑定账号名
   * @param  {string} account 账号
   * @return {void}
   */
  bindAccount: async function (account) -> void

  /**
   * 阿里云推送解除账号名,退出切换账号时调用
   * @return {void}
   */
  unbindAccount: async function () -> void

  /**
   * 阿里云推送绑定标签
   * @param  {string} target 目标
   * @param  {string[]} tags 标签列表
   * @param  {string} alias 别名
   * @return {void}
   */
  bindTags: async function (target, tags, alias) -> void

  /**
   * 阿里云推送解除绑定标签
   * @param  {string} target 目标
   * @param  {string[]} tags 标签列表
   * @param  {string} alias 别名
   * @return {void}
   */
  unbindTags: async function (target, tags, alias) -> void

  /**
   * 阿里云推送列出标签
   * @return {void}
   */
  listTags: async function () -> void

  /**
   * 添加别名
   * @param  {string} alias 别名
   * @return {void}
   */
  addAlias: async function (alias) -> void

  /**
   * 解绑别名
   * @param  {string} alias 别名
   * @return {void}
   */
  removeAlias: async function (alias) -> void

  /**
   * 获取别名列表
   * @return {void}
   */
  listAliases: async function () -> void

  /**
   * 设置服务端角标数量 - iOS ONLY
   * @param  {string} badgeNum 角标数量
   * @return {void}
   */
  syncBadgeNum: async function (badgeNum) -> void

  /**
   * 设置本地角标数量 - iOS ONLY
   * @param  {string} badgeNum 角标数量
   * @return {void}
   */
  setBadgeNum: async function (badgeNum) -> void
  
```

## 常见问题

- `Android`

  - 查看日志显示通知已经从服务器发送到客户端但没有在通知栏显示
  
    Android 8.0以上需要设置NotificationChannel，详情：https://help.aliyun.com/document_detail/67398.html

    客户端和服务器设置的Channel必须一致，默认客户端端口是1!

    如果配置了厂商通道，调用API时候必须加入辅助弹窗需要的参数如下：

    ```
      AndroidOpenType: ACTIVITY
      AndroidActivity: com.example.myFirstApp.MainActivity
      AndroidPopupActivity:com.example.myFirstApp.MainActivity
      AndroidPopupTitle: <通知标题>
      AndroidPopupBody: <通知内容>
    ```
    
    注意：辅助弹窗仅在当前厂商通道设备的应用后台进程被清理时生效，对非接入厂商通道设备和在线的设备不生效并优先走阿里云推送。

  - 厂商通道每个都不同，如何优化客户端创建的通道数量

    建议先创建小米通知类别，再根据小米生成的通道ID手动设置oppo通道ID，这样可以统一客户端的通知类别。

    在客户端设置variables.gradle添加aliyunPushChannelId配置:

    ```
    ext {
      ...
      aliyunPushChannelId = '123456:应用公告'
    }
    ```

    在服务器调用API时候添加如下参数:

    ```
      ......
      "androidNotificationChannel": "123456",
      "androidNotificationXiaomiChannel": "123456"
      ......
    ```

  - 杀死App点击通知无法打开APP
  
    后端推送时添加 `AndroidExtParameters {open_type:"application"}`
  
  - com.android.tools.build:gradle:3.3.0打包报错
    
    使用3.3.3版

  - `Android 8.0`以上无法获取到`Token`

    检查是否配置了`network_security_config.xml`信息，具体百度了解

  - 构建`apk`时候报错`Missing classes detected while running R8.`或者启动App时闪退？
    
    这是构建时使用了混淆导致的，在`app`的`proguard-rules.pro`中添加以下内容：

    ```
      # Rules for general channel of aliyun EMAS
      -keepclasseswithmembernames class ** {
          native <methods>;
      }
      -keepattributes Signature
      -keep class sun.misc.Unsafe { *; }
      -keep class com.taobao.** {*;}
      -keep class com.alibaba.** {*;}
      -keep class com.alipay.** {*;}
      -keep class com.ut.** {*;}
      -keep class com.ta.** {*;}
      -keep class anet.**{*;}
      -keep class anetwork.**{*;}
      -keep class org.android.spdy.**{*;}
      -keep class org.android.agoo.**{*;}
      -keep class android.os.**{*;}
      -keep class org.json.**{*;}
      -dontwarn com.taobao.**
      -dontwarn com.alibaba.**
      -dontwarn com.alipay.**
      -dontwarn anet.**
      -dontwarn org.android.spdy.**
      -dontwarn org.android.agoo.**
      -dontwarn anetwork.**
      -dontwarn com.ut.**
      -dontwarn com.ta.**

      # Rules for xiaomi push channel of aliyun EMAS
      -keep class com.xiaomi.** {*;}
      -dontwarn com.xiaomi.**

      # Rules for huawei push channel of aliyun EMAS
      -keep class com.huawei.** {*;}
      -dontwarn com.huawei.**

      # Rules for honor push channel of aliyun EMAS
      -ignorewarnings
      -keepattributes *Annotation*
      -keepattributes Exceptions
      -keepattributes InnerClasses
      -keepattributes Signature
      -keepattributes SourceFile,LineNumberTable
      -keep class com.hihonor.push.**{*;}

      # Rules for vivo push channel of aliyun EMAS
      -keep class com.vivo.** {*;}
      -dontwarn com.vivo.**

      # Rules for oppo push channel of aliyun EMAS
      -keep public class * extends android.app.Service

      # Rules for meizu push channel of aliyun EMAS
      -keep class com.meizu.cloud.** {*;}
      -dontwarn com.meizu.cloud.**
    ```

- `iOS`

  - `iOS`无法获取到`Token`
   
    `Xcode`中确认开启以下两项

    [<img src="https://github.com/CyrusNG/cordova-plugin-aliyun-push-compat-capacitor/blob/main/screenshoot/iOS_notification_config.png"/>](iOS_notification_config.png)

  - 角标数字不正确
   
    当前iOS角标做法是打开APP或点击通知就读取通知个数并同步到阿里云服务器，请在通知添加 { iOSBadgeAutoIncrement: true }
