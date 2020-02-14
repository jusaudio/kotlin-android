package com.curiolabs.jusaudio.persistence.entities

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.curiolabs.jusaudio.utility_classes.JusAudioConstants
import com.curiolabs.jusaudio.utility_classes.JusAudioConstants.AUDIO_INFO_LANG_ID
import com.curiolabs.jusaudio.utility_classes.JusAudioConstants.ROW_ID

@Keep
@Entity(tableName = JusAudioConstants.AUDIOS_FTS_TABLE_NAME)
@Fts4(contentEntity = JusAudios::class, languageId = "lid")
data class JusAudiosFts(
    @ColumnInfo(name = ROW_ID) @PrimaryKey var rowId: Int,
    @ColumnInfo(name = JusAudioConstants.AUDIO_SEARCH_TAGS) var audioSearchTags: String,
    @ColumnInfo(name = AUDIO_INFO_LANG_ID) var audioInfoLanguageId: Int
)