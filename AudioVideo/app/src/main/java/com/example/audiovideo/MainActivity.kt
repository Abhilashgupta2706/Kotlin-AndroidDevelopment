package com.example.audiovideo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnAudio = findViewById<Button>(R.id.btnAudio)
        val btnVideo = findViewById<Button>(R.id.btnVideo)

        btnAudio.setOnClickListener {
            val intent = Intent(this, AudioPlayer::class.java)
            startActivity(intent)
        }

        btnVideo.setOnClickListener {
            val intent = Intent(this, VideoPlayer::class.java)
            startActivity(intent)
        }
    }
}