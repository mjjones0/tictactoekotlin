package com.example.tictactoe

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import java.util.concurrent.atomic.AtomicBoolean

object BackgroundPlayer {
    private lateinit var backgroundMusic: MediaPlayer
    private var isMusicPlaying = false

    private lateinit var soundPool: SoundPool
    private val soundsLoaded = AtomicBoolean(false)
    private val soundMap = mutableMapOf<String, Int>()

    fun start(context: Context) {
        if (!isMusicPlaying) {
            backgroundMusic = MediaPlayer.create(context, R.raw.bg)
            backgroundMusic.isLooping = true
            backgroundMusic.start()
            isMusicPlaying = true
        }

        if (!soundsLoaded.get()) {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(3)
                    .build()

            soundPool.setOnLoadCompleteListener { _, _, _ ->
                if (soundMap.size == 5) {
                    soundsLoaded.set(true)
                }
            }

            loadSound(context, "x", R.raw.x)
            loadSound(context, "circle", R.raw.circle)
            loadSound(context, "line", R.raw.line)
            loadSound(context, "fanfare", R.raw.fanfare)
            loadSound(context, "foghorn", R.raw.foghorn)
        }
    }

    fun end() {
        if (isMusicPlaying) {
            backgroundMusic.stop()
            backgroundMusic.release()
            isMusicPlaying = false
        }

        if (soundsLoaded.get()) {
            soundPool.release()
            soundsLoaded.set(false)
            soundMap.clear()
        }
    }

    private fun loadSound(context: Context, soundName: String, soundResource: Int) {
        val soundId = soundPool.load(context, soundResource, 1)
        soundMap[soundName] = soundId
    }

    fun playSound(soundName: String) {
        if (soundsLoaded.get()) {
            val soundId = soundMap[soundName]
            soundId?.let {
                soundPool.play(it, 1f, 1f, 1, 0, 1f)
            }
        }
    }
}