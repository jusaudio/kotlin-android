package com.adrienshen_n_vlad.jus_audio.persistence.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_AUTHOR
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_COVER_THUMB_URL
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_INFO_LANG_ID
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_IS_FAVORITE
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_IS_IN_MY_COLLECTION
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_IS_RECOMMENDED
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_SEARCH_TAGS
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_STREAM_URL
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_TITLE
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.JUS_AUDIOS_TABLE_NAME
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.ROW_ID

@Keep
@Entity(
    tableName = JUS_AUDIOS_TABLE_NAME,
    indices = [Index(
        value = [AUDIO_STREAM_URL],
        unique = true
    )]
)
data class JusAudios(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ROW_ID) var rowId: Int?,
    @ColumnInfo(name = AUDIO_TITLE) var audioTitle: String,
    @ColumnInfo(name = AUDIO_AUTHOR) var audioAuthor: String,
    @ColumnInfo(name = AUDIO_STREAM_URL) var audioStreamUrl: String,
    @ColumnInfo(name = AUDIO_SEARCH_TAGS) var audioSearchTags: String,
    @ColumnInfo(name = AUDIO_COVER_THUMB_URL) var audioCoverThumbnailUrl: String,
    @ColumnInfo(name = AUDIO_IS_FAVORITE) var audioIsFavorite: Boolean,
    @ColumnInfo(name = AUDIO_IS_RECOMMENDED) var audioIsRecommended: Boolean,
    @ColumnInfo(name = AUDIO_IS_IN_MY_COLLECTION) var audioIsInMyCollection: Boolean,
    @ColumnInfo(name = AUDIO_INFO_LANG_ID) var audioInfoLanguageId: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt()
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(rowId)
        parcel.writeString(audioTitle)
        parcel.writeString(audioAuthor)
        parcel.writeString(audioStreamUrl)
        parcel.writeString(audioSearchTags)
        parcel.writeString(audioCoverThumbnailUrl)
        parcel.writeByte(if (audioIsFavorite) 1 else 0)
        parcel.writeByte(if (audioIsRecommended) 1 else 0)
        parcel.writeByte(if (audioIsInMyCollection) 1 else 0)
        parcel.writeInt(audioInfoLanguageId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<JusAudios> {
        override fun createFromParcel(parcel: Parcel): JusAudios {
            return JusAudios(parcel)
        }

        override fun newArray(size: Int): Array<JusAudios?> {
            return arrayOfNulls(size)
        }
    }

}

