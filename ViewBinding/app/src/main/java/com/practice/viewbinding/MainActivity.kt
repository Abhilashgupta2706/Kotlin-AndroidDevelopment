package com.practice.viewbinding

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.practice.viewbinding.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//        val tvMessage: TextView = findViewById(R.id.tvMessage)
//        val etName: EditText = findViewById(R.id.etName)
//        val btnSubmit: Button = findViewById(R.id.btnSubmit)

//        binding.btnSubmit.setOnClickListener {
//            val name = binding.etName.text.toString()
//            binding.tvMessage.text = "Hello! $name"
//            binding.etName.setText("")
//        }

        binding.apply {
            btnSubmit.setOnClickListener {
                val name = etName.text.toString()
                tvMessage.text = "Hello! $name"
                etName.setText("")
            }
        }

    }
}