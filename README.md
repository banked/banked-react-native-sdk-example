# banked-react-native-sdk-example

Here is an example project on how to integrate the existing native Android and iOS SDKs into a React Native app.

## Android Integration

1. Add the latest gradle dependency into the Android app module build file

```
implementation "com.banked:checkout:2.0.1-beta12"
```

2. Add an intent filter into the application manin activity so that the SDK can receive events from the banking providers
```
</intent-filter>
    <intent-filter>
    <action android:name="android.intent.action.VIEW" />

    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />

    <data
        android:host="<host>"
        android:scheme="<scheme>" />
    </intent-filter>
```

3. Create a ```ReactContextBaseJavaModule``` to bridge between the React Native code and call into the Banked SDK Android code
```
public class BankedSdkModule extends ReactContextBaseJavaModule {

    public BankedSdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @NonNull
    @Override
    public String getName() {
        return "BankedSdk";
    }

    @ReactMethod
    void initialise(String apiKey) {
        Banked.setApiKey(apiKey);
    }

    @ReactMethod
    void openBankedSdk(String paymentId, String continueUrl) {
        final FragmentActivity activity = (FragmentActivity) getCurrentActivity();
        Banked.startPayment(activity, paymentId, continueUrl);
    }
}
```

4. Create a ```ReactPackage``` to register and use the custom ```ReactContextBaseJavaModule``` class above
```
public class BankedReactPackage implements ReactPackage {

    @NonNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new BankedSdkModule(reactContext));
        return modules;
    }

    @NonNull
    @Override
    public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }
}
```

5. Register the ```ReactPackage``` in the ```ReactNativeHost``` declared in the Android ```Application``` class.
```
private final ReactNativeHost mReactNativeHost =
      new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
          return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
          @SuppressWarnings("UnnecessaryLocalVariable")
          List<ReactPackage> packages = new PackageList(this).getPackages();
          packages.add(new BankedReactPackage());
          return packages;
        }

        @Override
        protected String getJSMainModuleName() {
          return "index";
        }
      };
```

6. Add the Banked SDK lifecycle callback and add the payment session listener in your MainActivity
```
public class MainActivity extends ReactActivity implements OnPaymentSessionListener {

    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "BankedReactNativeExample";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Banked.setOnPaymentSessionListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Banked.onStart(this);
    }

    @Override
    public void onPaymentFailed(PaymentResult paymentResult) {
        displayJavascriptAlert("Payment failed");
    }

    @Override
    public void onPaymentSuccess(PaymentResult paymentResult) {
        displayJavascriptAlert("Payment success");
    }
    
    @Override
    public void onPaymentAborted() {
        Log.d("Banked", "onPaymentAborted");
    }

    private void displayJavascriptAlert(String message) {
        MainApplication application = (MainApplication) getApplication();
        ReactNativeHost reactNativeHost = application.getReactNativeHost();
        ReactInstanceManager reactInstanceManager = reactNativeHost.getReactInstanceManager();
        ReactContext reactContext = reactInstanceManager.getCurrentReactContext();

        if (reactContext != null) {
            CatalystInstance catalystInstance = reactContext.getCatalystInstance();
            WritableNativeArray params = new WritableNativeArray();
            params.pushString(message);
            catalystInstance.callFunction("JavaScriptExposedAlert", "alert", params);
        }
    }
}
```

More information on how to integrate Android SDK in an application can be found at https://github.com/banked/banked-android-sdk-examples

## iOS Integration

1. Use [Cocoapods](https://cocoapods.org/) to install the Banked Checkout SDK

To integrate Banked Checkout SDK into your Xcode project using CocoaPods, specify it in your Podfile:

```swift
pod ‘Banked’
```
**Note**: Banked is dynamic framework (with some dependecies, like Sentry) but Cocoapods builds pods by default as static libraries. To use a framework like this we’d normally use the ``use_frameworks!``. However, this causes all pods to be compiled as dynamic frameworks which won't work for all React Native pods. To avoid this you can use plugin like [cocoapods-user-defined-build-types](https://github.com/joncardasis/cocoapods-user-defined-build-types)

Full explanation of this problem here: [Swift Dynamic Frameworks & React Native] (https://medium.com/@joncardasis/swift-dynamic-frameworks-react-native-3d77c4972f32)

You will have to modify your ``Gemfile`` in the root folder and add something like ``gem 'cocoapods-user-defined-build-types'``. Then run ``bundle install``.

Add this to the top of your ``Podfile``:
```
plugin 'cocoapods-user-defined-build-types'
enable_user_defined_build_types!
```
And to use the proper pod add:
```
pod 'Banked', :build_type => :dynamic_framework
```
Because Banked is an XCFramework, we also need iOS13, so in your Podfile add this: ``platform :ios, '13.0'``.

2. Create ``BankedCheckoutWrapper.swift`` to bridge between Banked iOS SDK and Objective-C 

```
import Foundation
import UIKit
import Banked

@objc class BankedCheckoutWrapper: NSObject {
     
  @objc static let shared: BankedCheckoutWrapper = BankedCheckoutWrapper()
  
  @objc func setUp(apiKey: String) {
    BankedCheckout.shared.setUp(apiKey)
  }
  
  @objc func presentCheckout(viewController: UIViewController ,paymentId: String, continueURL: String) {
    BankedCheckout.shared.presentCheckout(viewController , paymentId: paymentId, action: .pay, continueURL: continueURL) { response in
      switch response {
      case .success:
        print("success")
      case .failure(let error):
        print("error \(error)")
      }
    }
  }
  
  @objc func handlePayment(url: URL) {
    
    BankedCheckout.shared.handlePaymentWithURL(url, action: .pay) { response in
      switch response {
      case .success:
        print("success")
      case .failure(let error):
        print("error \(error)")
      }
    }
  }
}

```

3. Create ``BankedSdk`` Objective-C class to bridge between the React Native code and call into the ``BankedCheckoutWrapper``. 

Add the following to BankedSdk.h: 

```
#import <Foundation/Foundation.h>
#import "React/RCTBridge.h"

NS_ASSUME_NONNULL_BEGIN

@interface BankedSdk : NSObject <RCTBridgeModule>

@end

NS_ASSUME_NONNULL_END

```

Next up, implemennt the native module as follows: 

```
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

```
