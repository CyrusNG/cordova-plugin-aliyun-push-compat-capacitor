//
//  AppDelegate+AliyunPush.h
//  SevenCordova
//
//  Created by ☺strum☺ on 2018/4/24.
//

#import <CloudPushSDK/CloudPushSDK.h>

@interface AliyunPushDelegate: NSObject

+(AliyunPushDelegate*)getInstance;
+(void)boot;

@end
