package com.txwstudio.app.wifilinkspeed

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var mHandler: Handler? = null
    val updateInterval = 1000
    private var floatWindowStatus = false
    private val requestCodeOverlay = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar()
        setOnClickListener()

        getWifiInfo()
        mHandler = Handler()
    }

    override fun onResume() {
        super.onResume()
        startRepeatingTask()
    }

    override fun onPause() {
        super.onPause()
        stopRepeatingTask()
    }


    /** App Initial */
    private fun setupTheme() {
        setTheme(R.style.AppTheme_NoActionBar)

        // Fix white status bar when sdk <= 21
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.darkMode_colorPrimary)
        }
    }

    private fun setSupportActionBar() {
        setSupportActionBar(toolbar_main)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setOnClickListener() {
        cardview_main_ssid.setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
        }

        cardview_main_openspeedtest.setOnClickListener {
            var intent = packageManager.getLaunchIntentForPackage("org.zwanoo.android.speedtest")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                intent = Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.data = Uri.parse("market://details?id=org.zwanoo.android.speedtest")
                startActivity(intent)
            }
        }

        cardview_main_openfastcom.setOnClickListener {
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(this, Uri.parse("https://fast.com"))
        }

        cardview_main_moreInfo.setOnClickListener {
            Toast.makeText(this, "cardview_main_moreInfo", Toast.LENGTH_SHORT).show()
        }

        cardview_main_floatWindow.setOnClickListener {
            if (checkOverlayPermission()) {
                if (floatWindowStatus) {
                    floatWindowStatus = false
                    textview_floatWindow_value.text = "Disable"

                    stopService(Intent(this, FloatWindowService::class.java))
                } else if (!floatWindowStatus) {
                    floatWindowStatus = true
                    textview_floatWindow_value.text = "Enable"

                    startService(Intent(this, FloatWindowService::class.java))

                }
            }
        }
    }

    private fun checkOverlayPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        if (!Settings.canDrawOverlays(this)) {
            val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            myIntent.data = Uri.parse("package:$packageName")
            startActivityForResult(myIntent, requestCodeOverlay)
            return false
        }
        return true
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCodeOverlay) {
            if (Settings.canDrawOverlays(this)) {
                startService(Intent(this, FloatWindowService::class.java))
            }
        }
    }

    /**
     * Get current wifi info then set to text.
     * */
    private fun getWifiInfo() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info: WifiInfo = wifiManager.connectionInfo

        textview_ssid_value.text = info.ssid.substring(1, info.ssid.length - 1)
        textview_rssi_value.text = info.rssi.toString() + " dBm"
        textview_linkspeed_value.text = info.linkSpeed.toString() + " Mbps"
    }


    /**
     * A repeater use to update wifi info in real-time.
     * */
    private var repeater: Runnable = object : Runnable {
        override fun run() {
            try {
                getWifiInfo()
            } finally {
                mHandler!!.postDelayed(this, updateInterval.toLong())
            }
        }
    }

    private fun startRepeatingTask() = repeater.run()
    private fun stopRepeatingTask() = mHandler?.removeCallbacks(repeater)

}
