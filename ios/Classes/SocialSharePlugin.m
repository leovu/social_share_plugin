#import "SocialSharePlugin.h"
//#import <TwitterKit/TWTRKit.h>

@implementation SocialSharePlugin {
    FlutterMethodChannel* _channel;
    UIDocumentInteractionController* _dic;
    FlutterResult _result;
}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"social_share_plugin"
            binaryMessenger:[registrar messenger]];
  SocialSharePlugin* instance = [[SocialSharePlugin alloc] initWithChannel:channel];
  [registrar addApplicationDelegate:instance];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (instancetype)initWithChannel:(FlutterMethodChannel*)channel {
    self = [super init];
    if(self) {
        _channel = channel;
    }
    return self;
}

 - (BOOL)application:(UIApplication *)application
     didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
   return YES;
 }

- (BOOL)application:(UIApplication *)application
            openURL:(NSURL *)url
            options:
                (NSDictionary<UIApplicationOpenURLOptionsKey, id> *)options {
   return YES;
}

 - (BOOL)application:(UIApplication *)application
               openURL:(NSURL *)url
     sourceApplication:(NSString *)sourceApplication
            annotation:(id)annotation {
   return YES;
 }

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    _result = result;
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else if([@"shareToTwitterLink" isEqualToString:call.method]) {
      NSURL *twitterURL = [NSURL URLWithString:@"twitter://"];
      if([[UIApplication sharedApplication] canOpenURL:twitterURL]) {
          [self twitterShare:call.arguments[@"text"] url:call.arguments[@"url"]];
          result(nil);
      } else {
          NSString *twitterLink = @"itms-apps://itunes.apple.com/us/app/apple-store/id333903271";
          if (@available(iOS 10.0, *)) {
              [[UIApplication sharedApplication] openURL:[NSURL URLWithString:twitterLink] options:@{} completionHandler:^(BOOL success) {}];
          } else {
              [[UIApplication sharedApplication] openURL:[NSURL URLWithString:twitterLink]];
          }
          result(false);
      }
  } else {
    result(FlutterMethodNotImplemented);
  }
}

- (void)twitterShare:(NSString*)text
                 url:(NSString*)url {
    UIApplication* application = [UIApplication sharedApplication];
    NSString* shareString = [NSString stringWithFormat:@"https://twitter.com/intent/tweet?text=%@&url=%@", text, url];
    NSString* escapedShareString = [shareString stringByAddingPercentEncodingWithAllowedCharacters:NSCharacterSet.URLQueryAllowedCharacterSet];
    NSURL* shareUrl = [NSURL URLWithString:escapedShareString];
    if (@available(iOS 10.0, *)) {
        [application openURL:shareUrl options:@{} completionHandler:^(BOOL success) {
            if(success) {
                [self->_channel invokeMethod:@"onSuccess" arguments:nil];
                NSLog(@"Sending Tweet!");
            } else {
                [self->_channel invokeMethod:@"onCancel" arguments:nil];
                NSLog(@"Tweet sending cancelled");
            }
        }];
    } else {
        [application openURL:shareUrl];
        [self->_channel invokeMethod:@"onSuccess" arguments:nil];
        NSLog(@"Sending Tweet!");
    }
//    TWTRComposer *composer = [[TWTRComposer alloc] init];
//    [composer setText:text];
//    [composer setURL:[NSURL URLWithString:url]];
//    [composer showFromViewController:controller completion:^(TWTRComposerResult result) {
//        if (result == TWTRComposerResultCancelled) {
//            [self->_channel invokeMethod:@"onCancel" arguments:nil];
//            NSLog(@"Tweet composition cancelled");
//        }
//        else {
//            [self->_channel invokeMethod:@"onSuccess" arguments:nil];
//            NSLog(@"Sending Tweet!");
//        }
//    }];
}
@end
