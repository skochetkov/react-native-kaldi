package com.helloworld;

import android.widget.Toast;
import com.facebook.react.bridge.Callback;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import javax.annotation.Nullable;

public class ToastModule extends ReactContextBaseJavaModule {
  private static ReactApplicationContext reactContext;

  private static final String DURATION_SHORT_KEY = "SHORT";
  private static final String DURATION_LONG_KEY = "LONG";

  ToastModule(ReactApplicationContext context) {
    super(context);
    reactContext = context;
  }

  @Override
  public String getName() {
    return "ToastExample";
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put(DURATION_SHORT_KEY, Toast.LENGTH_SHORT);
    constants.put(DURATION_LONG_KEY, Toast.LENGTH_LONG);
    return constants;
  }

  /***
   * This method demostrates using Andorid Toast to show some text on ReactNative side
   * @param message
   * @param duration
   */
  @ReactMethod
  public void show(String message, int duration) {
    Toast.makeText(getReactApplicationContext(), message, duration).show();
  }

  /***
   * This Method demonstrates passing some string from Java to ReactNative
   * @return
   */
  @ReactMethod
  public void getSomeVariable(Callback errorCallback, Callback successCallback) {
    try { 
      Random rand = new Random(); //instance of random class
      int upperbound = 25;
        //generate random values from 0-24
      int intRandom = rand.nextInt(upperbound); 
      successCallback.invoke("random number from java: " + intRandom);
    } catch (IllegalViewOperationException e) {
      errorCallback.invoke(e.getMessage());
    }
  }

  /***
   * This Method demonstrates using DeviceEventManagerModule to emit events 
   * @return
   */
  @ReactMethod
  public void getSomeEvent(String reactNativeInput) {
    WritableMap params = Arguments.createMap();
    params.putString("eventProperty", "you input is: " + reactNativeInput);

    sendEvent(reactContext, "EventReminder", params);
  }  

  private void sendEvent(ReactContext reactContext,  String eventName,  @Nullable WritableMap params) {
    reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
  }
}