package com.txwstudio.app.wifilinkspeed.ui.main

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.txwstudio.app.wifilinkspeed.R
import com.txwstudio.app.wifilinkspeed.databinding.FragmentMainBinding
import com.txwstudio.app.wifilinkspeed.service.FloatWindowService


/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
        private const val updateInterval = 1
        private const val REQUEST_CODE_OVERLAY = 0
    }

    private val liveCamViewModel: MainFragmentViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding

    private var floatWindowStatus = false

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

    @TargetApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_OVERLAY -> {
                if (Settings.canDrawOverlays(requireContext())) {

                }
            }
        }
    }

    private fun subscribeUi() {
        binding.switchMainFragmentFloatingWindowSwitch.setOnCheckedChangeListener { compoundButton, boolean ->
            if (checkOverlayPermission()) {
                if (floatWindowStatus) {
                    floatWindowStatus = false
                    binding.switchMainFragmentFloatingWindowSwitch.isChecked = false
                    requireContext().stopService(Intent(requireActivity(), FloatWindowService::class.java))
                } else if (!floatWindowStatus) {
                    floatWindowStatus = true
                    binding.switchMainFragmentFloatingWindowSwitch.isChecked = true
                    requireContext().startService(Intent(requireActivity(), FloatWindowService::class.java))
                }
            } else {
                binding.switchMainFragmentFloatingWindowSwitch.isChecked = false
            }
        }

        binding.textViewMainFragmentSpeedtestShortcutButton.setOnClickListener {
            var intent = requireContext().packageManager.getLaunchIntentForPackage("org.zwanoo.android.speedtest")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(R.string.speedtestDialog_Title)
                builder.setMessage(R.string.speedtestDialog_Meg)
                builder.setPositiveButton(R.string.global_yes) { dialog, id ->
                    intent = Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent?.data = Uri.parse("market://details?id=org.zwanoo.android.speedtest")
                    startActivity(intent)
                }
                builder.setNegativeButton(R.string.global_no) { dialog, id -> }

                builder.show()
            }
        }

        binding.textViewMainFragmentFastComShortcutButton.setOnClickListener {
            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(requireContext(), Uri.parse("https://fast.com"))
        }

        binding.textViewMainFragmentAboutButton.setOnClickListener {

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

    /**
     * Deal with overlay window permission.
     * @return True, if OS version is below M.
     * @return False, if OS version is above M
     * */
    private fun checkOverlayPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        if (!Settings.canDrawOverlays(requireContext())) {
            val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            myIntent.data = Uri.parse("package:${requireContext().packageName}")
            startActivityForResult(myIntent, REQUEST_CODE_OVERLAY)
            return false
        }
        return true
    }
}