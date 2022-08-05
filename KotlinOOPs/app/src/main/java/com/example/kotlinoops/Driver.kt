package com.example.kotlinoops

import android.util.Log

class Driver(private var name: String, credit: Int) {
    private var totalCredit = 100
    //    var driverName = ""
    //    lateinit var driverName: String
    //    var driverName = name

    private var car = Car()

    init {
        //    driverName = name
        totalCredit += credit
        car.maxSpeed = 200
        car.start()
    }

    fun showDriverName() {
        //    Log.i("MyTag", "Driver Name: $driverName")
        Log.i("MyTag", "By Driver Named: $name, with Credit of $totalCredit/-")
    }
}