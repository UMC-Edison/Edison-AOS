package com.umc.edison.common.logging

import android.util.Log
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.umc.edison.BuildConfig

object AppLogger {
    private const val PREFIX_DEBUG = "D/"
    private const val PREFIX_INFO = "I/"
    private const val PREFIX_WARN = "W/"
    private const val PREFIX_ERROR = "E/"
    private val isDebug = BuildConfig.DEBUG

    fun d(tag: String, message: String) {
        if (isDebug) Log.d(tag, message)
        Firebase.crashlytics.log("$PREFIX_DEBUG$tag: $message")
    }

    fun i(tag: String, message: String) {
        if (isDebug) Log.i(tag, message)
        Firebase.crashlytics.log("$PREFIX_INFO$tag: $message")
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
        Firebase.crashlytics.log("$PREFIX_WARN$tag: $message")
        throwable?.let { Firebase.crashlytics.recordException(it) }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        Firebase.crashlytics.log("$PREFIX_ERROR$tag: $message")
        throwable?.let { Firebase.crashlytics.recordException(it) }
    }
}
