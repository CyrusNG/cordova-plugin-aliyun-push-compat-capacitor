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

#pragma mark AliyunNotification通知
- (void)onNotificationReceived:(NSNotification *)notification {

    NSDictionary * info = notification.object;

    if(!info){
        return;
    }

    NSMutableDictionary *params = [[NSMutableDictionary alloc] initWithDictionary:info];
    [params removeObjectForKey:@"type"];
    [params removeObjectForKey:@"body"];
    [params removeObjectForKey:@"title"];

    NSMutableDictionary *message = [NSMutableDictionary dictionary];
    [message setObject:info[@"type"] forKey:@"type"];
    [message setObject:info[@"title"] forKey:@"title"];
    [message setObject:info[@"body"] forKey:@"body"];
    [message setObject:params forKey:@"params"];
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
    if(!info) { return; }
    NSMutableDictionary *message = [NSMutableDictionary dictionary];
    [message setObject:info[@"type"] forKey:@"type"];
    [message setObject:info[@"title"] forKey:@"title"];
    [message setObject:info[@"body"] forKey:@"body"];
    [message setObject:@"" forKey:@"params"];
    [message setObject:@"" forKey:@"url"];

    // _deathNotify = message;

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
  检查通知权限
 */
- (void)checkPermission:(CDVInvokedUrlCommand*)command {
    BOOL force = [[command.arguments objectAtIndex:0] isEqual: @YES]? YES: NO;

    NSMutableDictionary *savedReturnObject = [NSMutableDictionary dictionary];

    dispatch_async(dispatch_get_main_queue(), ^{
        
        UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
        [center getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings * _Nonnull settings) {
            
            switch (settings.authorizationStatus) {
                // User hasn't accepted or rejected permissions yet. This block shows the allow/deny dialog
                case UNAuthorizationStatusNotDetermined:
                    savedReturnObject[@"neverAsked"] = @YES;
                    break;
                case UNAuthorizationStatusDenied:
                    savedReturnObject[@"denied"] = @YES;
                    break;
                case UNAuthorizationStatusAuthorized:
                    savedReturnObject[@"granted"] = @YES;
                    break;
                default:
                    savedReturnObject[@"unknown"] = @YES;
                    break;
            }
            
            if(savedReturnObject[@"neverAsked"] == nil) { savedReturnObject[@"asked"] = @YES; }
            
            if (force && savedReturnObject[@"neverAsked"] != nil) {
                [self requestPermission: command];
            } else {
                [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:savedReturnObject] callbackId:command.callbackId];
            }
        }];
    });
}

/**
请求通知权限
*/
- (void)requestPermission:(CDVInvokedUrlCommand*)command{
    
    NSMutableDictionary *savedReturnObject = [NSMutableDictionary dictionary];
    
    float systemVersionNum = [[[UIDevice currentDevice] systemVersion] floatValue];
    
    // iOS 10+ Notifications Permission
    if (systemVersionNum >= 10.0) {
        UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
        [center requestAuthorizationWithOptions: (UNAuthorizationOptionAlert | UNAuthorizationOptionBadge | UNAuthorizationOptionSound) completionHandler:^(BOOL granted, NSError * _Nullable error) {
            savedReturnObject[@"asked"] = @YES;
            if(granted) { savedReturnObject[@"granted"] = @YES; }
            else { savedReturnObject[@"denied"] = @YES; }
            [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:savedReturnObject] callbackId:command.callbackId];
        }];
        
    // iOS 8+ Notifications Permission
    }else if (systemVersionNum >= 8.0) {
        // iOS 8 Notifications
#pragma clang diagnostic push
#pragma clang diagnostic ignored"-Wdeprecated-declarations"
        [[UIApplication sharedApplication] registerUserNotificationSettings:[UIUserNotificationSettings settingsForTypes: (UIUserNotificationTypeAlert | UIUserNotificationTypeBadge | UIUserNotificationTypeSound) categories:nil]];
        savedReturnObject[@"asked"] = @YES;
        savedReturnObject[@"granted"] = @YES;
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:savedReturnObject] callbackId:command.callbackId];
#pragma clang diagnostic pop
    // legacy iOS Notifications Permission
    } else {
#pragma clang diagnostic push
#pragma clang diagnostic ignored"-Wdeprecated-declarations"
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes: (UIRemoteNotificationTypeAlert | UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound)];
        savedReturnObject[@"asked"] = @YES;
        savedReturnObject[@"granted"] = @YES;
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:savedReturnObject] callbackId:command.callbackId];
#pragma clang diagnostic pop
    }
}

/**
  打开App设置页面
 */
- (void)openAppSettings:(CDVInvokedUrlCommand*)command{
    if ([[UIDevice currentDevice].systemVersion floatValue] >= 10.0f) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString] options:@{} completionHandler:^(BOOL success) {}];
    }else{
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]];
    }
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}


/**
 * 启动阿里云推送服务
 */
