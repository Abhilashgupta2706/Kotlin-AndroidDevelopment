package com.example.audiovideo

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AudioPlayer : AppCompatActivity() {

    private lateinit var seekBar: SeekBar
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var runnable: Runnable
    private lateinit var handler: Handler

    private lateinit var currentTime: TextView
    private lateinit var totalDuration: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        val btnHome = findViewById<FloatingActionButton>(R.id.fabHome)
        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        seekBar = findViewById(R.id.sbMedia)
        handler = Handler(Looper.getMainLooper())

        val btnPlay = findViewById<FloatingActionButton>(R.id.fabPlay)
        val btnPause = findViewById<FloatingActionButton>(R.id.fabPause)
        val btnStop = findViewById<FloatingActionButton>(R.id.fabStop)

        btnPlay.setOnClickListener {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.crowdclap)
                initializeSeekBar()
            }
            mediaPlayer?.start()

            btnPlay.visibility = View.INVISIBLE
            btnPause.visibility = View.VISIBLE
            btnStop.visibility = View.VISIBLE
        }

        btnPause.setOnClickListener {
            mediaPlayer?.pause()
            btnPlay.visibility = View.VISIBLE
            btnPause.visibility = View.INVISIBLE
        }

        btnStop.setOnClickListener {
            currentTime = findViewById(R.id.tvCurrent)
            totalDuration = findViewById(R.id.tvDue)

            btnPlay.visibility = View.VISIBLE
            btnPause.visibility = View.INVISIBLE
            btnStop.visibility = View.INVISIBLE

            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer = null

            handler.removeCallbacks(runnable)
            currentTime.text = "0 sec"
            seekBar.progress = 0
        }
    }

    private fun initializeSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer?.seekTo(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        currentTime = findViewById(R.id.tvCurrent)
        totalDuration = findViewById(R.id.tvDue)

        seekBar.max = mediaPlayer!!.duration
        runnable = Runnable {
            seekBar.progress = mediaPlayer!!.currentPosition
            val playTime = mediaPlayer!!.currentPosition / 1000
            currentTime.text = "${playTime + 1} sec"
            val duration = mediaPlayer!!.duration / 1000
            val dueTime = duration - playTime
            totalDuration.text = "${dueTime + 1} sec"

            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
    }

}