package com.adrienshen_n_vlad.jus_audio.background_work

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.adrienshen_n_vlad.jus_audio.MainActivity
import com.adrienshen_n_vlad.jus_audio.R
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_PLAYER_NOTIFICATION_CHANNEL_ID
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

private const val LOG_TAG = "AudioPlayerService"
private const val PLAYBACK_NOTIFICATION_ID = 129

class AudioPlayerService : Service() {

    var listenerIsSet: Boolean = false

    private val binder = LocalBinder()
    private var audioPlayer: SimpleExoPlayer? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
    private val concatenatingMediaSource = ConcatenatingMediaSource()
    private val playList = ArrayList<JusAudios>()


    private val mediaDescriptionAdapter: PlayerNotificationManager.MediaDescriptionAdapter by lazy {
        object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                val intent = Intent(this@AudioPlayerService, MainActivity::class.java)
                return PendingIntent.getActivity(
                    this@AudioPlayerService,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            override fun getCurrentContentText(player: Player): String? {
                return if(player.currentWindowIndex < playList.size)
                            playList[player.currentWindowIndex].audioAuthor
                        else ""
            }

            override fun getCurrentContentTitle(player: Player): String {
                return if(player.currentWindowIndex < playList.size)
                                            playList[player.currentWindowIndex].audioTitle
                                         else ""
            }

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                return null
            }


        }
    }

    private val notificationListener: PlayerNotificationManager.NotificationListener by lazy {
        object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean
            ) {
                startForeground(notificationId, notification)
            }

            override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                stopSelf() //terminate service
            }

        }
    }


    fun setListener(listener: Player.EventListener) {
        audioPlayer!!.addListener(listener)
        listenerIsSet = true
    }

    fun removeListener(listener: Player.EventListener) {
        audioPlayer!!.removeListener(listener)
        listenerIsSet = false
    }


    fun getTitleAndAuthor(): Array<String> {
        val currentlyPlaying = playList[audioPlayer!!.currentWindowIndex]
        Log.d(LOG_TAG, currentlyPlaying.audioTitle + " " + currentlyPlaying.audioAuthor)
        return arrayOf(currentlyPlaying.audioTitle, currentlyPlaying.audioAuthor)
    }


    private fun buildMediaSource(audioUrlStr: String): MediaSource? {
        Log.d(LOG_TAG, "building media source for $audioUrlStr")
        val uri = Uri.parse(audioUrlStr)

        return ProgressiveMediaSource.Factory(
            DefaultDataSourceFactory(
                this,
                getString(R.string.app_name)
            )
        )
            .createMediaSource(uri)

    }

    /*************** playback ************/
    private fun clearPlayList() {
        concatenatingMediaSource.clear()
        playList.clear()
    }

    fun loadPlayList(newAudios: ArrayList<JusAudios>) {
        Log.d(LOG_TAG, "loadPlayList() called with " + newAudios.size.toString() + " audios")
        if (newAudios.size > 0) {
            clearPlayList()
            newAudios.forEachIndexed { index, newAudio ->
                playList.add(index, newAudio)
                concatenatingMediaSource.addMediaSource(
                    index,
                    buildMediaSource(
                        audioUrlStr = newAudio.audioStreamUrl
                    )
                )
            }
        }
    }

    private fun preparePlayListAndNotifyUser() {
        audioPlayer!!.prepare(concatenatingMediaSource)
        audioPlayer!!.playWhenReady = true
        notifyUser()
    }


    fun addToPlayList(position: Int, audio: JusAudios) {
        Log.d(LOG_TAG, "addToPlayList() ${audio.audioTitle} at pos $position")
        playList.add(position, audio)
        concatenatingMediaSource.addMediaSource(
            position,
            buildMediaSource(
                audioUrlStr = audio.audioStreamUrl
            )
        )
    }

    fun removeFromPlayList(index: Int) {
        Log.d(LOG_TAG, "removeFromPlayList() called")
        playList.removeAt(index)
        concatenatingMediaSource.removeMediaSource(index)
        if(playList.size == 0) {
            clearPlayList()
            audioPlayer!!.playWhenReady = false
        }

    }

    fun playAudioInPlayList(audio: JusAudios) {
        val index = playList.indexOf(audio)
        Log.d( LOG_TAG, "playAudioInPlayList() at $index ${audio.audioTitle}")
        audioPlayer!!.seekTo(index, C.TIME_UNSET)
        preparePlayListAndNotifyUser()
    }



    private fun notifyUser() {
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
            this,
            AUDIO_PLAYER_NOTIFICATION_CHANNEL_ID,
            R.string.audio_player_channel_name,
            R.string.audio_player_channel_desc,
            PLAYBACK_NOTIFICATION_ID,
            mediaDescriptionAdapter,
            notificationListener
        )
        playerNotificationManager!!.setPlayer(audioPlayer)


    }

    /** Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }


    /**************** SERVICE LIFE CYCLE ***************/
    override fun onBind(intent: Intent?): IBinder? {
        Log.d(LOG_TAG, " onBind() called")
        return binder
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, " onStartCommand() called")
        audioPlayer = SimpleExoPlayer.Builder(this).build()
        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "onDestroy() called")
        releasePlayer()
    }

    fun releasePlayer() {
        if (audioPlayer != null) {
            audioPlayer!!.stop()
            audioPlayer!!.release()
            audioPlayer = null
        }
        stopSelf()
    }

    fun getPlayer(): Player? =  audioPlayer
}