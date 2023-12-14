package com.msl.myphotopicker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.otpless.dto.OtplessResponse
import com.otpless.main.OtplessEventCallback
import com.otpless.main.OtplessEventData
import com.otpless.main.OtplessManager
import com.otpless.main.OtplessView
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var otplessView: OtplessView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        onInstall()

    }

    private fun onInstall(){
        // Initialise OtplessView
        otplessView = OtplessManager.getInstance().getOtplessView(this)



        val extras = JSONObject().also {
            it.put("method", "get")
            val params = JSONObject()
            params.put("cid", "153VHPJ57Z8R3MQ77SYCOER6ZHEPGKND")
            it.put("params", params)
        }


        Log.i("OtplessLucky", "onCreate: ")
        otplessView.showOtplessFab(false)
        otplessView.setFabText("This is My Application")
        otplessView.setCallback(this::onOtplessCallback, extras)
        otplessView.showOtplessLoginPage(extras, this::onOtplessResult)


        // very important to call here, verification is done on low memory recreate case
        otplessView.verifyIntent(intent)
    }

    private fun onOtplessCallback(response: OtplessResponse) {
        Log.i("OtplessLucky", "response: $response")
        if (response.errorMessage != null) {
            // todo error handing
            Log.i("OtplessLucky", "error message : "+response.errorMessage)
            onBackPressed()
        } else {
            val token = response.data.optString("token")
            // todo token verification with api
            Log.i("OtplessLucky", "token: $token")

            val intent: Intent? = Intent(this, onOtplessResultActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun onOtplessResult(response: OtplessResponse) {
        Log.i("OtplessLucky", "onOtplessResult response: $response")
        if (response.errorMessage != null) {
            // todo error handing

            Log.i("OtplessLucky", "onOtplessResult error message : "+response.errorMessage)
            onBackPressed()
        } else {
            val token = response.data.optString("token")
            // todo token verification with api
            Log.i("OtplessLucky", "onOtplessResult token: $token")
            val intent: Intent? = Intent(this, onOtplessResultActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        onInstall()
    }

    override fun onStart() {
        super.onStart()
        onInstall()
    }

    override fun onPause() {
        super.onPause()
        onBackPressed()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // make sure you call this code before super.onBackPressed()
        otplessView.closeView()
        if (otplessView.onBackPressed()) return
    }
}