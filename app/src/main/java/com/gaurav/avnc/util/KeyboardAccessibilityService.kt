package com.gaurav.avnc.util

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

typealias KeyEventListener = (event: KeyEvent) -> Boolean

class KeyboardAccessibilityService : AccessibilityService() {
    companion object {
        private const val TAG = "AVNCKeyboardService"

        var instance: KeyboardAccessibilityService? = null
    }

    private val keyEventListeners = mutableMapOf<KeyEventListener, Set<Int>>()

    fun registerKeyEventListener(listener: KeyEventListener, ignoredKeycodes: Set<Int>): Boolean {
        return keyEventListeners.put(listener, ignoredKeycodes) == null
    }

    fun unregisterKeyEventListener(listener: KeyEventListener): Boolean {
        return keyEventListeners.remove(listener) != null
    }

    fun isKeyEventHandled(event: KeyEvent, listener: KeyEventListener? = null): Boolean {
        if (listener != null) {
            val ignoredKeycodes = keyEventListeners[listener]
            return ignoredKeycodes?.contains(event.keyCode) == false
        }
        for (ignoredKeycodes in keyEventListeners.values) {
            if (ignoredKeycodes.contains(event.keyCode)) {
                return false
            }
        }
        return true
    }

    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent?) {}

    public override fun onKeyEvent(event: KeyEvent): Boolean {
        var handled = false
        for ((listener, ignoredKeycodes) in keyEventListeners) {
            if (!ignoredKeycodes.contains(event.keyCode)) {
                handled = listener.invoke(event) || handled
            }
        }
        return handled || super.onKeyEvent(event)
    }

    override fun onServiceConnected() {
        Log.i(TAG, "onServiceConnected")
        super.onServiceConnected()
        instance = this
    }

    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "onUnbind")
        instance = null
        return super.onUnbind(intent)
    }
}