package com.reactnativecommunity.webview;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.ReactApplicationContext;

class RCTWebViewPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext) = arrayListOf(
        RCTWebViewModule(reactContext)
    )

    override fun createViewManagers(reactContext: ReactApplicationContext) = arrayListOf(
        RCTWebViewManager()
    )
}