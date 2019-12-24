package com.adrienshen_n_vlad.jus_audio.persistence.dao

import androidx.room.*
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIOS_TABLE_NAME
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_AUTHOR
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_INFO_LANG_ID
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.ROW_ID
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_IS_FAVORITE
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_STREAM_URL
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIO_TITLE
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.QUERY_LIMIT

@Dao
interface JusAudiosDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAudios(audiosList: ArrayList<JusAudios>) : List<Long>


    @Query("SELECT *, $ROW_ID, $AUDIO_INFO_LANG_ID FROM $AUDIOS_TABLE_NAME WHERE $AUDIO_STREAM_URL = :audioStreamUrl ORDER BY $ROW_ID ASC")
    fun selectByStreamUrl(audioStreamUrl: String): JusAudios?


    @Query("SELECT *, $ROW_ID, $AUDIO_INFO_LANG_ID FROM $AUDIOS_TABLE_NAME WHERE $AUDIO_IS_FAVORITE = :isFavorite ORDER BY $ROW_ID ASC")
    fun getFavorites(isFavorite: Boolean = true): JusAudios?

    @Update
    fun updateAudios(vararg jusAudios: JusAudios)

    @Query("SELECT *, $ROW_ID, $AUDIO_INFO_LANG_ID FROM $AUDIOS_TABLE_NAME WHERE $AUDIO_TITLE MATCH :searchQuery " +
            "OR $AUDIO_AUTHOR MATCH :searchQuery ORDER BY $ROW_ID ASC LIMIT :limit OFFSET :offset")
    fun findAudio(searchQuery: String, limit : Int = QUERY_LIMIT, offset: Int = 0): List<JusAudios>

}