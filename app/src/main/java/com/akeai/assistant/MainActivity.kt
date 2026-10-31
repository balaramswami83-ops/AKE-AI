package com.akeai.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var status: TextView
    private val requestSpeech = 100
    private val requestPermission = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 36, 28, 36)
        }

        val title = TextView(this).apply {
            text = "AKE AI"
            textSize = 32f
            gravity = Gravity.CENTER
        }

        status = TextView(this).apply {
            text = "ନମସ୍କାର! ମୁଁ AKE AI।\nକଥା କହିବା ପାଇଁ ତଳେ ଦବାନ୍ତୁ।"
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(0, 28, 0, 28)
        }

        val listen = Button(this).apply {
            text = "🎙 କଥା କୁହ"
            textSize = 20f
            setOnClickListener { startListening() }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(listen)
        setContentView(layout)
    }

    private fun startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), requestPermission)
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "or-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "AKE AI କୁ କୁହନ୍ତୁ")
        }

        try {
            startActivityForResult(intent, requestSpeech)
        } catch (_: Exception) {
            status.text = "ଫୋନ୍‌ରେ Speech Recognition ସୁବିଧା ମିଳୁନାହିଁ।"
            speak("Speech recognition is not available")
        }
    }

    @Deprecated("Deprecated by Android")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == requestSpeech && resultCode == RESULT_OK) {
            val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            status.text = "ତୁମେ କହିଲ: $spoken"
            handleCommand(spoken)
        }
    }

    private fun handleCommand(text: String) {
        val q = text.lowercase(Locale.ROOT)

        when {
            q.contains("google") || q.contains("ସର୍ଚ୍ଚ") || q.contains("search") -> {
                val query = text.replace("Google", "", true)
                    .replace("search", "", true)
                    .replace("ସର୍ଚ୍ଚ", "").trim()

                status.text = "Google ରେ ଖୋଜୁଛି: $query"
                speak("Google ରେ ଖୋଜୁଛି")

                val url = "https://www.google.com/search?q=" +
                    android.net.Uri.encode(query.ifBlank { text })

                startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
            }

            q.contains("youtube") -> openApp("com.google.android.youtube", "YouTube")
            q.contains("chrome") -> openApp("com.android.chrome", "Chrome")

            q.contains("ନମସ୍କାର") || q.contains("hello") || q.contains("hi") -> {
                status.text = "ନମସ୍କାର! ମୁଁ AKE AI।"
                speak("ନମସ୍କାର! ମୁଁ AKE AI।")
            }

            else -> {
                status.text = "ତୁମେ କହିଲ: $text\nଏହି କମାଣ୍ଡ ପାଇଁ ଆଗକୁ AI ସଂଯୋଗ କରିବାକୁ ପଡ଼ିବ।"
                speak("ତୁମ କଥା ଶୁଣିଲି। ଏହି କାମ ପାଇଁ ଆଗକୁ AI ସଂଯୋଗ କରିବାକୁ ପଡ଼ିବ।")
            }
        }
    }

    private fun openApp(packageName: String, label: String) {
        val launch = packageManager.getLaunchIntentForPackage(packageName)

        if (launch != null) {
            speak("$label ଖୋଲୁଛି")
            startActivity(launch)
        } else {
            speak("$label ଆପ୍ ମିଳିଲା ନାହିଁ")
            status.text = "$label ଆପ୍ ଫୋନ୍‌ରେ ନାହିଁ।"
        }
    }

    private fun speak(message: String) {
        if (::tts.isInitialized) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "ake_speech")
        }
    }

    override fun onInit(statusCode: Int) {
        if (statusCode == TextToSpeech.SUCCESS) {
            tts.language = Locale("or", "IN")
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
