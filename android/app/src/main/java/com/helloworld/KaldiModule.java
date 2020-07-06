package com.helloworld;

import android.os.AsyncTask;

import android.Manifest;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;

import com.facebook.react.uimanager.IllegalViewOperationException;
import com.facebook.react.bridge.JSApplicationIllegalArgumentException;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONObject;
import org.kaldi.Assets;
import org.kaldi.KaldiRecognizer;
import org.kaldi.Model;
import org.kaldi.RecognitionListener;
import org.kaldi.SpeechRecognizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import javax.annotation.Nullable;

import java.util.Random;

public class KaldiModule  extends ReactContextBaseJavaModule implements RecognitionListener {
    private static ReactApplicationContext reactContext;
    static final String REACT_NAME = "KaldiExample";
    private Promise mVoicepromise;
    private static boolean DEBUG = true;

    public static final String ACTIVITY_DOES_NOT_EXIST = "ACTIVITY_DOES_NOT_EXIST";

    static {
        System.loadLibrary("kaldi_jni");
    }

    static private final int STATE_START = 0;
    static private final int STATE_READY = 1;
    static private final int STATE_FILE  = 2;
    static private final int STATE_MIC   = 3;
    static private final int STATE_WAITING = 4;
    static private final int STATE_CLOSED = 5;

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    static private Model model;
    private SpeechRecognizer recognizer;

    public KaldiModule(ReactApplicationContext context) {
        super(context);
        this.reactContext = context;
    }

    @Override
    public String getName() {
        return REACT_NAME;
    }

    /**
     * RecognitionListener
     */
    @Override
    public void onTimeout() {
        recognizer.cancel();
        recognizer = null;
        setUiState(STATE_READY);
    }

    /**
     * RecognitionListener
     */
    @Override
    public void onError(Exception e) {
        setErrorState(e.getMessage());
    }

    /**
     * RecognitionListener
     */
    @Override
    public void onResult(String hypothesis) {
        try {
            JSONObject j = new JSONObject(hypothesis);
            //transcriptView.append(j.getString("text") + " ");
            setDebugState("text: " + j.getString("text"));
        } catch (Exception e) {
            //resultView.append("error converting to json vvvvvv");
            setErrorState("error converting to json vvvvvv");
        }
        //resultView.append("result: " + hypothesis + "\n");
        setDebugState("result: " + hypothesis);
    }

    /**
     * RecognitionListener
     */
    @Override
    public void onPartialResult(String hypothesis) {
       // resultView.append("partial: " + hypothesis + "\n");
       setDebugState("partial: " + hypothesis);
    }

    @ReactMethod
    public boolean destroy(Promise promise) {

        boolean canceled = false;
        
        if (recognizer != null) {
            canceled = recognizer.cancel();
            recognizer.shutdown();
            setUiState(STATE_CLOSED);
        }
        return canceled;
    }

    /**
     * stop method stops recognizer's thread and posts final results using main handler.
     */
    @ReactMethod
    public boolean stop(Promise promise) {
        if (recognizer != null) {
            setUiState(STATE_READY);
            return recognizer.stop();
        }

        return false;
    }

    /**
     * cancel method is similar to stop method but it stops recognizer's thread without posting final results
     */
    @ReactMethod
    public boolean cancel(Promise promise) {
        if (recognizer != null) {
            setUiState(STATE_READY);
            return recognizer.cancel();
        }

        return false;
    }

    @ReactMethod
    public void init(int permission, Promise promise) {
        try {
            
            Activity currentActivity = getCurrentActivity();
            if (currentActivity == null) {
                promise.reject(ACTIVITY_DOES_NOT_EXIST);
                return;
            }

            setUiState(STATE_START);
            
            // TODO
            // Check if user has given permission to record audio
            int permissionCheck = ContextCompat.checkSelfPermission(reactContext, Manifest.permission.RECORD_AUDIO);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(currentActivity, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
                promise.reject("Permission for record audio denied");
                return;
            }

            new SetupTask(currentActivity).execute();
        } catch (Exception e) {
            promise.reject(new JSApplicationIllegalArgumentException(e.getMessage()));
        }
    }

    @ReactMethod
    public void recognizeMicrophone(Promise promise) {
        setDebugState("waiting for recognize microphone");

        recognizeMicrophone();
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<Activity> activityReference;

        SetupTask(Activity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Exception doInBackground(Void... params) {
            setDebugState("doInBackground: " + activityReference.get());
            String assetDirStr = "";
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                if(assetDir != null) assetDirStr = assetDir.toString(); 
                setDebugState("!!!!" + assetDir.toString());
                //Log.d("!!!!", assetDir.toString());
                model = new Model(assetDir.toString() + "/model-android");
            } catch (IOException e) {
                setErrorState(String.format("Asset Directory: %s ; Failed to init model %s", assetDirStr, e.getMessage()));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
                setErrorState(String.format("Failed to init recognizer %s", result));
            } else {
                setUiState(STATE_READY);
            }
        }
    }

    public void recognizeMicrophone() {
        if (recognizer != null) {
            setDebugState("recognizer != null");
            setUiState(STATE_READY);
            recognizer.cancel();
            recognizer = null;
        } else {
            setDebugState("recognizer == null");
            setUiState(STATE_MIC);
            try {
                recognizer = new SpeechRecognizer(model);
                recognizer.addListener(this);
                recognizer.startListening();
                setDebugState("recognizer started listening...");
            } catch (IOException e) {
                setErrorState(e.getMessage());
            }
        }
    }

    /***
     * This Method demonstrates passing some string from Java to ReactNative
     * @return
     */
    @ReactMethod
    public void getSomeVariableFromJava(Callback errorCallback, Callback successCallback) {
        try { 
            Random rand = new Random(); //instance of random class
            int upperbound = 25;
            //generate random values from 0-24
            int intRandom = rand.nextInt(upperbound); 
            successCallback.invoke("random number from Kaldi module: " + intRandom);
        } catch (IllegalViewOperationException e) {
        errorCallback.invoke(e.getMessage());
        }
    }

    private static void setUiState(int state) {
        WritableMap params = Arguments.createMap();

        switch (state) {
            case STATE_START:
                params.putString("eventProperty", "STATE START");
                break;
            case STATE_READY:
                params.putString("eventProperty", "STATE READY");
                break;
            case STATE_FILE:
                params.putString("eventProperty", "STARTING");
                break;
            case STATE_MIC:
                params.putString("eventProperty", "STOP MICROPHONE");
                break;
            case STATE_WAITING:
                params.putString("eventProperty", "STATE WAITING");
                break;
        }

        sendEvent("uistate", params);
    }

    private static void setDebugState(String message) {
        if(DEBUG) {
            WritableMap params = Arguments.createMap();
            params.putString("eventProperty", message);
            sendEvent("uistate", params);
        }
    }

    private static void setErrorState(String message) {
        WritableMap params = Arguments.createMap();
        params.putString("eventProperty", message);
        sendEvent("errorstate", params);
    }

    private static void sendEvent(String eventName,
                            @Nullable WritableMap params) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
    }
    
}