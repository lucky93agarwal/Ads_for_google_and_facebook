package com.msl.myphotopicker
import android.app.Application
import com.android.billingclient.api.BillingClient
import com.google.android.gms.ads.MobileAds
import com.facebook.FacebookSdk
import com.facebook.ads.AudienceNetworkAds
import com.facebook.appevents.AppEventsLogger
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this)

// Initialize the Facebook SDK
        FacebookSdk.setAutoInitEnabled(true)
        FacebookSdk.fullyInitialize()

        // Activate App Events Logger
        AppEventsLogger.activateApp(this)

        // Initialize the Audience Network SDK
        AudienceNetworkAds.initialize(this);
    }
}