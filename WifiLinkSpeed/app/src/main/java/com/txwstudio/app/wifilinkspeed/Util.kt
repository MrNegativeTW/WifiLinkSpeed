package com.txwstudio.app.wifilinkspeed

import android.content.Context
import android.widget.Toast

class Util {

    fun Toast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
}