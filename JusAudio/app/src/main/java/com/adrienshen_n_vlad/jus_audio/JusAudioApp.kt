package com.adrienshen_n_vlad.jus_audio

import android.app.Application
import com.adrienshen_n_vlad.jus_audio.persistence.JusAudioDatabase

class JusAudioApp : Application(){


    private var jusAudioDatabase:JusAudioDatabase? = null

    override fun onCreate() {
        super.onCreate()

        jusAudioDatabase = JusAudioDatabase.getDatabase(this)

    }
}