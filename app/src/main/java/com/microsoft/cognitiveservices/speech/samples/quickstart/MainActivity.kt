
package com.microsoft.cognitiveservices.speech.samples.quickstart

import android.Manifest.permission
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import com.microsoft.cognitiveservices.speech.*
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import com.microsoft.cognitiveservices.speech.dialog.ActivityReceivedEventArgs
import com.microsoft.cognitiveservices.speech.dialog.CustomCommandsConfig
import com.microsoft.cognitiveservices.speech.dialog.DialogServiceConnector
import org.json.JSONObject
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var dialogServiceConnector: DialogServiceConnector

    init {
        initSpeechSDK()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusTextView = findViewById<TextView>(R.id.status)
        statusTextView.text = "listening for \"computer\""

        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission.RECORD_AUDIO, permission.INTERNET), 123)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        startKeywordListeningAsync()
    }

    private fun initSpeechSDK(){


        val dialogServiceConfig = CustomCommandsConfig.fromSubscription(
                BuildConfig.TEST_APP_ID,
                BuildConfig.TEST_SPEECH_RESOURCE_KEY,
                BuildConfig.TEST_APP_REGION
        )

        dialogServiceConfig.setProperty(PropertyId.SpeechServiceConnection_RecoLanguage, "en-us")

        val audioConfig = AudioConfig.fromDefaultMicrophoneInput()

        dialogServiceConnector = DialogServiceConnector(dialogServiceConfig, audioConfig)

        dialogServiceConnector.recognizing.addEventListener { _: Any?, speechRecognitionResultEventArgs: SpeechRecognitionEventArgs ->

            val statusTextView = findViewById<TextView>(R.id.status)
            val reason = speechRecognitionResultEventArgs.result.reason
            val text = speechRecognitionResultEventArgs.result.text
            val message = "$reason $text"
            if (reason == ResultReason.RecognizingKeyword) {
                statusTextView.text = message
            } else {
                statusTextView.text = statusTextView.text.toString() + "\n" + message
            }

            Log.i("KEYWORDTEST",message)
        }

        dialogServiceConnector.recognized.addEventListener { _: Any?, speechRecognitionResultEventArgs: SpeechRecognitionEventArgs ->

            val statusTextView = findViewById<TextView>(R.id.status)
            val reason = speechRecognitionResultEventArgs.result.reason
            val text = speechRecognitionResultEventArgs.result.text
            val message = "$reason $text"
            statusTextView.text = statusTextView.text.toString() + "\n" + message
            Log.i("KEYWORDTEST",message)
        }

        dialogServiceConnector.sessionStarted.addEventListener { _: Any?, sessionEventArgs: SessionEventArgs ->
            Log.i("KEYWORDTEST","SESSION STARTED $sessionEventArgs.sessionId")
        }

        dialogServiceConnector.sessionStopped.addEventListener { _: Any?, sessionEventArgs: SessionEventArgs ->
            Log.i("KEYWORDTEST","SESSION STOPPED $sessionEventArgs.sessionId")
        }

        dialogServiceConnector.canceled.addEventListener { _: Any?, canceledEventArgs: SpeechRecognitionCanceledEventArgs ->

            val errorCode = canceledEventArgs.errorCode.value
            val errorDetails = canceledEventArgs.errorDetails

            Log.i("KEYWORDTEST","CANCELLED ERROR $errorCode DETAILS $errorDetails")
        }

        dialogServiceConnector.activityReceived.addEventListener { _: Any?, activityEventArgs: ActivityReceivedEventArgs ->

            val statusTextView = findViewById<TextView>(R.id.status)
            val activity = activityEventArgs.activity
            Log.i("KEYWORDTEST","RECEIVED ACTIVITY $activity")
            val json = JSONObject(activityEventArgs.activity)
            val event = json.getString("event")
            statusTextView.text = statusTextView.text.toString() + "\n RECEIVED EVENT " + event + "\n listening for \"computer\""
        }
    }

    private fun startKeywordListeningAsync(){
        val inputStream: InputStream = assets.open("kws.table")
        val model = KeywordRecognitionModel.fromStream(inputStream, "computer", false)
        dialogServiceConnector.startKeywordRecognitionAsync(model)
    }
}