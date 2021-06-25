//
//  BankedSdk.m
//  BankedReactNativeExample
//
//  Created by Kristina Borisova on 21/06/2021.
//

#import "BankedSdk.h"
#import "React/RCTLog.h"
#import "BankedReactNativeExample-Swift.h"

@implementation BankedSdk

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(initialise:(NSString *)apiKey)
{
  [[BankedCheckoutWrapper shared] setUpWithApiKey: apiKey];
}

RCT_EXPORT_METHOD(openBankedSdk:(NSString *)paymentId and:(NSString *)continueUrl)
{
  dispatch_async(dispatch_get_main_queue(), ^{
    UIViewController *presentedViewController = RCTPresentedViewController();
    
    [[BankedCheckoutWrapper shared] presentCheckoutWithViewController: presentedViewController  paymentId: paymentId continueURL: continueUrl];
  });
}

RCT_EXPORT_METHOD(handlePaymentForURL:(NSURL *)url)
{
  [[BankedCheckoutWrapper shared] handlePaymentWithUrl: url];
}

@end
