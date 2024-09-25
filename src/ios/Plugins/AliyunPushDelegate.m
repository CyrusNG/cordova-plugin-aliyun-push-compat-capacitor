//
//  AppDelegate+AliyunPush.m
//  SevenCordova
//
//  Created by ☺strum☺ on 2018/4/24.
//

#import "AliyunPushDelegate.h"
#import "AliyunNotificationLauncher.h"
#import <objc/runtime.h>


@implementation AliyunPushDelegate

static AliyunPushDelegate* _instance = nil;

+(AliyunPushDelegate*)getInstance {
    if (!_instance) {
        _instance = [[AliyunPushDelegate alloc] init];
    }
    return _instance;
}

+(void)load {
    AliyunPushDelegate* _self = [AliyunPushDelegate getInstance];
    [[NSNotificationCenter defaultCenter] addObserver:_self
                                             selector:@selector(applicationDidFinishLaunching:)
                                                 name:UIApplicationDidFinishLaunchingNotification object:nil];
}

+(void)boot:(void (^)(BOOL result, id response))callback {
    // 绑定事件
    AliyunPushDelegate* _self = [AliyunPushDelegate getInstance];
    [[NSNotificationCenter defaultCenter] addObserver:_self
                                             selector:@selector(applicationDidRegisterForRemoteNotifications:)
                                                 name:@"CDApplicationDidRegisterForRemoteNotificationsNotification" object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:_self
                                             selector:@selector(applicationDidFailToRegisterForRemoteNotifications:)
                                                 name:@"CDApplicationDidFailToRegisterForRemoteNotificationsNotification" object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:_self
                                             selector:@selector(applicationDidReceiveRemoteNotification:)
                                                 name:@"CDApplicationDidReceiveRemoteNotificationNotification" object:nil];
    // 初始化SDK
    [[AliyunNotificationLauncher sharedAliyunNotificationLauncher] initCloudPush: [UIApplication sharedApplication] callback: callback];
}

- (void)applicationDidFinishLaunching:(NSNotification *)notification
{
    if (notification)
    {
        UIApplication *application  = (UIApplication *)notification.object;
        NSDictionary *launchOptions = [notification userInfo];
        NSLog(@"category sharedAliyunNotificationLauncher ");
        [[AliyunNotificationLauncher sharedAliyunNotificationLauncher] didFinishLaunchingWithOptions:launchOptions andApplication:application];
    }
}


/*
 *  APNs注册成功回调，将返回的 deviceToken 上传到 CloudPush 服务器
 */
- (void)applicationDidRegisterForRemoteNotifications:(NSNotification *)notification {
    NSData *deviceToken = (NSData *)notification.object;
    NSLog(@"Upload deviceToken to CloudPush server.");
    [CloudPushSDK registerDevice:deviceToken withCallback:^(CloudPushCallbackResult *res) {
        if (res.success) {
            NSLog(@"Register deviceToken success, deviceToken: %@", [CloudPushSDK getApnsDeviceToken]);
        } else {
            NSLog(@"Register deviceToken failed, error: %@", res.error);
        }
    }];
}

- (void)applicationDidReceiveRemoteNotification:(NSNotification *)notification {
    UIApplication *application = (UIApplication*)notification.object;
    NSDictionary *userInfo = [notification userInfo];
    [[AliyunNotificationLauncher sharedAliyunNotificationLauncher] didReceiveRemoteNotification:userInfo andApplication:application];
}

- (void)applicationDidFailToRegisterForRemoteNotifications:(NSNotification *)notification {
    NSError *error = (NSError*)notification.object;
    NSLog(@"Register APNs failed, error: %@", error);
}

@end
