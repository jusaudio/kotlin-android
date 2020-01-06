package com.adrienshen_n_vlad.jus_audio.utility_classes

import java.net.URLEncoder

object Urls {

    private const val YOUTUBE_BASE_URL = "https://www.googleapis.com/youtube/v3/search"

    fun generateYTSearchUrl(query : String, offset: Int) : String {
        val maxResults = offset + JusAudioConstants.QUERY_LIMIT
        val url = StringBuilder()
            .append(YOUTUBE_BASE_URL)
            .append("?part=snippet&q=")
            .append(URLEncoder.encode(query, "UTF-8"))
            .append("&type=video&key=${JusAudioConstants.API_KEY}")
            .append("&maxResults=$maxResults")
        return url.toString()
    }
}