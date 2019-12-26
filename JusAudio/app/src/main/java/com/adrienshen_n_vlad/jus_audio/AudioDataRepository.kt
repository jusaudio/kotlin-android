package com.adrienshen_n_vlad.jus_audio

import android.util.Log
import com.adrienshen_n_vlad.jus_audio.content_providers.JusAudiosContentProvider
import com.adrienshen_n_vlad.jus_audio.persistence.JusAudioDatabase
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios
import com.adrienshen_n_vlad.jus_audio.persistence.entities.PlayHistory
import com.adrienshen_n_vlad.jus_audio.persistence.entities.RecommendedAudios
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.SEARCH_QUERY_TXT_MIN_LENGTH

class AudioDataRepository(
    private val jusAudioContentProvider: JusAudiosContentProvider,
    private val jusAudioDatabase: JusAudioDatabase
) {

    //cache
    private val playHistoryCache = ArrayList<JusAudios>()
    private val recommendedAudiosCache = ArrayList<JusAudios>()

    /********* getters *********/
    fun findAudio(searchQuery: String, offset: Int = 0): ArrayList<JusAudios>? {
        Log.d("Repository_search", "findAudio() called")
        return if (searchQuery.trim().length > SEARCH_QUERY_TXT_MIN_LENGTH) {
            var foundAudios: List<JusAudios>? =
                jusAudioDatabase.jusAudiosFtsDao()
                    .findAudio(searchQuery = "*${searchQuery}*", offset = offset)
            if (foundAudios != null && foundAudios.isNotEmpty())
                foundAudios as ArrayList<JusAudios>
            else {
                foundAudios =
                    jusAudioContentProvider.searchAudio(query = searchQuery, offset = offset)
                if (foundAudios.isNotEmpty())
                    jusAudioDatabase.jusAudiosDao().insertAudios(foundAudios)
                foundAudios
            }
        } else null
    }

    fun getRecommendedAudios(offset: Int = 0): ArrayList<JusAudios>? {
        Log.d("Repository_recommended", "getRecommended() called")

        if (recommendedAudiosCache.size <= offset) {

            Log.d("Repository_recommended", "fetching from room")
            //poll the local database
            val recommendedAudios =
                jusAudioDatabase.recommendedAudiosDao().getRecommendedAudios(offset = offset)
            if (recommendedAudios != null && recommendedAudios.isNotEmpty()) {

                Log.d("Repository_recommended", "found in room")
                for (recommendedAudio in recommendedAudios) {
                    val completeAudioData = jusAudioDatabase.jusAudiosDao()
                        .selectByStreamUrl(recommendedAudio.audioStreamUrl)
                    recommendedAudiosCache.add(completeAudioData!!)
                }

            } else {

                Log.d("Repository_recommended", "fetching from server")
                //poll the server
                val recommendedAudiosFromServer =
                    jusAudioContentProvider.getRecommendations(offset = offset)
                if (recommendedAudiosFromServer.isNotEmpty()) {

                    Log.d("Repository_recommended", "found in server")
                    for (recommendedAudio in recommendedAudiosFromServer) {
                        val rowId = jusAudioDatabase.jusAudiosDao().insertAudio(recommendedAudio)
                        jusAudioDatabase.recommendedAudiosDao().insertAudio(
                            RecommendedAudios(
                                rowId = rowId,
                                audioStreamUrl = recommendedAudio.audioStreamUrl
                            )
                        )
                        recommendedAudiosCache.add(recommendedAudio)
                    }
                }

            }
        }


        Log.d("Repository_recommended", "returning cache")

        return recommendedAudiosCache


    }

    fun getHistory(offset: Int = 0): ArrayList<JusAudios>? {
        Log.d("Repository_history", "getHistory() called")
        if (playHistoryCache.size <= offset) {
            val audiosList = jusAudioDatabase.playHistoryDao().getHistory(offset = offset)
            if (audiosList != null && audiosList.isNotEmpty()) {
                Log.d("Repository_history", "found in local db")
                for (audio in audiosList) {
                    val completeAudioData = jusAudioDatabase.jusAudiosDao()
                        .selectByStreamUrl(audio.audioStreamUrl)
                    playHistoryCache.add(completeAudioData!!)
                }
            }
        }
        Log.d("Repository_history", "returning cache")
        return playHistoryCache
    }

    fun deleteFromHistory(audio: JusAudios) {
        playHistoryCache.remove(audio)
        jusAudioDatabase.playHistoryDao().deleteFromHistory(
            PlayHistory(
                audio.rowId!!.toLong(),
                audioStreamUrl = audio.audioStreamUrl
            )
        )
    }

    fun clearHistory() {
        playHistoryCache.clear()
        jusAudioDatabase.playHistoryDao().deleteAllHistory()
    }

    fun toggleFavoriteItem(audioToModify: JusAudios) {
        jusAudioDatabase.jusAudiosDao().updateAudios(audioToModify)
        updatePlayHistoryCache(audioToModify)
        updateRecommendedCache(audioToModify)

    }

    fun removeRecommendedAudio(rowId: Int, audioStreamUrl: String) {
        jusAudioDatabase.recommendedAudiosDao().deleteFromRecommended(
            RecommendedAudios(
                rowId = rowId.toLong(),
                audioStreamUrl = audioStreamUrl
            )
        )
        deleteItemInRecommendedCache(rowId = rowId)
    }

    private fun updatePlayHistoryCache(audioToModify: JusAudios) {
        //todo
    }

    private fun updateRecommendedCache(audioToModify: JusAudios) {
        //todo
    }

    private fun deleteItemInRecommendedCache(rowId: Int) {
        //todo
    }

    fun addToHistory(audio: JusAudios) {
        playHistoryCache.remove(audio)
        playHistoryCache.add(audio)
        jusAudioDatabase.playHistoryDao().insertAudio(
            PlayHistory(
                rowId = audio.rowId!!.toLong(),
                audioStreamUrl = audio.audioStreamUrl
            )
        )
    }


}