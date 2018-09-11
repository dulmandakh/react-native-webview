package com.reactnativecommunity.webview

import android.webkit.WebView

/**
 * Implement this interface in order to config your {@link WebView}. An instance of that
 * implementation will have to be given as a constructor argument to {@link RCTWebViewManager}.
 */
interface WebViewConfig {
  fun configWebView(webView: WebView)
}
