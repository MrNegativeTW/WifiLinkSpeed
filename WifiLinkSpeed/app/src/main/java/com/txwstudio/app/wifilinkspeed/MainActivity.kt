package com.txwstudio.app.wifilinkspeed

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.WindowManager
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val toolBar: Toolbar ?= null
    private var mHandler: Handler? = null
    private val updateInterval = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar()

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
        toolBar?.findViewById<Toolbar>(R.id.toolbar_main)
        setSupportActionBar(toolBar)
    }


    private fun getWifiInfo() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info: WifiInfo = wifiManager.connectionInfo

        textview_ssid_value.text = info.ssid.substring(1, info.ssid.length - 1)
        textview_rssi_value.text = info.rssi.toString() + " dBm"
        textview_linkspeed_value.text = info.linkSpeed.toString()+ " Mbps"
    }

    var mStatusChecker: Runnable = object : Runnable {
        override fun run() {
            try {
                getWifiInfo()
            } finally {
                mHandler!!.postDelayed(this, updateInterval.toLong())
            }
        }
    }

    private fun startRepeatingTask() {
        mStatusChecker.run()
    }

    private fun stopRepeatingTask() {
        mHandler?.removeCallbacks(mStatusChecker)
    }


    fun openSpeedTest(view: View) {
        var intent = packageManager.getLaunchIntentForPackage("org.zwanoo.android.speedtest")
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            intent = Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setData(Uri.parse("market://details?id=org.zwanoo.android.speedtest"))
            startActivity(intent)
        }
    }

    fun openFastCom(view: View) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://fast.com"))
        startActivity(intent)
    }

    fun openWifiSettings(view: View) {
        startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
    }
}