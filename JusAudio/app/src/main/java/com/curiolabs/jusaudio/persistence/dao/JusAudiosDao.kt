package com.curiolabs.jusaudio.persistence.dao

import androidx.room.*
import com.curiolabs.jusaudio.persistence.entities.JusAudios
import com.curiolabs.jusaudio.utility_classes.JusAudioConstants.AUDIO_IS_FAVORITE
import com.curiolabs.jusaudio.utility_classes.JusAudioConstants.AUDIO_IS_IN_MY_COLLECTION
import com.curiolabs.jusaudio.utility_classes.JusAudioConstants.AUDIO_IS_RECOMMENDED
import com.curiolabs.jusaudio.utility_classes.JusAudioConstants.JUS_AUDIOS_TABLE_NAME
import com.curiolabs.jusaudio.utility_classes.JusAudioConstants.QUERY_LIMIT
import com.curiolabs.jusaudio.utility_classes.JusAudioConstants.ROW_ID

@Dao
interface JusAudiosDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAudios(audiosList: ArrayList<JusAudios>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAudio(audio: JusAudios): Long


    @Query("SELECT * FROM $JUS_AUDIOS_TABLE_NAME WHERE $AUDIO_IS_IN_MY_COLLECTION= :isInCollection ORDER BY $ROW_ID ASC LIMIT :limit OFFSET :offset")
    fun getMyCollection(
        offset: Int = 0,
        isInCollection: Boolean = true,
        limit: Int = QUERY_LIMIT
    ): List<JusAudios>


    @Query("SELECT * FROM $JUS_AUDIOS_TABLE_NAME WHERE $AUDIO_IS_RECOMMENDED= :isRecommended ORDER BY $ROW_ID ASC LIMIT :limit OFFSET :offset")
    fun getRecommendations(
        offset: Int = 0,
        isRecommended: Boolean = true,
        limit: Int = QUERY_LIMIT
    ): List<JusAudios>


    @Query("SELECT * FROM $JUS_AUDIOS_TABLE_NAME WHERE $AUDIO_IS_FAVORITE = :isFavorite ORDER BY $ROW_ID ASC LIMIT :limit OFFSET :offset")
    fun getFavorites(
        isFavorite: Boolean = true,
        offset: Int = 0,
        limit: Int = QUERY_LIMIT
    ): List<JusAudios>


    /********************** updates and clearing ************************/
    @Update
    fun updateAudios(vararg jusAudios: JusAudios)

    @Update
    fun updateAudio(jusAudio: JusAudios)


    @Query("UPDATE $JUS_AUDIOS_TABLE_NAME SET $AUDIO_IS_IN_MY_COLLECTION= :isNotInCollection WHERE $AUDIO_IS_IN_MY_COLLECTION= :isInCollection")
    fun clearMyCollection(
        isInCollection: Boolean = true,
        isNotInCollection: Boolean = false
    )

    @Query("UPDATE $JUS_AUDIOS_TABLE_NAME SET $AUDIO_IS_RECOMMENDED = :isNotRecommended WHERE $AUDIO_IS_RECOMMENDED= :isRecommended")
    fun clearRecommendations(
        isRecommended: Boolean = true,
        isNotRecommended: Boolean = false
    )

    @Query("UPDATE $JUS_AUDIOS_TABLE_NAME SET $AUDIO_IS_FAVORITE= :isNotFavorite WHERE $AUDIO_IS_FAVORITE = :isFavorite")
    fun clearFavorites(
        isFavorite: Boolean = true,
        isNotFavorite: Boolean = false
    )

}