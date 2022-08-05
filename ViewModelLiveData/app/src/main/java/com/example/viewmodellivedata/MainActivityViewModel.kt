package com.example.viewmodellivedata

import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    var totalCount = 0

   fun  increaseCount(){
       ++totalCount
   }
}