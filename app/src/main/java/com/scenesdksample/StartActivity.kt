package com.scenesdksample

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        // Set click listener for the start button
        findViewById<View>(R.id.startButton).setOnClickListener {
            startMainActivity()
        }

        findViewById<View>(R.id.info).setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage(Html.fromHtml(resources.getString(R.string.app_info),Html.FROM_HTML_MODE_LEGACY))
                .setPositiveButton(resources.getString(android.R.string.ok), null)
                .show()
        }

        findViewById<View>(R.id.exit).setOnClickListener {
            exit()
        }

    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    override fun onBackPressed() {
        exit()
    }

    fun exit(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.are_you_sure_want_to_exit))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { _, _ -> this@StartActivity.finish() }
            .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }
}
