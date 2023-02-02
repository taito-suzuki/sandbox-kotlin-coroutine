package com.example.app

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

fun main() {
    runBlocking {
        for (i in 1..10000) {
            async {
                val ii = i
                println("${LocalDateTime.now()} ${Thread.currentThread().name} job${ii} start")
                delay(1000)
                println("${LocalDateTime.now()} ${Thread.currentThread().name} job${ii} end")
            }
        }
    }
}
