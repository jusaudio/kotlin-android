package com.adrienshen_n_vlad.jus_audio.persistence.dao

import androidx.room.*
import com.adrienshen_n_vlad.jus_audio.persistence.entities.RecommendedAudios
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.RECENTLY_RECOMMENDED_TABLE_NAME
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.ROW_ID

@Dao
interface RecommendedAudiosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAudio(recommendedAudio: RecommendedAudios) : Long

    @Query("SELECT * FROM $RECENTLY_RECOMMENDED_TABLE_NAME ORDER BY $ROW_ID ASC")
    fun getRecommendedAudios(): RecommendedAudios?

    @Query("DELETE FROM $RECENTLY_RECOMMENDED_TABLE_NAME")
    fun deleteAllPreviousRecommendations()

    @Delete
    fun deleteFromRecommended(recommendedAudio: RecommendedAudios)
}