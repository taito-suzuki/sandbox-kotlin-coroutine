package com.example.app

import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.CoroutineContext

// Appends "name" of a coroutine to a current thread name when coroutine is executed
class CoroutineName(val name: String) : ThreadContextElement<String> {
    // declare companion object for a key of this element in coroutine context
    companion object Key : CoroutineContext.Key<CoroutineName>

    // provide the key of the corresponding context element
    override val key: CoroutineContext.Key<CoroutineName>
        get() = Key

    // this is invoked before coroutine is resumed on current thread
    override fun updateThreadContext(context: CoroutineContext): String {
        println("再開します")
        val previousName = Thread.currentThread().name
        Thread.currentThread().name = "$previousName # $name"
        return previousName
    }

    // this is invoked after coroutine has suspended on current thread
    override fun restoreThreadContext(context: CoroutineContext, oldState: String) {
        println("中断します")
        Thread.currentThread().name = oldState
    }
}