package com.example.app

import kotlinx.coroutines.*

fun main() {
    runBlocking {
        val jobs = listOf(
            launch {
                while (true) {
                    delay(1000)
                    println("loop1")
                }
            },
            launch {
                try {
                    while (true) {
                        delay(1000)
                        println("loop2")
                    }
                } catch(e: CancellationException) {
                    println("loop2 ジョブがキャンセルされました by $e")
                }
            },
            launch {
                while (true) {
                    delay(1000)
                    println("loop3")
                }
            },
        )
        delay(5000)
        jobs.forEach { it.cancel() }
        jobs.joinAll()
        delay(5000)
        println("done")
    }
}