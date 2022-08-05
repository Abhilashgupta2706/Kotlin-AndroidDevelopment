package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        Log.i("MyTAG", "SecondActivity: OnCreate")

        val username = intent.getStringExtra("USER")
        val textView = findViewById<TextView>(R.id.tvOffer)
        val message =
            "Congratulation $username, you go free access to all the features for one month."

        textView.text = message
    }

    override fun onStart() {
        super.onStart()
        Log.i("MyTAG", "SecondActivity: OnStart")
    }

    override fun onResume() {
        super.onResume()
        Log.i("MyTAG", "SecondActivity: OnResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i("MyTAG", "SecondActivity: OnPause")
    }

    override fun onStop() {
        super.onStop()
        Log.i("MyTAG", "SecondActivity: OnStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("MyTAG", "SecondActivity: onDestroy")
    }

    override fun onRestart() {
        super.onRestart()
        Log.i("MyTAG", "SecondActivity: onRestart")
    }
}