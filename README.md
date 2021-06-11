# banked-react-native-sdk-example

Here is an example project on how to integrate the existing native Android and iOS SDKs into a React Native app.

## Android Integration

1. Add the latest gradle dependency into the Android app module build file

```
implementation "com.banked:checkout:2.0.0-rc1"
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

TODO

Information on iOS integration can be found at https://github.com/petterh/react-native-android-activity