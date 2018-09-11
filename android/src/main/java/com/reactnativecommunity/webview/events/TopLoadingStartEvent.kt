package com.reactnativecommunity.webview.events;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

/**
 * Event emitted when loading has started
 */
class TopLoadingStartEvent(viewId: Int, private val eventData: WritableMap): Event<TopLoadingStartEvent>(viewId) {

  companion object {
    val EVENT_NAME = "topLoadingStart"
  }

  override fun getEventName() = EVENT_NAME

  override fun canCoalesce() = false

  override fun getCoalescingKey() = 0.toShort()

  override fun dispatch(rctEventEmitter: RCTEventEmitter) = rctEventEmitter.receiveEvent(viewTag, eventName, eventData)
}
