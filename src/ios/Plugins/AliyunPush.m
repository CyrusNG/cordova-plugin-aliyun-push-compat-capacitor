/********* AliyunPush.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import "AliyunNotificationLauncher.h"
#import "AliyunPushDelegate.h"

@interface AliyunPush : CDVPlugin {
    NSDictionary *_deathNotify;
}

@property (nonatomic,strong) CDVInvokedUrlCommand * messageCommand;
@property (nonatomic,strong) NSString *alertmsg;
@end

@implementation AliyunPush

- (void)pluginInitialize{

    [super pluginInitialize];
    // 推送通知 注册
    [[NSNotificationCenter defaultCenter] addObserver:self
                                            selector:@selector(onNotificationReceived:)
                                                 name:@"AliyunNotification"
                                               object:nil];

    // 推送消息 注册
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(onMessageReceived:)
                                                 name:@"AliyunNotificationMessage"
                                               object:nil];

//    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
//        [self requireNotifyPermission:nil];
//    });
}


/**
  弹出通知请求
 */
- (void)requireNotifyPermission:(CDVInvokedUrlCommand*)command{
    NSString *title = NSLocalizedStringWithDefaultValue(@"aliyun_dialog_title", @"Localizable", NSBundle.mainBundle, @"Require notification permission", nil);
    NSString *message = NSLocalizedStringWithDefaultValue(@"aliyun_dialog_message", @"Localizable", NSBundle.mainBundle, @"Please turn on the notification permission.", nil);
    NSString *cancelText = NSLocalizedStringWithDefaultValue(@"aliyun_dialog_negative_text", @"Localizable", NSBundle.mainBundle, @"IGNORE", nil);
    NSString *settingText = NSLocalizedStringWithDefaultValue(@"aliyun_dialog_positive_text", @"Localizable", NSBundle.mainBundle, @"SETTING", nil);

    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert];

    [alertController addAction:[UIAlertAction actionWithTitle:cancelText style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:SWIFT_CDVCommandStatus_ERROR] callbackId:command.callbackId];
    }]];

    [alertController addAction:[UIAlertAction actionWithTitle:settingText style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {

        if ([[UIDevice currentDevice].systemVersion floatValue] >= 10.0f) {

            [[UIApplication sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString] options:@{} completionHandler:^(BOOL success) {}];
        }else{
             [[UIApplication sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]];
        }
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
    }]];

    [self.viewController presentViewController:alertController animated:YES completion:^{}];
}

#pragma mark AliyunNotification通知
- (void)onNotificationReceived:(NSNotification *)notification {

    NSDictionary * info = notification.object;

    if(!info){
        return;
    }

    NSMutableDictionary *extra = [[NSMutableDictionary alloc] initWithDictionary:info];
    [extra removeObjectForKey:@"type"];
    [extra removeObjectForKey:@"body"];
    [extra removeObjectForKey:@"title"];

    NSMutableDictionary *message = [NSMutableDictionary dictionary];
    [message setObject:extra forKey:@"extra"];
    [message setObject:info[@"type"] forKey:@"type"];
    [message setObject:info[@"title"] forKey:@"title"];
    [message setObject:info[@"body"] forKey:@"content"];
    [message setObject:@"" forKey:@"url"];

    NSLog(@"x----数据来了");
    NSLog(@"%@",info[@"body"]);

//    _deathNotify = message;

    CDVPluginResult *result;
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:message];
    [result setKeepCallbackAsBool:true];
    [self.commandDelegate sendPluginResult:result callbackId:self.messageCommand.callbackId];


    NSString *requestData = [NSString stringWithFormat:@"sevenPushReceive(\"%@\")",info[@"body"]];

    [self.commandDelegate evalJs:requestData];
}

#pragma mark AliyunNotification消息

