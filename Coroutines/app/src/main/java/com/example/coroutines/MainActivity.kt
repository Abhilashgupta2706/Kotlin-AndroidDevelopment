package com.example.coroutines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private var count = 0
    private lateinit var tvMessage: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvCount = findViewById<TextView>(R.id.tvCount)
        tvMessage = findViewById(R.id.tvMessage)
        val btnCount = findViewById<TextView>(R.id.btnCount)
        val btnDownload = findViewById<TextView>(R.id.btnDownload)

        btnCount.setOnClickListener {
            tvCount.text = count++.toString()
        }
        btnDownload.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                downloadUserData()
            }
        }
    }

    private suspend fun downloadUserData() {
        for (i in 1..200000) {
            Log.i("MyTag", "Downloading user $i in ${Thread.currentThread().name}")
            withContext(Dispatchers.Main) {
                tvMessage.text = "Downloading user $i"
            }
            delay(100)
        }
    }
}

