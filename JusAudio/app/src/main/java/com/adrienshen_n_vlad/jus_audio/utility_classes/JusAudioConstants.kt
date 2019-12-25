package com.adrienshen_n_vlad.jus_audio.utility_classes

object JusAudioConstants {

    const val QUERY_LIMIT = 10

    //database tables
    const val DATABASE_NAME = "jus_audios_db"
    const val AUDIOS_TABLE_NAME = "jus_audios_table"
    const val HISTORY_TABLE_NAME = "history_table"
    const val RECENTLY_RECOMMENDED_TABLE_NAME = "recently_recommended_table"

    //database columns
    const val ROW_ID = "rowid" //required to support @Fts4 tables in Room
    const val AUDIO_INFO_LANG_ID = "lid" //required to support @Fts4 tables in Room
    const val AUDIO_TITLE = "audio_title"
    const val AUDIO_AUTHOR = "audio_author"
    const val AUDIO_STREAM_URL = "audio_stream_url"
    const val AUDIO_COVER_THUMB_URL = "audio_cover_thumbnail_url"
    const val AUDIO_IS_FAVORITE = "audio_is_fav"
    const val DEFAULT_LANG_ID = 0
}