- (void)boot:(CDVInvokedUrlCommand*)command{
    [AliyunPushDelegate boot:^(BOOL result, id response) {
        if(result) {
            [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
        } else {
            [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:response] callbackId:command.callbackId];
        }
    }];
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
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    }

    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

/**
 * 阿里云推送绑定账号名
 */
- (void)bindAccount:(CDVInvokedUrlCommand*)command{

    NSString* account = [command.arguments objectAtIndex:0];

    if(account.length != 0){

        [[AliyunNotificationLauncher sharedAliyunNotificationLauncher] bindAccountWithAccount:account andCallback:^(BOOL result, id response) {

            CDVPluginResult *cdvresult;

            if(result){
                cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            }else{
                cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:response];
            }

            [self.commandDelegate sendPluginResult:cdvresult callbackId:command.callbackId];

        }];
    }

}

/**
 * 阿里云推送账号解绑
 */
- (void)unbindAccount:(CDVInvokedUrlCommand*)command{

    [[AliyunNotificationLauncher sharedAliyunNotificationLauncher] unbindAccountAndCallback:^(BOOL result, id response) {

        CDVPluginResult *cdvresult;

        if(result){
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }else{
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:response];
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

    [[AliyunNotificationLauncher sharedAliyunNotificationLauncher] bindTagsWithTags:target :tags :alias andCallback:^(BOOL result, id response) {
        CDVPluginResult *cdvresult;

        if(result){
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }else{
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:response];
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

    [[AliyunNotificationLauncher sharedAliyunNotificationLauncher] unbindTagsWithTags:target :tags :alias andCallback:^(BOOL result, id response) {

        CDVPluginResult *cdvresult;

        if(result){
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }else{
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:response];
        }

        [self.commandDelegate sendPluginResult:cdvresult callbackId:command.callbackId];

    }];

}

/**
 *查询标签
 */
- (void)listTags:(CDVInvokedUrlCommand*)command{

    [[AliyunNotificationLauncher sharedAliyunNotificationLauncher] listTagsAndCallback:^(BOOL result, id response) {

        CDVPluginResult *cdvresult;

        if(result){

            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:response];
        }else{
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:response];
        }

        [self.commandDelegate sendPluginResult:cdvresult callbackId:command.callbackId];

    }];

}


- (void)addAlias:(CDVInvokedUrlCommand*)command{
    NSString* aliases = [command.arguments objectAtIndex:0];
    if(aliases.length != 0){
        [[AliyunNotificationLauncher sharedAliyunNotificationLauncher]
         addAlias:aliases andCallback:^(BOOL result, id response) {
            CDVPluginResult *cdvresult;
            if(result){
                cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            }else{
                cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:response];
            }
            [self.commandDelegate sendPluginResult:cdvresult callbackId:command.callbackId];
        }];
    }
}

- (void)removeAlias:(CDVInvokedUrlCommand*)command{
    NSString* aliases = [command.arguments objectAtIndex:0];
    
    // bugfix: cordova throw [NSNull length] error when aliases is null 
    if([aliases isEqual:[NSNull null]]) { aliases = @""; }
    
    [[AliyunNotificationLauncher sharedAliyunNotificationLauncher]
        removeAlias:aliases andCallback:^(BOOL result, id response) {
        CDVPluginResult *cdvresult;

        if(result){
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }else{
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:response];
        }

        [self.commandDelegate sendPluginResult:cdvresult callbackId:command.callbackId];
    }];
}

- (void)listAliases:(CDVInvokedUrlCommand*)command{
    [[AliyunNotificationLauncher sharedAliyunNotificationLauncher]
     listAliases:^(BOOL result, id response) {

        CDVPluginResult *cdvresult;

        if(result){
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:response];
        }else{
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:response];
        }

        [self.commandDelegate sendPluginResult:cdvresult callbackId:command.callbackId];

    }];
}

- (void)syncBadgeNum:(CDVInvokedUrlCommand*)command {
    NSString* stringNum = [command.arguments objectAtIndex:0];
    NSUInteger badgeNum = [stringNum integerValue];
    
    [[AliyunNotificationLauncher sharedAliyunNotificationLauncher]
     syncBadgeNum:badgeNum andCallback:^(BOOL result, id response) {
        CDVPluginResult *cdvresult;

        if(result){
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }else{
            cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:response];
        }

        [self.commandDelegate sendPluginResult:cdvresult callbackId:command.callbackId];
    }];
    
}

- (void)setBadgeNum:(CDVInvokedUrlCommand*)command{
    
    NSString* stringNum = [command.arguments objectAtIndex:0];
    NSUInteger badgeNum = [stringNum integerValue];

    UIApplication *app = [UIApplication sharedApplication];
    app.applicationIconBadgeNumber = badgeNum;

    CDVPluginResult *cdvresult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];

    [self.commandDelegate sendPluginResult:cdvresult callbackId:command.callbackId];
}

@end
