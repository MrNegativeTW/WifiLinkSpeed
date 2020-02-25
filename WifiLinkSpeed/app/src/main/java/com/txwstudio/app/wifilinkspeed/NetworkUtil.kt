package com.txwstudio.app.wifilinkspeed

import android.util.Log
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

class NetworkUtil {

    fun getInternalIpAddress(): String {
        try {
            val networkInterface = NetworkInterface.getNetworkInterfaces()
            while (networkInterface.hasMoreElements()) {
                val intf = networkInterface.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (ex: Exception) {
            return "-"
        }
        return "-"
    }

    fun getInternalIpAddress2() {
        val address = InetAddress.getLocalHost().address
        val hostAddress = InetAddress.getLocalHost().hostAddress
        Log.i("TESTTT","address: $address hostAddress: $hostAddress")
        Log.i("TESTTT","getIPAddress: ${getIPAddress(true)}")
    }


    fun getIPAddress(useIPv4: Boolean): String? {
        try {
            val interfaces: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (useIPv4) {
                            if (isIPv4) return sAddr
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                return if (delim < 0) sAddr.toUpperCase() else sAddr.substring(0, delim).toUpperCase()
                            }
                        }
                    }
                }
            }
        } catch (ignored: java.lang.Exception) {
        } // for now eat exceptions
        return ""
    }



















    fun getExternalIpAddress(): String? {
        return HttpRequest.get("http://api.ipify.org/").connectTimeout(3000).body()
//        var result: String? = null
//
//        Thread(Runnable {
//            try {
//                result = HttpRequest.get("http://api.ipify.org/").connectTimeout(5000).body()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }).start()
//
//        return result
    }


}