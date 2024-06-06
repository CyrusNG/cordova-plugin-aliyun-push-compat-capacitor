# cordova-plugin-aliyun-push-compat-capacitor
- 基于 https://www.npmjs.com/package/@comingzones/cordova-plugin-aliyun-push 修改而来
- 此插件为兼容CapacitorJS而修改的Cordova插件，目的是能在CapacitorJS下使用阿里云移动推送
- Android包含`小米`、`华为`、`荣耀`、`OPPO`、`VIVO`、`魅族` 厂商辅助通道

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
步骤二：相应业务逻辑适当时候调用init()
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
步骤一：配置读取.env（[参照.env章节 - 如何在Android Studio中读取.env文件？- 步骤一](###-.env)）
步骤二：res/values/strings.xml中添加如下内容：（cordova中的plugin.xml有写但是capacitor不知道什么原因没有插入相应内容）
```
<string name="aliyun_dialog_title">消息提醒需要通知权限</string>
<string name="aliyun_dialog_message">请前往设置打开应用通知权限。</string>
<string name="aliyun_dialog_negative_text">忽略</string>
<string name="aliyun_dialog_positive_text">设置</string>
```
步骤三：同步插件到app:
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
![](https://github.com/CyrusNG/cordova-plugin-aliyun-push-compat-capacitor/blob/master/screenshoot/iOS_add_xcconfig.png)
2、在App.debug.xconfig / App.release.xconfig内include相应的Pods和Env的xcconfig：
```
//根据不同的环境(debug/release)修改以下对应的Pods的xcconfig路径
#include? "Pods/Target Support Files/Pods-App/Pods-App.XXXXXXXX.xcconfig"
#include? "Env/Env-App.xcconfig"
```
![](https://github.com/CyrusNG/cordova-plugin-aliyun-push-compat-capacitor/blob/master/screenshoot/iOS_xcconfig_content.png)
3、Env-App.xcconfig内容留空（因为脚本会读.env文件并写入此文件）
步骤二：在PROJECT -> App -> Info -> Configurations -> Debug / Release -> 选择刚创建的App.debug / App.release
![](https://github.com/CyrusNG/cordova-plugin-aliyun-push-compat-capacitor/blob/master/screenshoot/iOS_update_config_setting.png)
步骤三：在Build Pre-action中添加脚本
1、打开：xCode菜单 -> Product -> Scheme -> Edit Scheme... -> Build -> Pre-actions -> +
2、在『Provide build settings from』选项里选择自己的App
![](https://github.com/CyrusNG/cordova-plugin-aliyun-push-compat-capacitor/blob/master/screenshoot/iiOS_add_script_in_scheme.png)
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
![](https://github.com/CyrusNG/cordova-plugin-aliyun-push-compat-capacitor/blob/master/screenshoot/iOS_add_script_in_scheme.png)
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
![](https://github.com/CyrusNG/cordova-plugin-aliyun-push-compat-capacitor/blob/master/screenshoot/iOS_update_info_plist.png.png)
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
![](https://github.com/CyrusNG/cordova-plugin-aliyun-push-compat-capacitor/blob/master/screenshoot/android_build_gradle_readenv_task.png)
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
    <meta-data android:name="ALIYUN_PUSH_DEBUG" android:value="${ALIYUN_PUSH_DEBUG}"/>
    <meta-data android:name="CHANNEL_ID" android:value="${ALIYUN_PUSH_CHANNEL_ID}"/>
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


## 注意
- `Android`

  - 杀死 App 点击通知无法打开 APP 的,后端推送时添加 `AndroidExtParameters {open_type:"application"}`
  - 因为 android 应用商店的隐私协议需要在同意后才能获取 某些信息，所以添加了 initPush 初始化。
  - 打包报错的需要修改 build.gradle 中 com.android.tools.build:gradle:3.3.3 版本号 3.3.0 是不行，我暂时使用的 3.3.3


## JS API
```
   /**
     * 初始化阿里云推送服务
     * @param  {Function} successCallback 成功回调
     * @param  {Function} errorCallback   失败回调
     * @return {void}
     */
    init: function(successCallback, errorCallback)
    
    /**
     * 获取设备唯一标识deviceId，deviceId为阿里云移动推送过程中对设备的唯一标识（并不是设备UUID/UDID）
     * @param  {Function} successCallback 成功回调
     * @param  {Function} errorCallback   失败回调
     * @return {void}
     */
    getRegisterId: function(successCallback, errorCallback)

    /**
     * 阿里云推送绑定账号名
     * @param  {string} account         账号
     * @param  {Function} successCallback 成功回调
     * @param  {Function} errorCallback   失败回调
     * @return {void}
     */
    bindAccount: function(account, successCallback, errorCallback)

    /**
     * 阿里云推送解除账号名,退出或切换账号时调用
     * @param  {Function} successCallback 成功回调
     * @param  {Function} errorCallback   失败回调
     * @return {void}
     */
    unbindAccount: function(successCallback, errorCallback)

    /**
     * 阿里云推送绑定标签
     * @param  {string[]} tags            标签列表
     * @param  {Function} successCallback 成功回调
     * @param  {Function} errorCallback   失败回调
     * @return {void}
     */
    bindTags: function(tags, successCallback, errorCallback)

    /**
     * 阿里云推送解除绑定标签
     * @param  {string[]} tags            标签列表
     * @param  {Function} successCallback 成功回调
     * @param  {Function} errorCallback   失败回调
     * @return {void}
     */
    unbindTags: function(tags, successCallback, errorCallback)

    /**
     * 阿里云推送解除绑定标签
     * @param  {Function} successCallback 成功回调
     * @param  {Function} errorCallback   失败回调
     * @return {void}
     */
    listTags: function(successCallback, errorCallback)

    /**
     * 添加别名
     * @param  {Function} successCallback 成功回调
     * @param  {Function} errorCallback   失败回调
     * @return {void}
     */
    addAlias: function (alias, successCallback, errorCallback)

    /**
     * 解绑别名
     * @param  {Function} successCallback 成功回调
     * @param  {Function} errorCallback   失败回调
     * @return {void}
     */
    removeAlias: function (alias, successCallback, errorCallback)

    /**
     * 获取别名列表
     * @param  {Function} successCallback 成功回调
     * @param  {Function} errorCallback   失败回调
     * @return {void}
     */
    listAliases: function (successCallback, errorCallback)

    /**
      * 没有权限时，请求开通通知权限，其他路过
      * @param  string msg  请求权限的描述信息
      * @param {} successCallback
      * @param {*} errorCallback
      */
    requireNotifyPermission:function(msg,successCallback, errorCallback)

    /**
    * 阿里云推送消息透传回调
    * @param  {Function} successCallback 成功回调
    */
    onMessage:function(sucessCallback) ;

    # sucessCallback:调用成功回调方法，注意没有失败的回调，返回值结构如下：
    #json: {
      type:string 消息类型,
      title:string '阿里云推送',
      content:string '推送的内容',
      extra:string | Object<k,v> 外健,
      url:路由（后台发送推送时，在ExtParameters参数里写入url如{url:'demoapp://...'}）
    }

    #消息类型
    {
      message:透传消息，
      notification:通知接收，
      notificationOpened:通知点击，
      notificationReceived：通知到达，
      notificationRemoved：通知移除，
      notificationClickedWithNoAction：通知到达，
      notificationReceivedInApp：通知到达打开 app
    }

```

## 常见问题

1. `Android 8.0`以上无法获取到`Token`
   检查是否配置了`network_security_config.xml`信息，具体百度了解

1. `iOS`无法获取到`Token`
   `Xcode`中确认开启以下两项
   ![](https://github.com/CyrusNG/cordova-plugin-aliyun-push-compat-capacitor/blob/master/screenshoot/iOS_notification_config.png)