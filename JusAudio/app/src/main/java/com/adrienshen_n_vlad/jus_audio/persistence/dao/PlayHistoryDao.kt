package com.adrienshen_n_vlad.jus_audio.persistence.dao

import androidx.room.*
import com.adrienshen_n_vlad.jus_audio.persistence.entities.PlayHistory
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.HISTORY_TABLE_NAME
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.QUERY_LIMIT
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.ROW_ID

@Dao
interface PlayHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAudio(playedAudio: PlayHistory): Long

    @Query("SELECT * FROM $HISTORY_TABLE_NAME ORDER BY $ROW_ID DESC LIMIT :limit OFFSET :offset")
    fun getHistory(offset: Int = 0, limit: Int = QUERY_LIMIT): List<PlayHistory>?

    @Query("DELETE FROM $HISTORY_TABLE_NAME")
    fun deleteAllHistory()

    @Delete
    fun deleteFromHistory(playedAudio: PlayHistory)

}