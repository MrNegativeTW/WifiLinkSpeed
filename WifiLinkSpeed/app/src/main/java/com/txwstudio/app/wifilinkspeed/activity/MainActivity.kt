package com.txwstudio.app.wifilinkspeed.activity

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.*
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.txwstudio.app.wifilinkspeed.service.FloatWindowService
import com.txwstudio.app.wifilinkspeed.NetworkUtil
import com.txwstudio.app.wifilinkspeed.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_moreinfo.view.*
import java.lang.Exception


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

        // Init Google Ad Mob
        MobileAds.initialize(this)
        adview_main_ad.loadAd(AdRequest.Builder().build())

        getWifiInfo()
        mHandler = Handler()

        getUnsplashRandomPhoto()
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

//         Fix white status bar when sdk <= 21
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.gray)
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
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            try {
                startActivity(intent)
            } catch (e: Exception) {}
        }

        cardview_main_ssid.setOnLongClickListener {
            getUnsplashRandomPhoto()
            return@setOnLongClickListener true
        }

        cardview_main_openspeedtest.setOnClickListener {
            var intent = packageManager.getLaunchIntentForPackage("org.zwanoo.android.speedtest")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.speedtestDialog_Title)
                builder.setMessage(R.string.speedtestDialog_Meg)
                builder.setPositiveButton(R.string.global_yes){ dialog, id ->
                    intent = Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent?.data = Uri.parse("market://details?id=org.zwanoo.android.speedtest")
                    startActivity(intent)
                }
                builder.setNegativeButton(R.string.global_no){ dialog, id ->  }

                builder.show()
            }
        }

        cardview_main_openfastcom.setOnClickListener {
            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(this, Uri.parse("https://fast.com"))
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
        textview_bssid_value.text = info.bssid
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

    private fun getUnsplashRandomPhoto() {
        Picasso.get().load("https://source.unsplash.com/random")
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(imageView_main_ssid)
    }

}
