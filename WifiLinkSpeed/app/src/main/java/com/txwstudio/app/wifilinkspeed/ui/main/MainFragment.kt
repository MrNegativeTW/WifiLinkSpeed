package com.txwstudio.app.wifilinkspeed.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.txwstudio.app.wifilinkspeed.R
import com.txwstudio.app.wifilinkspeed.databinding.FragmentMainBinding

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
        private const val updateInterval = 1
    }

    private val liveCamViewModel: MainFragmentViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding

    private lateinit var mHandler: Handler
    private var updateWifiInfoTask = object : Runnable {
        override fun run() {
            try {
                getWifiInfo()
            } finally {
                mHandler.postDelayed(this, updateInterval.toLong())
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeUi()
        mHandler = Handler(Looper.getMainLooper())
    }

    override fun onResume() {
        super.onResume()
        getWifiInfo()
        mHandler.post(updateWifiInfoTask)
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(updateWifiInfoTask)
    }

    private fun subscribeUi() {
        binding.switchMainFragmentSpeedtestShortcutButton.setOnClickListener {
            var intent = requireContext().packageManager.getLaunchIntentForPackage("org.zwanoo.android.speedtest")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                val builder = AlertDialog.Builder(requireContext())
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

        binding.switchMainFragmentFastComShortcutButton.setOnClickListener {
            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(requireContext(), Uri.parse("https://fast.com"))
        }
        binding.switchMainFragmentAboutButton.setOnClickListener {

        }
    }

    private fun getWifiInfo() {
        val wifiManager = context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info: WifiInfo = wifiManager.connectionInfo

        binding.textViewMainFragmentCurrentSsidContent.text = info.ssid.substring(1, info.ssid.length - 1)
        binding.textViewMainFragmentCurrentWifiMacContent.text = info.bssid
        binding.textViewMainFragmentCurrentRssiContent.text = info.rssi.toString() + " dBm"
        binding.textViewMainFragmentCurrentLinkSpeedContent.text = info.linkSpeed.toString() + " Mbps"
    }

}