package com.samatani.app

import android.app.Application
import com.samatani.app.data.SamaTaniRepository

class SamaTaniApplication : Application() {

    lateinit var repository: SamaTaniRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = SamaTaniRepository(this)
    }
}
