package com.adrienshen_n_vlad.jus_audio.persistence.entities

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIOS_TABLE_NAME
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_AUTHOR
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_COVER_THUMB_URL
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_INFO_LANG_ID
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_IS_FAVORITE
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_STREAM_URL
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_TITLE
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.ROW_ID

@Keep
@Fts4(languageId = "lid", notIndexed = [AUDIO_STREAM_URL, AUDIO_COVER_THUMB_URL, AUDIO_IS_FAVORITE])
@Entity(
    tableName = AUDIOS_TABLE_NAME
)
data class JusAudios(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ROW_ID) var rowId: Int?,
    @ColumnInfo(name = AUDIO_TITLE) var audioTitle: String,
    @ColumnInfo(name = AUDIO_AUTHOR) var audioAuthor: String,
    @ColumnInfo(name = AUDIO_STREAM_URL) var audioStreamUrl: String,
    @ColumnInfo(name = AUDIO_COVER_THUMB_URL) var audioCoverThumbnailUrl: String,
    @ColumnInfo(name = AUDIO_IS_FAVORITE) var audioIsFavorite: Boolean,
    @ColumnInfo(name = AUDIO_INFO_LANG_ID) var audioInfoLanguageId: Int
)