package com.bankedreactnativeexample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.banked.checkout.Banked;
import com.banked.checkout.OnPaymentSessionListener;
import com.banked.checkout.feature.status.model.PaymentResult;
import com.facebook.react.ReactActivity;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableNativeArray;

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
    public void onPaymentFailed(@NonNull PaymentResult paymentResult) {
        Log.d("Banked", "onPaymentFailed!");
        displayJavascriptAlert("Payment failed");
    }

    @Override
    public void onPaymentSuccess(@NonNull PaymentResult paymentResult) {
        Log.d("Banked", "onPaymentSuccess - " + paymentResult.toString());
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
