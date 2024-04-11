package com.star.antimosquito

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private var audioTrack: AudioTrack? = null
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val toggleButton = findViewById<Button>(R.id.toggle_button)
        toggleButton.setOnClickListener {
            if (isPlaying) {
                isPlaying = !isPlaying
                stopUltrasonic()
                Toast.makeText(this@MainActivity, "Ultrasonic repellent turned off", Toast.LENGTH_SHORT).show()
            } else {
                isPlaying = !isPlaying
                startUltrasonic()
                Toast.makeText(this@MainActivity, "Ultrasonic repellent turned on", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startUltrasonic() {
        val bufferSize = AudioTrack.getMinBufferSize(20000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val audioFormat = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(20000)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(audioAttributes)
            .setAudioFormat(audioFormat)
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()

        Thread {
            val buffer = ShortArray(bufferSize)
            while (isPlaying) {
                for (i in buffer.indices) {
                    if (!isPlaying) break // Check if playback should stop
                    buffer[i] = (16383 * Math.sin(2 * Math.PI * 20000 * i / 44100.0)).toInt().toShort()
                }
                if (isPlaying) audioTrack?.write(buffer, 0, buffer.size) // Write to audio track if still playing
            }
            audioTrack?.stop() // Stop playback when thread exits
            audioTrack?.release()
        }.start()
    }

    private fun stopUltrasonic() {
        isPlaying = false
    }
}