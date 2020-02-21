package com.txwstudio.app.wifilinkspeed

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.WindowManager
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mHandler: Handler? = null
    private val updateInterval = 1000

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

        cardview_main_openspeedtest.setOnClickListener{
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
    }
