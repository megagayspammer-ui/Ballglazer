package com.example

import android.app.Application
import com.example.elixir.ElixirEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ElixirApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val engine: ElixirEngine by lazy {
        ElixirEngine(applicationScope, this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ElixirApplication
            private set
    }
}
