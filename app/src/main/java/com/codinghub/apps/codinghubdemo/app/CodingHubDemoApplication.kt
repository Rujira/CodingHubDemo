package com.codinghub.apps.codinghubdemo.app

import android.app.Application
import android.content.Context

class CodingHubDemoApplication: Application() {

    companion object {
        private lateinit var instance: CodingHubDemoApplication

        fun getAppContext(): Context = instance.applicationContext

    }

    override fun onCreate() {
        instance = this
        super.onCreate()
    }

}