package com.curiolabs.jusaudio

import android.app.Application
import com.curiolabs.jusaudio.content_providers.JusAudiosContentProvider
import com.curiolabs.jusaudio.persistence.JusAudioDatabase
import com.curiolabs.jusaudio.repository.AudioDataRepository

class JusAudioApp : Application() {

    private var jusAudioContentProvider: JusAudiosContentProvider? = null
    private var jusAudioDatabase: JusAudioDatabase? = null
    var audioDataRepository: AudioDataRepository? = null

    override fun onCreate() {
        super.onCreate()
        jusAudioDatabase = JusAudioDatabase.getDatabase(this)
        jusAudioContentProvider =
            JusAudiosContentProvider()
        audioDataRepository =
            AudioDataRepository(
                jusAudioContentProvider = jusAudioContentProvider!!,
                jusAudioDatabase = jusAudioDatabase!!
            )

    }
}