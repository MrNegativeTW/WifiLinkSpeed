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
import android.os.StrictMode
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.size
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_moreinfo.view.*


class MainActivity : AppCompatActivity() {

    private var mHandler: Handler? = null
    val updateInterval = 1000
    private var floatWindowStatus = false
    private val requestCodeOverlay = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setupTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        overrideStrictMode()
        setSupportActionBar()

        setOnClickListener()

        initMobileAds()
        setupAds()

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

        // Enable Dark mode in dev
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES

        // Fix white status bar when sdk <= 21
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.darkMode_colorPrimary)
        }
    }

    private fun overrideStrictMode() {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    private fun setSupportActionBar() {
        setSupportActionBar(toolbar_main)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar_main.inflateMenu(R.menu.menu_layout)
    }

    private fun setOnClickListener() {
        cardview_main_ssid.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
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

        // Call moreInfoDialog
        cardview_main_moreInfo.setOnClickListener {
            Toast.makeText(this, "Hold...", Toast.LENGTH_SHORT).show()
            moreInfoDialog()
        }

        // Call checkOverlayPermission
        cardview_main_floatWindow.setOnClickListener {
            if (checkOverlayPermission()) {
                if (floatWindowStatus) {
                    floatWindowStatus = false
                    textview_floatWindow_value.text = getString(R.string.floatWindow_value)
                    stopService(Intent(this, FloatWindowService::class.java))
                } else if (!floatWindowStatus) {
                    floatWindowStatus = true
                    textview_floatWindow_value.text = getString(R.string.floatWindow_value_enable)
                    startService(Intent(this, FloatWindowService::class.java))
                }
            }
        }
    }

    private fun initMobileAds()  = MobileAds.initialize(this)
    private fun setupAds() = adview_main_ad.loadAd(AdRequest.Builder().build())


    //TODO(Get IP, can't functioning now.)
    /**
     * Bug:
     * 1. Get internal ip will get cellular ip.
     * 2. Get External ip can stop at main thread,
     * @fun overrideStrictMode
     * 3. Unable to aget device MAC address
     *
     * Get more info from current wifi
     * Include BSSID, Frequency , Device MAC, IP
     * */
    private fun moreInfoDialog() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info: WifiInfo = wifiManager.connectionInfo

        val content = View.inflate(this, R.layout.dialog_moreinfo, null)
        content.textview_dialogMoreInfo_bssid.text = info.bssid
        content.textview_dialogMoreInfo_freq.text = info.frequency.toString()
        content.textview_dialogMoreInfo_myMacAddress.text = info.macAddress


        NetworkUtil().getInternalIpAddress2()


        content.textview_dialogMoreInfo_inIP.text = "0"
//        content.textview_dialogMoreInfo_exIP.text = NetworkUtil().getExternalIpAddress()

        val builder = AlertDialog.Builder(this)
        builder.setView(content)
        builder.create()
        builder.show()
    }


    /**
     * Deal with overlay window permission.
     * @return True, if OS version is below M.
     * @return False, if OS version is above M
     * */
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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_layout, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_about -> aboutDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * AlertDialog that contains the basic information of the app.
     * */
    private fun aboutDialog() {
        val content = View.inflate(this, R.layout.dialog_about, null)

        val builder = AlertDialog.Builder(this)
        builder.setView(content)
        builder.create()
        builder.show()
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
