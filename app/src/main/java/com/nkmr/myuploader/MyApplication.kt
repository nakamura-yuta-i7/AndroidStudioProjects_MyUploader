package com.nkmr.myuploader

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.common.api.GoogleApiClient

class MyApplication : Application() {
    lateinit var preferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        preferences = getSharedPreferences( packageName + "_preferences", MODE_PRIVATE)
        mContext = this
    }

    companion object Factory {
        lateinit var mContext: MyApplication
        fun getContext(): Context {
            return mContext
        }
    }

    class LoginUser(var email: String)
    var loginUser: LoginUser? = null
    var googleApiClient: GoogleApiClient? = null
    fun login(email: String, mGoogleApiClient: GoogleApiClient) {
        loginUser = LoginUser(email)
        googleApiClient = mGoogleApiClient
    }
}