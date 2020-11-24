//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE.md file in the project root for full license information.
//
// <code>
package com.microsoft.cognitiveservices.speech.samples.quickstart

import android.Manifest.permission
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import com.microsoft.cognitiveservices.speech.KeywordRecognitionModel
import com.microsoft.cognitiveservices.speech.KeywordRecognizer
import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private val es = Executors.newFixedThreadPool(2)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Note: we need to request the permissions
        val requestCode = 5 // unique code for the permission request
        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission.RECORD_AUDIO, permission.INTERNET), requestCode)
    }

    fun onSpeechButtonClicked(v: View?) {
        val txt = findViewById<View>(R.id.hello) as TextView // 'hello' is the ID of your text view
        txt.text = "Say \"Computer\""
        val asyncTask = Runnable {
            try {
                val am = assets
                val config = AudioConfig.fromDefaultMicrophoneInput()!!
                val reco = KeywordRecognizer(config)
                val `is` = am.open("kws.table")
                val model = KeywordRecognitionModel.fromStream(`is`, "computer", false)
                val task = reco.recognizeOnceAsync(model)!!

                // Note: this will block the UI thread, so eventually, you want to
                //        register for the event (see full samples)
                val result = task.get()!!
                if (result.reason == ResultReason.RecognizedKeyword) {
                    txt.text = "Recognized " + result.text
                } else {
                    txt.text = "Error: got the wrong sort of recognition."
                }
                reco.close()
            } catch (ex: Exception) {
                Log.e("SpeechSDKDemo", "unexpected " + ex.message)
                assert(false)
            }
        }
        es.execute(asyncTask)
    }
}