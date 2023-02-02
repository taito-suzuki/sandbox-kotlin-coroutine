package com.example.app

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        launch {
            delay(5000)
            println("World!")
        }
        println("Hello")
    }
}