package com.adrienshen_n_vlad.jus_audio.persistence.entities

import androidx.annotation.Keep
import androidx.room.*
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.ROW_ID
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_STREAM_URL
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.HISTORY_TABLE_NAME

@Keep
@Entity(
    tableName = HISTORY_TABLE_NAME,
    indices = [Index(
        value = [AUDIO_STREAM_URL],
        unique = true
    )]
)
data class PlayHistory(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ROW_ID) var rowId: Int,
    @ColumnInfo(name = AUDIO_STREAM_URL) var audioStreamUrl: String
)