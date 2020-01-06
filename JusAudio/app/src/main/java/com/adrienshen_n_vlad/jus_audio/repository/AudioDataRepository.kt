package com.adrienshen_n_vlad.jus_audio.repository

import android.content.Context
import android.util.Log
import com.adrienshen_n_vlad.jus_audio.content_providers.JusAudiosContentProvider
import com.adrienshen_n_vlad.jus_audio.interfaces.AsyncResultListener
import com.adrienshen_n_vlad.jus_audio.persistence.JusAudioDatabase
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.QUERY_LIMIT
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.SEARCH_QUERY_TXT_MIN_LENGTH


private const val LOG_TAG = "AudioDataRepository"

class AudioDataRepository(
    private val context: Context
) {

    private val jusAudioContentProvider by lazy {
        JusAudiosContentProvider.getInstance(context)
    }

    private val jusAudioDatabase by lazy {
        JusAudioDatabase.getDatabase(context)
    }


    companion object {
        @Volatile
        private var INSTANCE: AudioDataRepository? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AudioDataRepository(context).also {
                    INSTANCE = it

                }
            }
    }

    //cache
    private val myCollectionCache = ArrayList<JusAudios>()
    private val recommendedAudiosCache = ArrayList<JusAudios>()

    /********* getters *********/
    fun findAudio(
        searchQuery: String,
        offset: Int = 0,
        asyncResultListener: AsyncResultListener
    ): ArrayList<JusAudios>? {
        Log.d(LOG_TAG, "findAudio() called")

        val results = ArrayList<JusAudios>()

        if (searchQuery.trim().length > SEARCH_QUERY_TXT_MIN_LENGTH) {


               results.addAll(
                   jusAudioDatabase.jusAudiosFtsDao()
                    .findAudio(searchQuery = "*${searchQuery}*", offset = offset)
               )

            if (results.isEmpty()) {
                //fetch from server and pass a listener
                Log.d(LOG_TAG, "fetching from server")
                jusAudioContentProvider.searchYouTube(
                    query = searchQuery,
                    offset = offset,
                    asyncResultListener = asyncResultListener
                )

            }
        }

     return results
    }

    fun saveFoundAudiosLocally(foundAudios: ArrayList<JusAudios>) {
        if (foundAudios.isNotEmpty()) {
            jusAudioDatabase.jusAudiosDao()
                .insertAudios(foundAudios)  //add to local database
        }
    }

    fun getRecommendedAudios(offset: Int = 0): ArrayList<JusAudios>? {
        Log.d(LOG_TAG, "getRecommendedAudios() called")

        if (recommendedAudiosCache.size <= offset) {

            Log.d(LOG_TAG, "fetching from local db")

            val recommendedAudios =
                jusAudioDatabase.jusAudiosDao().getRecommendations(offset = offset)

            if (recommendedAudios.isNotEmpty()) {

                recommendedAudiosCache.addAll(recommendedAudios) //add to cache

            } else {

                Log.d(LOG_TAG, "fetching from server")
                //poll the server
                val recommendedAudiosFromServer =
                    jusAudioContentProvider.getRecommendations(offset = offset)
                if (recommendedAudiosFromServer.isNotEmpty()) {

                    Log.d(LOG_TAG, "found in server")
                    jusAudioDatabase.jusAudiosDao()
                        .insertAudios(recommendedAudiosFromServer)  //add to local database

                    recommendedAudiosCache.addAll(recommendedAudiosFromServer) //add to cache
                }
            }

        }

        return recommendedAudiosCache


    }

    fun getMyCollection(offset: Int = 0, limit: Int = QUERY_LIMIT): ArrayList<JusAudios>? {
        Log.d(LOG_TAG, "getMyCollection() called offset $offset")
        if (myCollectionCache.size <= offset) {
            val audiosList =
                jusAudioDatabase.jusAudiosDao().getMyCollection(offset = offset, limit = limit)
            if (audiosList.isNotEmpty()) {
                Log.d(LOG_TAG, "found in local db " + audiosList.size.toString())
                myCollectionCache.addAll(audiosList)
            }
        }
        Log.d(LOG_TAG, "getMyCollection() returned cache size " + myCollectionCache.size.toString())
        return myCollectionCache
    }


    /******** setters *************/
    fun updateAudio(ogAudio: JusAudios, modifiedAudio: JusAudios) {
        Log.d(LOG_TAG, "updateAudio() ${modifiedAudio.audioTitle}")
        val posInCollection = myCollectionCache.indexOf(ogAudio)
        if (posInCollection >= 0) {
            myCollectionCache.removeAt(posInCollection)

        }

        if (modifiedAudio.audioIsInMyCollection) myCollectionCache.add(modifiedAudio)

        val posInRecommendations = recommendedAudiosCache.indexOf(ogAudio)
        if (posInRecommendations >= 0) recommendedAudiosCache[posInRecommendations] =
            modifiedAudio


        jusAudioDatabase.jusAudiosDao().updateAudio(modifiedAudio)
    }


    /************ deletions ************/
    fun clearMyCollection() {
        myCollectionCache.clear()
        jusAudioDatabase.jusAudiosDao().clearMyCollection()
    }

    fun clearRecommendations() {
        recommendedAudiosCache.clear()
        jusAudioDatabase.jusAudiosDao().clearRecommendations()
    }

    fun clearFavorites() {
        for (audio in myCollectionCache) {
            if (audio.audioIsFavorite) {
                myCollectionCache.removeAt(myCollectionCache.indexOf(audio))
                audio.audioIsFavorite = false
                myCollectionCache.add(audio)
            }
        }
        jusAudioDatabase.jusAudiosDao().clearFavorites()
    }


}