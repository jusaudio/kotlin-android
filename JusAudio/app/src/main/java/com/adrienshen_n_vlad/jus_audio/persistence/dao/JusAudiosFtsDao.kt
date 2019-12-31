package com.adrienshen_n_vlad.jus_audio.persistence.dao

import androidx.room.Dao
import androidx.room.Query
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.AUDIOS_FTS_TABLE_NAME
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.JUS_AUDIOS_TABLE_NAME
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.QUERY_LIMIT
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.ROW_ID

@Dao
interface JusAudiosFtsDao {

    @Query("SELECT * FROM $JUS_AUDIOS_TABLE_NAME JOIN $AUDIOS_FTS_TABLE_NAME ON jus_audios_table.rowid == jus_audios_fts_table.rowid WHERE jus_audios_fts_table.search_tags  MATCH :searchQuery GROUP BY jus_audios_table.rowid ORDER BY $ROW_ID ASC LIMIT :limit OFFSET :offset")
    fun findAudio(searchQuery: String, offset: Int = 0, limit: Int = QUERY_LIMIT): List<JusAudios>

}