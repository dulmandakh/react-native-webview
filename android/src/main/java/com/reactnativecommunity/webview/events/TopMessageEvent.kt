package com.reactnativecommunity.webview.events

import com.facebook.react.bridge.Arguments
import com.facebook.react.uimanager.events.Event
import com.facebook.react.uimanager.events.RCTEventEmitter

/**
 * Event emitted when there is an error in loading.
 */
class TopMessageEvent(viewId: Int, private val eventData: String): Event<TopMessageEvent>(viewId) {
  companion object {
    val EVENT_NAME = "topMessage"
  }

  override fun getEventName() = EVENT_NAME

  override fun canCoalesce() = false

  override fun getCoalescingKey() = 0.toShort()

  override fun dispatch(rctEventEmitter: RCTEventEmitter) {
    val data = Arguments.createMap()
    data.putString("data", eventData)
    rctEventEmitter.receiveEvent(viewTag, eventName, data)
  }
}
