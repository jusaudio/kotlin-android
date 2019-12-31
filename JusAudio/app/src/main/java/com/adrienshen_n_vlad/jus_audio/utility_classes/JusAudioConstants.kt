package com.adrienshen_n_vlad.jus_audio.utility_classes

object JusAudioConstants {

    //searching
    const val QUERY_LIMIT = 10
    const val SEARCH_QUERY_TXT_MIN_LENGTH = 3 //minimal text in query to start searching

    //database tables
    const val DATABASE_NAME = "jus_audios_db"
    const val AUDIOS_FTS_TABLE_NAME = "jus_audios_fts_table"
    const val JUS_AUDIOS_TABLE_NAME = "jus_audios_table"

    //database columns
    const val ROW_ID = "rowid" //required to support @Fts4 tables in Room
    const val AUDIO_INFO_LANG_ID = "lid" //required to support @Fts4 tables in Room
    const val AUDIO_TITLE = "title"
    const val AUDIO_AUTHOR = "author"
    const val AUDIO_SEARCH_TAGS = "search_tags"
    const val AUDIO_STREAM_URL = "stream_url"
    const val AUDIO_COVER_THUMB_URL = "cover_thumbnail_url"
    const val AUDIO_IS_FAVORITE = "is_favorite"
    const val AUDIO_IS_RECOMMENDED = "is_recommended"
    const val AUDIO_IS_IN_MY_COLLECTION = "is_in_my_collection"
    const val DEFAULT_LANG_ID = 0

}