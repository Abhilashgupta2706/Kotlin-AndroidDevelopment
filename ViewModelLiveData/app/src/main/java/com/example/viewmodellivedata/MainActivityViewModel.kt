package com.example.viewmodellivedata

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    //    var totalCount = 0
    var totalCount = MutableLiveData<Int>()

    init {
        totalCount.value = 0
    }

    fun increaseCount() {
//       ++totalCount
        totalCount.value = (totalCount.value)?.plus(1)
    }
}