package com.bankedreactnativeexample;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.banked.checkout.Banked;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

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
