package com.example.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class MyRecyclerViewAdapter(
    private val list: List<Fruit>,
    private val clickListener: (Fruit) -> Unit
) : RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.list_item, parent, false)
        return MyViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var fruit = list[position]
        holder.bind(fruit, clickListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

}

class MyViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    fun bind(fruit: Fruit, clickListener: (Fruit) -> Unit) {
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvSupplier = view.findViewById<TextView>(R.id.tvSupplier)

        tvName.text = fruit.name
        tvSupplier.text = fruit.supplier

        view.setOnClickListener {
            clickListener(fruit)
        }
    }
}


