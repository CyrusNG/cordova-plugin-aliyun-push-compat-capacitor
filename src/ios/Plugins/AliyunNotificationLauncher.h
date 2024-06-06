//
//  SevenAppNotificationLauncher.h
//  SevenPush
//
//  Created by ☺strum☺ on 2018/4/10.
//  Copyright © 2018年 zpk. All rights reserved.
//
#import <Cordova/CDV.h>
#import <Cordova/CDVConfigParser.h>
#import <Foundation/Foundation.h>
#import <UserNotifications/UserNotifications.h>

@interface AliyunNotificationLauncher : NSObject<UNUserNotificationCenterDelegate>

+ (id)sharedAliyunNotificationLauncher;

- (void)didFinishLaunchingWithOptions:(NSDictionary *)launchOptions andApplication:(UIApplication *)application;

#pragma mark - application notification delegate

- (void)didReceiveRemoteNotification:(NSDictionary *)userInfo andApplication:(UIApplication *)application;

#pragma mark - 绑定信息

- (NSString *)getDeviceId;

#pragma mark - 程序关闭 点击通知h进入
- (NSDictionary *)getRemoteInfo;

- (void)bindAccountWithAccount:(NSString *)account andCallback:(void (^)(BOOL result))callback;

- (void)bindTagsWithTags: (int )target :(NSArray *)tags :(NSString *)alias andCallback:(void (^)(BOOL result))callback;

- (void)unbindTagsWithTags:(int )target :(NSArray *)tags :(NSString *)alias andCallback:(void (^)(BOOL result))callback;

- (void)listTagsAndCallback:(void (^)(id result))callback;

- (void)unbindAccountAndCallback:(void (^)(BOOL result))callback;

- (void)addAlias:(NSString *)alias andCallback:(void (^)(BOOL result))callback;

- (void)removeAlias:(NSString *)alias andCallback:(void (^)(BOOL result))callback;

- (void)listAliases:(void (^)(id result))callback;

- (void)syncBadgeNum:(NSUInteger *)badgeNum andCallback:(void (^)(BOOL result))callback;

@end
