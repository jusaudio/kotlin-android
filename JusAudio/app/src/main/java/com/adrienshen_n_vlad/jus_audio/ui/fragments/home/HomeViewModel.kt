package com.adrienshen_n_vlad.jus_audio.ui.fragments.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adrienshen_n_vlad.jus_audio.AudioDataRepository
import com.adrienshen_n_vlad.jus_audio.JusAudioApp
import com.adrienshen_n_vlad.jus_audio.background_tasks.DoAsync
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.QUERY_LIMIT


class HomeViewModel(application: Application) : AndroidViewModel(application) {


    private val audioDataRepository: AudioDataRepository by lazy {
        (application.applicationContext as JusAudioApp).audioDataRepository!!
    }

    private val recommendedList = ArrayList<JusAudios>()
    fun getRecommendedList() = recommendedList

    private val playList = ArrayList<JusAudios>()
    fun getPlayList() = playList

    enum class State {
        LOADING,
        LOADED,
        ITEM_ADDED,
        ITEM_MODIFIED,
        ITEM_REMOVED
    }

    private val playListState = MutableLiveData<State>()
    fun observePlayListState(): LiveData<State> {
        return playListState
    }

    private val recommendedListState = MutableLiveData<State>()
    fun observeRecommendedListState(): LiveData<State> {
        return recommendedListState
    }


    var recentlyModifiedRecommendedAudioPos = -1
    var recentlyModifiedPlayListAudioPos = -1
    private var offsetRecommendedListBy = 0
    private var offsetPlayHistoryBy = 0
    var currentlyPlayingSongAtPos: Int = 0

    init {
        if (recommendedList.size == 0) loadRecommendedAudios()

        if (playList.size == 0) loadPlayList()
    }

    fun loadRecommendedAudios() {
        Log.d("loadRecommended", "called")
        if (recommendedList.size % QUERY_LIMIT == 0 || offsetRecommendedListBy - recommendedList.size <= QUERY_LIMIT) {

            Log.d("loadRecommended", "loading")
            val resultCallback = object : DoAsync.AsyncOperationListener {
                override fun onCompleted(isSuccessful: Boolean, dataFetched: Any?) {
                    if (isSuccessful) {
                        if (dataFetched != null && dataFetched is ArrayList<*> && dataFetched.size > 0) {
                            recommendedList.clear()
                            recommendedList.addAll((dataFetched as ArrayList<JusAudios>))
                            Log.d("loadRecommended", "completed " + dataFetched.size.toString())
                        }
                        offsetRecommendedListBy += QUERY_LIMIT
                        Log.d("loadRecommended", "completed successful : null")
                    }
                    recommendedListState.value = State.LOADED
                    Log.d("loadRecommended", "loaded")
                }
            }

            DoAsync(resultCallback)
                .execute({
                    audioDataRepository.getRecommendedAudios(offset = offsetRecommendedListBy)
                })
            recommendedListState.value = State.LOADING

        }
        //else we have fetched all recommended items


    }

    fun loadPlayList() {
        Log.d("loadPlayList", "called")
        if (playList.size % QUERY_LIMIT == 0 || offsetPlayHistoryBy - playList.size <= QUERY_LIMIT) {


            Log.d("loadPlayList", "loading")

            val resultCallback = object : DoAsync.AsyncOperationListener {
                override fun onCompleted(isSuccessful: Boolean, dataFetched: Any?) {
                    if (isSuccessful) {
                        if (dataFetched != null && dataFetched is ArrayList<*> && dataFetched.size > 0) {
                            playList.clear()
                            playList.addAll((dataFetched as ArrayList<JusAudios>))
                            Log.d("loadPlayList", "completed " + dataFetched.size.toString())
                        }
                        offsetPlayHistoryBy += QUERY_LIMIT
                        Log.d("loadPlayList", "completed successful : null")
                    }
                    playListState.value = State.LOADED


                    Log.d("loadPlayList", "loaded")
                }
            }

            DoAsync(resultCallback)
                .execute({
                    audioDataRepository.getHistory(offset = offsetPlayHistoryBy)
                })

            playListState.value = State.LOADING

        }
        //else we have fetched all playList items


    }

    fun toggleFavorite(itemPos: Int) {
        playList[itemPos].audioIsFavorite = !playList[itemPos].audioIsFavorite
        val audioToModiy = playList[itemPos]
        DoAsync()
            .execute({
                audioDataRepository.toggleFavoriteItem(audioToModiy)
            })
        recentlyModifiedPlayListAudioPos = itemPos
        playListState.value = State.ITEM_MODIFIED
    }

    fun clearHistory() {
        playList.clear()
        playListState.value = State.LOADED
        DoAsync()
            .execute({
                audioDataRepository.clearHistory()
            })
    }

    fun addRecommendedItemToPlayList(itemPos: Int) {

        val audio = recommendedList[itemPos]
        playList.add(0, audio)
        recentlyModifiedPlayListAudioPos = 0
        playListState.value = State.ITEM_ADDED

    }


    fun saveToHistory(itemPos: Int) {
        val audio = playList[itemPos]

        playList.remove(audio)
        recentlyModifiedPlayListAudioPos = itemPos
        playListState.value = State.ITEM_MODIFIED

        playList.add(audio)
        recentlyModifiedPlayListAudioPos = playList.size - 1
        playListState.value = State.ITEM_MODIFIED

        DoAsync()
            .execute({
                audioDataRepository.addToHistory(audio)
            })

    }

    fun removeFromPlayList(itemPos: Int) {
        val audio = playList[itemPos]
        playList.remove(audio)
        recentlyModifiedPlayListAudioPos = itemPos
        playListState.value = State.ITEM_MODIFIED
    }
}
