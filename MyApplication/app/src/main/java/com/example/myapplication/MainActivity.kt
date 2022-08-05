package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i("MyTAG", "MainActivity: OnCreate")

//        val greetingTextView = findViewById<TextView>(R.id.tvGreet)
        val inputField = findViewById<EditText>(R.id.etName)
        val submitBtn = findViewById<Button>(R.id.tvBtnSubmit)
        val greetingMsgTextView = findViewById<TextView>(R.id.tvGreetMsg)
        val viewOfferBtn = findViewById<Button>(R.id.btnOffers)

        var enteredName: String = ""

        submitBtn.setOnClickListener {
            enteredName = inputField.text.toString()

            if (enteredName == "") {
                viewOfferBtn.visibility = INVISIBLE
                greetingMsgTextView.text = ""
                Toast.makeText(
                    this@MainActivity,
                    "Name is required to submit!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val message = "Welcome $enteredName to my first android app."
                Log.i("MyTAG", message)
                greetingMsgTextView.text = message
                Log.i("MyTAG", "After displaying msg in TextView")
                inputField.text.clear()
                viewOfferBtn.visibility = VISIBLE
            }
        }

        viewOfferBtn.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            intent.putExtra("USER", enteredName)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i("MyTAG", "MainActivity: OnStart")
    }

    override fun onResume() {
        super.onResume()
        Log.i("MyTAG", "MainActivity: OnResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i("MyTAG", "MainActivity: OnPause")
    }

    override fun onStop() {
        super.onStop()
        Log.i("MyTAG", "MainActivity: OnStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("MyTAG", "MainActivity: onDestroy")
    }

    override fun onRestart() {
        super.onRestart()
        Log.i("MyTAG", "MainActivity: onRestart")
    }
}