- (void)onMessageReceived:(NSNotification *)notification {

    NSDictionary * info = notification.object;
    if(!info){
        return;
    }
    NSMutableDictionary *message = [NSMutableDictionary dictionary];
    [message setObject:@"" forKey:@"extra"];
    [message setObject:info[@"type"] forKey:@"type"];
    [message setObject:info[@"title"] forKey:@"title"];
    [message setObject:info[@"body"] forKey:@"content"];
    [message setObject:@"" forKey:@"url"];

//     _deathNotify = message;

    CDVPluginResult *result;
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:message];
    [result setKeepCallbackAsBool:true];
    [self.commandDelegate sendPluginResult:result callbackId:self.messageCommand.callbackId];

}


-(NSString *)NSStringToJson:(NSString *)str
{
    NSMutableString *s = [NSMutableString stringWithString:str];

    [s replaceOccurrencesOfString:@"\\" withString:@"\\\\" options:NSCaseInsensitiveSearch range:NSMakeRange(0, [s length])];

    return [NSString stringWithString:s];
}

/**
 * 启动阿里云推送服务
 */
- (void)boot:(CDVInvokedUrlCommand*)command{

    [AliyunPushDelegate boot];

    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];

    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

/**
 * 接收阿里云的消息
 */
- (void)onMessage:(CDVInvokedUrlCommand*)command{


    NSDictionary *remoteinfo =  [[AliyunNotificationLauncher sharedAliyunNotificationLauncher] getRemoteInfo];

    if(!self.messageCommand && remoteinfo ){

        NSMutableDictionary *newContent = [[NSMutableDictionary alloc] initWithDictionary:remoteinfo];
        [newContent removeObjectForKey:@"aps"];
        [newContent removeObjectForKey:@"i"];
        [newContent removeObjectForKey:@"m"];
        [newContent setObject:@"notificationOpened" forKey:@"type"];

        CDVPluginResult *result;
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:newContent];
        [result setKeepCallbackAsBool:true];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }

    self.messageCommand = command;
}

/**
 * 阿里云推送绑定账号名
 * 获取设备唯一标识deviceId，deviceId为阿里云移动推送过程中对设备的唯一标识（并不是设备UUID/UDID）
 */
- (void)getRegisterId:(CDVInvokedUrlCommand*)command{

    NSString *deviceId =  [[AliyunNotificationLauncher sharedAliyunNotificationLauncher] getDeviceId];

    CDVPluginResult *result;

    if(deviceId.length != 0){
       result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:deviceId];
    }else{
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

/**
 * 是否开启了通知功能
 */
- (void)isEnableNotification:(CDVInvokedUrlCommand*)command{
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];

    [center getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings *settings){
        BOOL enable = false;
        switch (settings.authorizationStatus) {
            case UNAuthorizationStatusAuthorized: {
                enable = true;
                break;
            }
            case UNAuthorizationStatusDenied: {
                enable = false;
                break;
            }
            default: {
                enable = false;
                break;
            }
        }
        NSDictionary *dict = [NSDictionary dictionaryWithObject:@(enable)forKey:@"enable" ];

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dict];

        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

/**
 * 阿里云推送绑定账号名
 */
- (void)bindAccount:(CDVInvokedUrlCommand*)command{

    NSString* account = [command.arguments objectAtIndex:0];

    if(account.length != 0){

        [[AliyunNotificationLauncher sharedAliyunNotificationLauncher] bindAccountWithAccount:account andCallback:^(BOOL result) {

            CDVPluginResult *cdvresult;

            if(result){
                cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            }else{
                cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
            }

            [self.commandDelegate sendPluginResult:cdvresult callbackId:command.callbackId];

        }];
    }

}

/**
 * 阿里云推送账号解绑
 */
- (void)unbindAccount:(CDVInvokedUrlCommand*)command{

    [[AliyunNotificationLauncher sharedAliyunNotificationLauncher] unbindAccountAndCallback:^(BOOL result) {

        CDVPluginResult *cdvresult;

        if(result){
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }else{
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        }

        [self.commandDelegate sendPluginResult:cdvresult callbackId:command.callbackId];

    }];

}


/**
 *绑定标签
 */
- (void)bindTags:(CDVInvokedUrlCommand*)command{
    int target = [(NSNumber *)[command.arguments objectAtIndex:0] intValue];
    NSArray *tags = [command.arguments objectAtIndex:1];
    NSString *alias = command.arguments.count > 2 ? [command.arguments objectAtIndex:2] : nil;

    [[AliyunNotificationLauncher sharedAliyunNotificationLauncher] bindTagsWithTags:target :tags :alias andCallback:^(BOOL result) {
        CDVPluginResult *cdvresult;

        if(result){
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }else{
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        }

        [self.commandDelegate sendPluginResult:cdvresult callbackId:command.callbackId];

    }];
}

/**
 *解绑定标签
 */
- (void)unbindTags:(CDVInvokedUrlCommand*)command{
    int target = [(NSNumber *)[command.arguments objectAtIndex:0] intValue];
    NSArray *tags = [command.arguments objectAtIndex:1];
    NSString *alias = command.arguments.count > 2 ? [command.arguments objectAtIndex:2] : nil;

    [[AliyunNotificationLauncher sharedAliyunNotificationLauncher] unbindTagsWithTags:target :tags :alias andCallback:^(BOOL result) {

        CDVPluginResult *cdvresult;

        if(result){
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }else{
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        }

        [self.commandDelegate sendPluginResult:cdvresult callbackId:command.callbackId];

    }];

}

/**
 *查询标签
 */
- (void)listTags:(CDVInvokedUrlCommand*)command{

    [[AliyunNotificationLauncher sharedAliyunNotificationLauncher] listTagsAndCallback:^(id result) {

        CDVPluginResult *cdvresult;

        if(result == [NSNull null] ){

            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        }else{
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:(NSDictionary *)result];
        }

        [self.commandDelegate sendPluginResult:cdvresult callbackId:command.callbackId];

    }];

}


