package com.txwstudio.app.wifilinkspeed

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.view.*
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.service_float_window.*


class FloatWindowService : Service() {

    private var windowManager: WindowManager? = null
    private var params: WindowManager.LayoutParams? = null

    private var floatLayout: View? = null
    private var textViewSSID: TextView? = null
    private var textViewRSSI: TextView? = null
    private var textViewLinkSpeed: TextView? = null

    private var mHandler: Handler? = null

    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate() {
        super.onCreate()

        initWindowManager()
        initLayout()
        layoutOnTouch()

        mHandler = Handler()
        windowManager?.addView(floatLayout, params)

        startRepeatingTask()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRepeatingTask()
        windowManager?.removeView(floatLayout)
    }


    /**
     * Setup WindowManager
     * TYPE_SYSTEM_OVERLAY deprecated in API level 26 (Oreo).
     * */
    private fun initWindowManager() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        params = WindowManager.LayoutParams()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params?.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            params?.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
        }

        params?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        params?.format = PixelFormat.TRANSLUCENT

        params?.width = WindowManager.LayoutParams.WRAP_CONTENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.gravity = Gravity.CENTER
    }

    /**
     * Setup layout, get from service_float_window.xml
     * */
    private fun initLayout() {
        floatLayout = LayoutInflater.from(application).inflate(R.layout.service_float_window, null)

        textViewSSID = floatLayout?.findViewById(R.id.textview_floatWindow_ssid_value)
        textViewRSSI = floatLayout?.findViewById(R.id.textview_floatWindow_rssi_value)
        textViewLinkSpeed = floatLayout?.findViewById(R.id.textview_floatWindow_linkSpeed_value)
    }

    /**
     * Move-able overlay
     * */
    @SuppressLint("ClickableViewAccessibility")
    private fun layoutOnTouch() {
        var touchedX: Float? = null
        var touchedY: Float? = null
        var originalX: Int? = null
        var originalY: Int? = null

        floatLayout?.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        originalX = params?.x
                        originalY = params?.y

                        touchedX = event.rawX
                        touchedY = event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params?.x = (originalX!! + event.rawX - touchedX!!).toInt()
                        params?.y = (originalY!! + event.rawY - touchedY!!).toInt()

                        windowManager?.updateViewLayout(floatLayout, params)
                        return true
                    }
                }
                return false
            }
        })
    }


    private fun getWifiInfo() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info: WifiInfo = wifiManager.connectionInfo

        textViewSSID?.text = info.ssid.substring(1, info.ssid.length - 1)
        textViewRSSI?.text = "${info.rssi} dBm"
        textViewLinkSpeed?.text = "${info.linkSpeed} Mbps"
    }


    /**
     * A repeater use to update wifi info in real-time.
     * */
    private var repeater: Runnable = object : Runnable {
        override fun run() {
            try {
                getWifiInfo()
            } finally {
                mHandler!!.postDelayed(this, 1000)
            }
        }
    }

    private fun startRepeatingTask() = repeater.run()
    private fun stopRepeatingTask() = mHandler?.removeCallbacks(repeater)
}