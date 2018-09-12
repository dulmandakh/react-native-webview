package com.reactnativecommunity.webview

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import com.facebook.common.logging.FLog
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.common.ReactConstants
import com.reactnativecommunity.webview.RCTWebViewManager.BLANK_URL
import com.reactnativecommunity.webview.RCTWebViewManager.dispatchEvent
import com.reactnativecommunity.webview.events.TopLoadingErrorEvent
import com.reactnativecommunity.webview.events.TopLoadingFinishEvent
import com.reactnativecommunity.webview.events.TopLoadingStartEvent
import java.util.regex.Pattern

class RCTWebViewClient : WebViewClient() {
    protected var mLastLoadFailed = false
    protected var mUrlPrefixesForDefaultIntent: ReadableArray? = null
    protected var mOriginWhitelist: List<Pattern>? = null

    override fun onPageFinished(webView: WebView, url: String) {
        super.onPageFinished(webView, url)

        if (!mLastLoadFailed) {
            val reactWebView = webView as RCTWebView
            reactWebView.callInjectedJavaScript()
            reactWebView.linkBridge()
            emitFinishEvent(webView, url)
        }
    }

    override fun onPageStarted(webView: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(webView, url, favicon)
        mLastLoadFailed = false

        dispatchEvent(
                webView,
                TopLoadingStartEvent(
                        webView.id,
                        createWebViewEvent(webView, url)))
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        if (url == BLANK_URL) return false

        // url blacklisting
        if (mUrlPrefixesForDefaultIntent != null && mUrlPrefixesForDefaultIntent!!.size() > 0) {
            val urlPrefixesForDefaultIntent = mUrlPrefixesForDefaultIntent!!.toArrayList()
            for (urlPrefix in urlPrefixesForDefaultIntent) {
                if (url.startsWith(urlPrefix as String)) {
                    launchIntent(view.context, url)
                    return true
                }
            }
        }

        if (mOriginWhitelist != null && shouldHandleURL(mOriginWhitelist!!, url)) {
            return false
        }

        launchIntent(view.context, url)
        return true
    }

    private fun launchIntent(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            FLog.w(ReactConstants.TAG, "activity not found to handle uri scheme for: $url", e)
        }

    }

    private fun shouldHandleURL(originWhitelist: List<Pattern>, url: String): Boolean {
        val uri = Uri.parse(url)
        val scheme = uri.scheme ?: ""
        val authority = uri.authority ?: ""
        val urlToCheck = "$scheme://$authority"
        for (pattern in originWhitelist) {
            if (pattern.matcher(urlToCheck).matches()) {
                return true
            }
        }
        return false
    }

    override fun onReceivedError(
            webView: WebView,
            errorCode: Int,
            description: String,
            failingUrl: String) {
        super.onReceivedError(webView, errorCode, description, failingUrl)
        mLastLoadFailed = true

        // In case of an error JS side expect to get a finish event first, and then get an error event
        // Android WebView does it in the opposite way, so we need to simulate that behavior
        emitFinishEvent(webView, failingUrl)

        val eventData = createWebViewEvent(webView, failingUrl)
        eventData.putDouble("code", errorCode.toDouble())
        eventData.putString("description", description)

        dispatchEvent(
                webView,
                TopLoadingErrorEvent(webView.id, eventData))
    }

    protected fun emitFinishEvent(webView: WebView, url: String) {
        dispatchEvent(
                webView,
                TopLoadingFinishEvent(
                        webView.id,
                        createWebViewEvent(webView, url)))
    }

    protected fun createWebViewEvent(webView: WebView, url: String): WritableMap {
        val event = Arguments.createMap()
        event.putDouble("target", webView.id.toDouble())
        // Don't use webView.getUrl() here, the URL isn't updated to the new value yet in callbacks
        // like onPageFinished
        event.putString("url", url)
        event.putBoolean("loading", !mLastLoadFailed && webView.progress != 100)
        event.putString("title", webView.title)
        event.putBoolean("canGoBack", webView.canGoBack())
        event.putBoolean("canGoForward", webView.canGoForward())
        return event
    }

    fun setUrlPrefixesForDefaultIntent(specialUrls: ReadableArray) {
        mUrlPrefixesForDefaultIntent = specialUrls
    }

    fun setOriginWhitelist(originWhitelist: List<Pattern>) {
        mOriginWhitelist = originWhitelist
    }
}
