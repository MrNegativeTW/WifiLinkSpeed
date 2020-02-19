package com.txwstudio.app.wifilinkspeed

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var ssid: TextView? = null
    private var rssi: TextView? = null
    private var linkSpeed: TextView? = null
    private var mHandler: Handler? = null
    private val updateInterval = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById()

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


    private fun findViewById() {
        ssid = findViewById(R.id.textview_ssid_value)
        rssi = findViewById(R.id.textview_status_value)
        linkSpeed = findViewById(R.id.textview_linkspeed_value)
    }

    private fun getWifiInfo() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info: WifiInfo = wifiManager.connectionInfo

        ssid?.text = info.ssid
        rssi?.text = info.rssi.toString()
        linkSpeed?.text = info.linkSpeed.toString()
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

    fun startRepeatingTask() {
        mStatusChecker.run()
    }

    fun stopRepeatingTask() {
        mHandler?.removeCallbacks(mStatusChecker)
    }


    fun openSpeedtest(v: View) {
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

    fun openFastCom(v: View) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://fast.com"))
        startActivity(intent)
    }
}