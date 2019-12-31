package com.adrienshen_n_vlad.jus_audio

import android.app.Application
import com.adrienshen_n_vlad.jus_audio.content_providers.JusAudiosContentProvider
import com.adrienshen_n_vlad.jus_audio.persistence.JusAudioDatabase
import com.adrienshen_n_vlad.jus_audio.repository.AudioDataRepository

class JusAudioApp : Application() {

    private var jusAudioContentProvider: JusAudiosContentProvider? = null
    private var jusAudioDatabase: JusAudioDatabase? = null
    var audioDataRepository: AudioDataRepository? = null

    override fun onCreate() {
        super.onCreate()
        jusAudioDatabase = JusAudioDatabase.getDatabase(this)
        jusAudioContentProvider = JusAudiosContentProvider()
        audioDataRepository =
            AudioDataRepository(
                jusAudioContentProvider = jusAudioContentProvider!!,
                jusAudioDatabase = jusAudioDatabase!!
            )

    }
}