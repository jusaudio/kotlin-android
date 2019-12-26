package com.adrienshen_n_vlad.jus_audio.content_providers

import android.util.Log
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.DEFAULT_LANG_ID
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.QUERY_LIMIT

class JusAudiosContentProvider {


    companion object {
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
        Log.d("returning from server", "start at  : $offset in ${dummySearchList.size}")
        val result = ArrayList<JusAudios>()
        var counter = 0
        while (counter < QUERY_LIMIT) {
            if (startAt < (dummySearchList.size - 1)) {

                Log.d("server says", "found ${dummySearchList[startAt].audioTitle}")
                result.add(dummySearchList[startAt])
            } else {
                Log.d("server says", "No more results")
                break
            }
            startAt += 1
            counter += 1
        }
        return result

    }


    fun searchAudio(query: String, offset: Int = 0): ArrayList<JusAudios> {

        var startAt = offset
        Log.d("returning from server", "searching for $query , offset by $offset")
        val result = ArrayList<JusAudios>()
        var counter = 0
        while (counter < QUERY_LIMIT) {
            if (startAt < (dummySearchList.size - 1)) {

                val tags = dummySearchList[startAt].audioSearchTags
                if (tags.contains(query, ignoreCase = true)) {
                    Log.d("server says", "found ${dummySearchList[startAt].audioTitle}")
                    result.add(dummySearchList[startAt])
                }


            } else {
                Log.d("server says", "No more results")
                break
            }
            startAt += 1
            counter += 1
        }
        return result
    }


    fun getRecommendations(offset: Int = 0): ArrayList<JusAudios> = generateDummyResults(offset)
}