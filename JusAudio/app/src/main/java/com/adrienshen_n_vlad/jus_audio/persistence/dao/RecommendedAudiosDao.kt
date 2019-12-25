package com.adrienshen_n_vlad.jus_audio.persistence.dao

import androidx.room.*
import com.adrienshen_n_vlad.jus_audio.persistence.entities.RecommendedAudios
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.QUERY_LIMIT
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.RECENTLY_RECOMMENDED_TABLE_NAME
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.ROW_ID

@Dao
interface RecommendedAudiosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAudio(recommendedAudio: RecommendedAudios): Long

    @Query("SELECT * FROM $RECENTLY_RECOMMENDED_TABLE_NAME ORDER BY $ROW_ID ASC LIMIT :limit OFFSET :offset")
    fun getRecommendedAudios(offset: Int = 0, limit: Int = QUERY_LIMIT): List<RecommendedAudios>?

    @Query("DELETE FROM $RECENTLY_RECOMMENDED_TABLE_NAME")
    fun deleteAllPreviousRecommendations()

    @Delete
    fun deleteFromRecommended(recommendedAudio: RecommendedAudios)
}