- (void)addAlias:(CDVInvokedUrlCommand*)command{
    NSString* aliases = [command.arguments objectAtIndex:0];
    if(aliases.length != 0){
        [[AliyunNotificationLauncher sharedAliyunNotificationLauncher]
         addAlias:aliases andCallback:^(BOOL result) {
            CDVPluginResult *cdvresult;
            if(result){
                cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            }else{
                cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
            }
            [self.commandDelegate sendPluginResult:cdvresult callbackId:command.callbackId];
        }];
    }
}

- (void)removeAlias:(CDVInvokedUrlCommand*)command{

    NSString *aliases = [command.arguments objectAtIndex:0];

    if (aliases.length!=0) {
        [[AliyunNotificationLauncher sharedAliyunNotificationLauncher]
         removeAlias:aliases andCallback:^(BOOL result) {
            CDVPluginResult *cdvresult;

            if(result){
                cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            }else{
                cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
            }

            [self.commandDelegate sendPluginResult:cdvresult callbackId:command.callbackId];
        }];
    }
}

- (void)listAliases:(CDVInvokedUrlCommand*)command{
    [[AliyunNotificationLauncher sharedAliyunNotificationLauncher]
     listAliases:^(id result) {

        CDVPluginResult *cdvresult;

        if(result == [NSNull null] ){

            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        }else{
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:(NSDictionary *)result];
        }

        [self.commandDelegate sendPluginResult:cdvresult callbackId:command.callbackId];

    }];
}

- (void)syncBadgeNum:(CDVInvokedUrlCommand*)command{
    
    NSString* stringNum = [command.arguments objectAtIndex:0];
    NSUInteger badgeNum = [stringNum integerValue];
    
    [[AliyunNotificationLauncher sharedAliyunNotificationLauncher]
     syncBadgeNum:badgeNum andCallback:^(BOOL result) {
        CDVPluginResult *cdvresult;

        if(result){
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }else{
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        }

        [self.commandDelegate sendPluginResult:cdvresult callbackId:command.callbackId];
    }];
    
}

- (void)setApplicationIconBadgeNumber:(CDVInvokedUrlCommand*)command{
    
    NSString* stringNum = [command.arguments objectAtIndex:0];
    NSUInteger badgeNum = [stringNum integerValue];

    UIApplication *app=[UIApplication sharedApplication];
    app.applicationIconBadgeNumber=badgeNum;
}



@end
