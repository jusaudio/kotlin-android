package com.adrienshen_n_vlad.jus_audio.persistence.entities

import androidx.annotation.Keep
import androidx.room.*
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.RECENTLY_RECOMMENDED_TABLE_NAME


@Keep
@Entity(
    tableName = RECENTLY_RECOMMENDED_TABLE_NAME,
    indices = [Index(
        value = [JusAudioConstants.AUDIO_STREAM_URL],
        unique = true
    )]
)
data class RecommendedAudios(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = JusAudioConstants.ROW_ID) var rowId: Int,
    @ColumnInfo(name = JusAudioConstants.AUDIO_STREAM_URL) var audioStreamUrl: String
)