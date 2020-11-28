package com.txwstudio.app.wifilinkspeed.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class MainFragmentViewModel(application: Application) : AndroidViewModel(application) {

    val currentSsid = MutableLiveData<String>()
    val currentBssid = MutableLiveData<String>()
    val currentRssi = MutableLiveData<String>()
    val currentLinkSpeed = MutableLiveData<String>()

    fun getWifiInfo() {
//        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val info: WifiInfo = wifiManager.connectionInfo

//        currentSsid.value = info.ssid.substring(1, info.ssid.length - 1)
//        currentBssid.value = info.bssid
//        currentRssi.value = info.rssi.toString() + " dBm"
//        currentLinkSpeed.value = info.linkSpeed.toString() + " Mbps"
    }
}