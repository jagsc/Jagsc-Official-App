package org.jagsc.jagsc_official_app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView = findViewById<WebView>(R.id.webView)
        webView.webViewClient = WebViewClient()
        webView.loadUrl("http://student.android-group.jp/")

        val home = findViewById<Button>(R.id.home)
        home.setOnClickListener {
            webView.loadUrl("http://student.android-group.jp/")
        }

        val students = findViewById<Button>(R.id.students)
        students.setOnClickListener {
            webView.loadUrl("http://student.android-group.jp/about/")
        }

        val history = findViewById<Button>(R.id.history)
        history.setOnClickListener {
            webView.loadUrl("http://student.android-group.jp/")
        }

        val entry = findViewById<Button>(R.id.entry)
        entry.setOnClickListener {
            webView.loadUrl("http://student.android-group.jp/join/")
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}
