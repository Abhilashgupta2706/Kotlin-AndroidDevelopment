package com.example.viewmodelscopeincoroutine

import kotlinx.coroutines.delay

class UserRepository {

    suspend fun getUsers(): List<User> {
        delay(8000)
        val users: List<User> = listOf(
            User(1, "Sam"),
            User(2, "Vam"),
            User(3, "Tam"),
            User(4, "Jam")
        )
        return users
    }
}