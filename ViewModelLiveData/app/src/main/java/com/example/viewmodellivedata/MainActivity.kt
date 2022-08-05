package com.example.viewmodellivedata

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        val tvCount = findViewById<TextView>(R.id.tvCount)
        val btnCount = findViewById<Button>(R.id.btnCount)

        tvCount.text = viewModel.totalCount.toString()

        btnCount.setOnClickListener {
            viewModel.increaseCount()
            tvCount.text = viewModel.totalCount.toString()
        }
    }
}