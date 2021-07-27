#import "AndroidInAppUpdatePlugin.h"
#if __has_include(<android_in_app_update/android_in_app_update-Swift.h>)
#import <android_in_app_update/android_in_app_update-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "android_in_app_update-Swift.h"
#endif

@implementation AndroidInAppUpdatePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftAndroidInAppUpdatePlugin registerWithRegistrar:registrar];
}
@end
