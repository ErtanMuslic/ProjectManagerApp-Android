package com.ertan.projecrmanagerapp

import android.app.Application
import com.ertan.projecrmanagerapp.data.remote.RetrofitInstance

class ProjectManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitInstance.init(this)
    }
}