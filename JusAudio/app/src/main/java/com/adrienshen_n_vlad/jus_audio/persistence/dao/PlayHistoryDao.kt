package com.adrienshen_n_vlad.jus_audio.persistence.dao

import androidx.room.*
import com.adrienshen_n_vlad.jus_audio.persistence.entities.PlayHistory
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.ROW_ID
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.HISTORY_TABLE_NAME

@Dao
interface PlayHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAudio(playedAudio: PlayHistory) : Long

    @Query("SELECT * FROM $HISTORY_TABLE_NAME ORDER BY $ROW_ID ASC")
    fun getHistory(): PlayHistory?

    @Query("DELETE FROM $HISTORY_TABLE_NAME")
    fun deleteAllHistory()

    @Delete
    fun deleteFromHistory(playedAudio: PlayHistory)

}