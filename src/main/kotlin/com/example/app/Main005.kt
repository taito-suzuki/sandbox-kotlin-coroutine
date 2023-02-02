package com.example.app

import kotlinx.coroutines.*
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

suspend fun f001(): Int {
    delay(3000)
    println("hello")
    return 10
}

class A() : CoroutineContext.Element {
    override val key = Key

    companion object Key : CoroutineContext.Key<A>
}

fun main() = runBlocking<Unit> {
    launch(context = Dispatchers.Default + CoroutineName("fooo") + A()) {
        coroutineContext.job.invokeOnCompletion { println("end") }
        println(coroutineContext)
        println(coroutineContext[ContinuationInterceptor])
        f001()
    }
}