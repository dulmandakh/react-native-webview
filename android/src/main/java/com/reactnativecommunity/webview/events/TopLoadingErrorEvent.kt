package com.reactnativecommunity.webview.events;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

/**
 * Event emitted when there is an error in loading.
 */
class TopLoadingErrorEvent(viewId: Int, val eventData: WritableMap): Event<TopLoadingErrorEvent>(viewId) {

  companion object {
      val EVENT_NAME = "topLoadingError"
  }

  override fun getEventName() = EVENT_NAME

  override fun canCoalesce() = false

  override fun getCoalescingKey() = 0.toShort()

  override fun dispatch(rctEventEmitter: RCTEventEmitter) = rctEventEmitter.receiveEvent(viewTag, eventName, eventData)
}
