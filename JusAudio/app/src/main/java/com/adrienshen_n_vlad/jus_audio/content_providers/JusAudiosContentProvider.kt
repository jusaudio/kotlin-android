package com.adrienshen_n_vlad.jus_audio.content_providers

import android.content.Context
import android.net.Uri
import android.util.Log
import com.adrienshen_n_vlad.jus_audio.interfaces.AsyncResultListener
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.DEFAULT_LANG_ID
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.QUERY_LIMIT
import com.adrienshen_n_vlad.jus_audio.utility_classes.Urls
import com.adrienshen_n_vlad.jus_audio.utility_classes.VolleyRequestQueue
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

const val LOG_TAG = "ContentProvider"

class JusAudiosContentProvider(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: JusAudiosContentProvider? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: JusAudiosContentProvider(context).also {
                    INSTANCE = it

                }
            }

        val dummySearchList = ArrayList<JusAudios>().also {
            for (i in 1..100) {
                val numStr = i.toString()
                val songTitle = "Song $numStr"
                val songAuthor = "Authors $numStr"
                val streamUrl = "Url $numStr"
                val coverThumbUrl = "CoverUrl $numStr"
                val tagsBuilder = StringBuilder()
                    .append(generateTags(audioTitleOrAuthors = songTitle))
                    .append(generateTags(audioTitleOrAuthors = songAuthor))

                it.add(
                    JusAudios(
                        rowId = null,
                        audioTitle = songTitle,
                        audioAuthor = songAuthor,
                        audioStreamUrl = streamUrl,
                        audioSearchTags = tagsBuilder.toString(),
                        audioCoverThumbnailUrl = coverThumbUrl,
                        audioIsFavorite = false,
                        audioIsRecommended = false,
                        audioIsInMyCollection = false,
                        audioInfoLanguageId = DEFAULT_LANG_ID
                    )
                )
            }
        }

        private fun generateTags(audioTitleOrAuthors: String): String {
            val delimeter = "[^\\p{L}0-9']+".toRegex()
            val words = audioTitleOrAuthors.split(regex = delimeter)
            val tags = StringBuilder()
            val space = " "
            for (word in words) {
                tags.append(word)
                tags.append(space)
            }
            return tags.toString()
        }
    }

    private fun generateDummyResults(offset: Int = 0): ArrayList<JusAudios> {
        var startAt = offset
        Log.d(LOG_TAG, "fetching, start at  : $offset in ${dummySearchList.size}")
        val result = ArrayList<JusAudios>()
        var counter = 0
        while (counter < QUERY_LIMIT) {
            if (startAt < (dummySearchList.size - 1)) {

                Log.d(LOG_TAG, "found ${dummySearchList[startAt].audioTitle}")
                result.add(dummySearchList[startAt])
            } else {
                Log.d(LOG_TAG, "found NO more results")
                break
            }
            startAt += 1
            counter += 1
        }
        return result

    }


    fun searchYouTube(query: String, offset: Int = 0, asyncResultListener: AsyncResultListener){
        if(offset > 40 ) asyncResultListener.onCompleted(isSuccessful = true)

        else {

            Log.d(LOG_TAG, "searching for $query , offset by $offset")
            val url = Uri.parse(Urls.generateYTSearchUrl(query, offset)).toString()

            Log.d(LOG_TAG, "YouTube Search Url : $url")
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                Response.Listener { response ->
                    asyncResultListener.onCompleted(isSuccessful = true, dataFetched =  decodeYoutubeResult(query, response, offset), dataIsToBeSavedLocally = true )
                },
                Response.ErrorListener { error ->
                    Log.d(
                        LOG_TAG,
                        "YouTube Search, ${error.networkResponse.statusCode} Error Occurred"
                    )
                    asyncResultListener.onCompleted(isSuccessful = false )

                }
            )

            VolleyRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest)
        }

    }

    /**study hint : JSON snippet ******/
    private fun decodeYoutubeResult(
        query: String,
        response: JSONObject,
        offset: Int
    ): ArrayList<JusAudios> {
        val items = response.getJSONArray("items")
        val totalItems = items.length()

        /**since youtube returns all results regardless including ones we may have already fetched
        //filter according to offset*/
        var i = offset
        val audios = ArrayList<JusAudios>()
        while (i < totalItems) {
            val item = items.getJSONObject(i)
            val audio = item.getJSONObject("snippet")
            val audioTitle = audio.getString("title")
            val videoId = item.getJSONObject("id").getString("videoId")
            val audioStreamUrl = "https://www.youtube.com/watch?v=$videoId"
            val tagsBuilder = StringBuilder()
                .append(generateTags(audioTitleOrAuthors = audioTitle))
                .append(generateTags(audioTitleOrAuthors = query))
            val thumbnailUrl =
                audio.getJSONObject("thumbnails")
                    .getJSONObject("default")
                    .getString("url")

            audios.add(
                JusAudios(
                    rowId = null,
                    audioTitle = audioTitle,
                    audioAuthor = audio.getString("channelTitle"),
                    audioIsRecommended = false,
                    audioIsInMyCollection = false,
                    audioIsFavorite = false,
                    audioInfoLanguageId = DEFAULT_LANG_ID,
                    audioSearchTags = tagsBuilder.toString(),
                    audioStreamUrl = audioStreamUrl,
                    audioCoverThumbnailUrl = thumbnailUrl
                )
            )
            i += 1
        }
        return audios
    }


    fun getRecommendations(offset: Int = 0): ArrayList<JusAudios> {
        val dummyList = generateDummyResults(offset)
        for (audio in dummyList) audio.audioIsRecommended = true
        return dummyList
    }
}