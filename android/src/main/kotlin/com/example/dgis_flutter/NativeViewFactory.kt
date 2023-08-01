package com.example.dgis_flutter

import android.content.Context
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class NativeViewFactory(messenger: BinaryMessenger) :
    PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    private val mess = messenger


    override fun create(context: Context?, viewId: Int, args: Any?): PlatformView {

        val creationParams = args as Map<String, Any?>?
        return NativeView(context!!, viewId, creationParams, mess)
    }
}