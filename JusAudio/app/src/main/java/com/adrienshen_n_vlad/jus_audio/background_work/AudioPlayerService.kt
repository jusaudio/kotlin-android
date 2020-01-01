package com.adrienshen_n_vlad.jus_audio.background_work

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.adrienshen_n_vlad.jus_audio.MainActivity
import com.adrienshen_n_vlad.jus_audio.R
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_PLAYER_NOTIFICATION_CHANNEL_ID
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

private const val LOG_TAG = "AudioPlayerService"
private const val PLAYBACK_NOTIFICATION_ID = 129
class AudioPlayerService : Service() {

    companion object{
        const val INTENT_KEY = "PLAY_LIST"
    }

    private var audioPlayer: SimpleExoPlayer? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
    private val concatenatingMediaSource = ConcatenatingMediaSource()
    private val audios = ArrayList<JusAudios>()

    private val audioPlaybackStateListener: Player.EventListener by lazy {
        object : Player.EventListener {
            override fun onPositionDiscontinuity(@Player.DiscontinuityReason reason: Int) {
                Log.d(LOG_TAG, "audioPBackStateListener - onPositionDiscontinuity")
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                val stateString: String
                when (playbackState) {
                    ExoPlayer.STATE_IDLE -> {
                        stateString = "ExoPlayer.STATE_IDLE"
                    }
                    ExoPlayer.STATE_BUFFERING -> {
                        stateString = "ExoPlayer.STATE_BUFFERING"
                    }
                    ExoPlayer.STATE_READY -> {
                        stateString = "ExoPlayer.STATE_READY"
                    }
                    ExoPlayer.STATE_ENDED -> {
                        stateString = "ExoPlayer.STATE_ENDED"
                    }
                    else -> {
                        stateString = "UNKNOWN_STATE"
                    }
                }

                Log.d(
                    LOG_TAG,
                    "audioPBackStateListener- changed state to " + stateString
                            + " playWhenReady: " + playWhenReady.toString()
                )
            }

            override fun onPlayerError(error: ExoPlaybackException) {
                super.onPlayerError(error)
                Log.d(
                    LOG_TAG,
                    "audioPBackStateListener() an error occurred ${error.message}",
                    error.cause
                )
            }

        }


    }

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
                return audios[player.currentWindowIndex].audioAuthor
            }

            override fun getCurrentContentTitle(player: Player): String {
                return audios[player.currentWindowIndex].audioTitle
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


    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, " onCreate() called")
    }

    private fun initAudioPlayer() {
        //player state
        audioPlayer = SimpleExoPlayer.Builder(this).build()
        audioPlayer!!.addListener(audioPlaybackStateListener)
    }

    private fun createPlayList() {
        concatenatingMediaSource.clear()
        for (audio in audios)
            concatenatingMediaSource.addMediaSource(
                buildMediaSource(
                    audio.rowId!!,
                    audio.audioStreamUrl
                )
            )
    }

    private fun buildMediaSource(audioId: Int, audioUrlStr: String): MediaSource? {
        //todo use real audioUrlStr
        val uri = Uri.parse(getString(R.string.dummy_audio_url))

        return ProgressiveMediaSource.Factory(
            DefaultDataSourceFactory(
                this,
                getString(R.string.app_name)
            )
        )
            .setTag(audioId)
            .createMediaSource(uri)

    }

    private fun play() {
        audioPlayer!!.prepare(concatenatingMediaSource)
        audioPlayer!!.playWhenReady = true
    }


    private fun createNotification() {
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




    /**************** SERVICE LIFE CYCLE ***************/
    override fun onBind(intent: Intent?): IBinder?  = null


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d(LOG_TAG, " onStartCommand() called")
        audios.addAll(intent!!.getParcelableArrayListExtra(INTENT_KEY)!!)
        initAudioPlayer()
        createPlayList()
        play()
        createNotification()
        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "onDestroy() called")
        releasePlayer()
    }

    private fun releasePlayer() {
        if (audioPlayer != null) {
            audioPlayer!!.release()
            audioPlayer = null
        }
    }
}