package com.example.recyclerview

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fruitsList = listOf<Fruit>(
            Fruit("Mango", "Someone"),
            Fruit("Apple", "Someone"),
            Fruit("Guava", "Someone"),
            Fruit("Banana", "Someone"),
            Fruit("Kiwi", "Someone"),
            Fruit("Pie", "Someone"),
            Fruit("Lemon", "Someone"),
            Fruit("orange", "Someone")
        )

        val recyclerView = findViewById<RecyclerView>(R.id.myRecyclerView)
        recyclerView.setBackgroundColor(Color.YELLOW)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MyRecyclerViewAdapter(fruitsList) { selectedItem: Fruit ->
            listItemClicked(selectedItem)
        }
    }

    private fun listItemClicked(fruit: Fruit) {
        Toast.makeText(
            this@MainActivity,
            "${fruit.name} is supplied by ${fruit.supplier}",
            Toast.LENGTH_SHORT
        ).show()
    }
}