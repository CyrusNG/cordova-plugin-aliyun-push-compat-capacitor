//
//  AliyunPush.h
//  PushCordova
//
//  Created by ☺strum☺ on 2018/4/11.
//
#import <Cordova/CDV.h>

@interface AliyunPush : CDVPlugin

/**
 * 接收阿里云的消息
 */
- (void)onMessage:(CDVInvokedUrlCommand*)command;

/**
  检查通知权限
 */
- (void)checkPermission:(CDVInvokedUrlCommand*)command;

/**
请求通知权限
*/
- (void)requestPermission:(CDVInvokedUrlCommand*)command;

/**
  打开App设置页面
 */
- (void)openAppSettings:(CDVInvokedUrlCommand*)command;

/**
 * 启动阿里云推送服务
 */
- (void)boot:(CDVInvokedUrlCommand*)command;

/**
 * 阿里云推送绑定账号名
 * 获取设备唯一标识deviceId，deviceId为阿里云移动推送过程中对设备的唯一标识（并不是设备UUID/UDID）
 */
- (void)getRegisterId:(CDVInvokedUrlCommand*)command;

/**
 * 阿里云推送绑定账号名
 */
- (void)bindAccount:(CDVInvokedUrlCommand*)command;


/**
* 阿里云推送账号解绑
*/
- (void)unbindAccount:(CDVInvokedUrlCommand*)command;

/**
 *绑定标签
 */
- (void)bindTags:(CDVInvokedUrlCommand*)command;

/**
 *解绑定标签
 */
- (void)unbindTags:(CDVInvokedUrlCommand*)command;

/**
 *查询标签
 */
- (void)listTags:(CDVInvokedUrlCommand*)command;

/**
 *添加别名
 */
- (void)addAlias:(CDVInvokedUrlCommand*)command;

/**
 *删除别名
 */
- (void)removeAlias:(CDVInvokedUrlCommand*)command;

/**
 *查询别名
 */
- (void)listAliases:(CDVInvokedUrlCommand*)command;

/**
 *同步服务端角标
 */
- (void)syncBadgeNum:(CDVInvokedUrlCommand*)command;

/**
 *设置本地角标
 */
- (void)setBadgeNum:(CDVInvokedUrlCommand*)command;

@end
