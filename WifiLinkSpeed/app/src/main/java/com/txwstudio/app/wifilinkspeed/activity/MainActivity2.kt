package com.txwstudio.app.wifilinkspeed.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.txwstudio.app.wifilinkspeed.R
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setupToolBar()
    }

    private fun setupToolBar() {
        setSupportActionBar(toolbar_mainFrag)
//        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar_mainFrag.inflateMenu(R.menu.menu_main_activity2)
    }
}