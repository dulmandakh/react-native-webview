package com.reactnativecommunity.webview

import android.os.Build
import android.text.TextUtils
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import com.facebook.common.logging.FLog
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.common.ReactConstants
import com.facebook.react.common.build.ReactBuildConfig
import com.facebook.react.uimanager.ThemedReactContext
import com.reactnativecommunity.webview.RCTWebViewManager.BRIDGE_NAME
import com.reactnativecommunity.webview.RCTWebViewManager.dispatchEvent
import com.reactnativecommunity.webview.events.TopMessageEvent

/**
 * Subclass of [WebView] that implements [LifecycleEventListener] interface in order
 * to call [WebView.destroy] on activity destroy event and also to clear the client
 */

/**
 * WebView must be created with an context of the current activity
 *
 * Activity Context is required for creation of dialogs internally by WebView
 * Reactive Native needed for access to ReactNative internal system functionality
 *
 */
class RCTWebView(reactContext: ThemedReactContext) : WebView(reactContext), LifecycleEventListener {
    protected var injectedJS: String? = null
    private var messagingEnabled = false
    private var rctWebViewClient: RCTWebViewClient? = null

    override fun onHostResume() {
        // do nothing
    }

    override fun onHostPause() {
        // do nothing
    }

    override fun onHostDestroy() {
        cleanupCallbacksAndDestroy()
    }

    override fun setWebViewClient(client: WebViewClient?) {
        super.setWebViewClient(client)
        rctWebViewClient = client as RCTWebViewClient?
    }

    fun setInjectedJavaScript(js: String?) {
        injectedJS = js
    }

    protected fun createRCTWebViewBridge(webView: RCTWebView): RCTWebViewBridge {
        return RCTWebViewBridge(webView)
    }

    fun getRCTWebViewClient(): RCTWebViewClient? = rctWebViewClient

    fun setMessagingEnabled(enabled: Boolean) {
        if (messagingEnabled == enabled) {
            return
        }

        messagingEnabled = enabled
        if (enabled) {
            addJavascriptInterface(createRCTWebViewBridge(this), BRIDGE_NAME)
            linkBridge()
        } else {
            removeJavascriptInterface(BRIDGE_NAME)
        }
    }

    fun callInjectedJavaScript() {
        if (settings.javaScriptEnabled &&
                injectedJS != null &&
                !TextUtils.isEmpty(injectedJS)) {
            loadUrl("javascript:(function() {\n$injectedJS;\n})();")
        }
    }

    fun linkBridge() {
        if (messagingEnabled) {
            if (ReactBuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // See isNative in lodash
                val testPostMessageNative = "String(window.postMessage) === String(Object.hasOwnProperty).replace('hasOwnProperty', 'postMessage')"
                evaluateJavascript(testPostMessageNative, ValueCallback { value ->
                    if (value == "true") {
                        FLog.w(ReactConstants.TAG, "Setting onMessage on a WebView overrides existing values of window.postMessage, but a previous value was defined")
                    }
                })
            }

            loadUrl("javascript:(" +
                    "window.originalPostMessage = window.postMessage," +
                    "window.postMessage = function(data) {" +
                    BRIDGE_NAME + ".postMessage(String(data));" +
                    "}" +
                    ")")
        }
    }

    fun onMessage(message: String) {
        dispatchEvent(this, TopMessageEvent(this.getId(), message))
    }

    fun cleanupCallbacksAndDestroy() {
        setWebViewClient(null)
        destroy()
    }
}