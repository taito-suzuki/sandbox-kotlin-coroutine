package com.example.app

import kotlinx.coroutines.*
import kotlin.coroutines.ContinuationInterceptor

// CoroutineContextの勉強用

fun main() {
    runBlocking {
        // コルーチンはCoroutineContextを持つ。
        // CoroutineContextは、そのコルーチンに紐づくさまざまな情報を持つ。
        println("Parent job started")
        // coroutineContextをprintすると、配列のような文字列表記が出力される。
        // > Current coroutineContext is [BlockingCoroutine{Active}@51efea79, BlockingEventLoop@5034c75a]
        // "BlockingCoroutine{Active}@51efea79"がそのコルーチンに紐づくJob。
        // "BlockingEventLoop@5034c75a"がCoroutineDispatcher（後述）。
        println("Current coroutineContext is $coroutineContext")
        // CoroutineContextに紐づく情報は、coroutineContext[key]で参照可能。
        println("CoroutineDispatcher is ${coroutineContext[ContinuationInterceptor]}")
        println("Job is ${coroutineContext[Job]}")
        // Jobは、.jobフィールドでも参照可能。
        // Jobとは、runBlockingのblockで指定した処理（Lambda関数）。
        // なんか自己言及みたい。
        println("Job is ${coroutineContext.job}")
        println("Is equal? ${coroutineContext.job == coroutineContext[Job]}")

        val parentCoroutineContext = coroutineContext

        // CoroutineDispatcherとは、コルーチンをどのスレッドで実行するか？を決定するためのオブジェクト。

        // launch関数をCoroutineContext引数を指定せずに実行した場合、
        // CoroutineDispatcherは親のコルーチン（runBlockingで作られたコルーチン）から継承される。
        launch {
            println("Job${coroutineContext.job.hashCode()}: Child job started")
            println("Job${coroutineContext.job.hashCode()}: Child coroutine has ${coroutineContext[ContinuationInterceptor]}")
            println("Job${coroutineContext.job.hashCode()}: Is this dispatcher equal to parent one? ${coroutineContext[ContinuationInterceptor] == parentCoroutineContext[ContinuationInterceptor]}")
            println("Job${coroutineContext.job.hashCode()}: Running on thread ${Thread.currentThread().name}")
        }

        // Dispatchers.Defaultはデフォルトで指定される（後述）もの。
        // Dispatchers.Defaultが指定されたコルーチンは、共通のスレッドプール上で実行される。
        // スレッドプール上にあるスレッドを使用するため、新しいスレッドが生成されるとは限らない。
        launch (Dispatchers.Default) {
            println("Job${coroutineContext.job.hashCode()}: Child job started")
            println("Job${coroutineContext.job.hashCode()}: Child coroutine has ${coroutineContext[ContinuationInterceptor]}")
            println("Job${coroutineContext.job.hashCode()}: Running on thread ${Thread.currentThread().name}")
        }

        // newSingleThreadContextにより生成されるCoroutineContextを指定すると、
        // コルーチンを実行するためのスレッドを新規作成する。
        // 必ず新しいスレッドが作られるため、より多くのリソースを消費する。乱用は避けるべき。
        launch (newSingleThreadContext("MyOwnThread")) {
            println("Job${coroutineContext.job.hashCode()}: Child job3 started")
            println("Job${coroutineContext.job.hashCode()}: Child coroutine has ${coroutineContext[ContinuationInterceptor]}")
            println("Job${coroutineContext.job.hashCode()}: Running on thread ${Thread.currentThread().name}")
        }
    }
}