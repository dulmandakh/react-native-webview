package com.reactnativecommunity.webview

import android.webkit.JavascriptInterface

class RCTWebViewBridge(private val context: RCTWebView) {
    @JavascriptInterface
    fun postMessage(message: String) {
        context.onMessage(message)
    }
}