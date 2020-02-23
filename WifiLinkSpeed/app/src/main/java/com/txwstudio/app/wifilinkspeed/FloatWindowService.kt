package com.txwstudio.app.wifilinkspeed

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast


class FloatWindowService: Service() {

    private var windowManager: WindowManager? = null
    private var params: WindowManager.LayoutParams? = null
    private var linearLayout: LinearLayout? = null

    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate() {
        super.onCreate()

        Toast.makeText(this, "Service onCreate", Toast.LENGTH_SHORT).show()

        // Initial
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        linearLayout = LinearLayout(this)

        // Create Layout
        val linearLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        linearLayout?.setBackgroundColor(Color.argb(125, 200, 200, 200))
        linearLayout?.layoutParams = linearLayoutParams


        /**
         * Setup WindowManager
         * TYPE_SYSTEM_OVERLAY deprecated in API level 26 (Oreo).
         * */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = WindowManager.LayoutParams(50, 50,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT)
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1){
            params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    PixelFormat.TRANSLUCENT)
        }
        params?.gravity = Gravity.END or Gravity.BOTTOM
        params?.title = "Title Here"


        windowManager?.addView(linearLayout, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager?.removeViewImmediate(linearLayout)
        Toast.makeText(this, "Service onDestroy", Toast.LENGTH_SHORT).show()
    }
}