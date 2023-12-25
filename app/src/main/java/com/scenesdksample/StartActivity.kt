package com.scenesdksample

import android.content.Intent
import android.os.Bundle
//import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {

//    private val SPLASH_DELAY: Long = 2000000 // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        // Set click listener for the start button
        findViewById<View>(R.id.startButton).setOnClickListener {
            startMainActivity()
        }

    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
