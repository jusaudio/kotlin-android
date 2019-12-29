package com.adrienshen_n_vlad.jus_audio.ui.fragments.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adrienshen_n_vlad.jus_audio.AudioDataRepository
import com.adrienshen_n_vlad.jus_audio.JusAudioApp
import com.adrienshen_n_vlad.jus_audio.background_work.DoAsync
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.QUERY_LIMIT


class HomeViewModel(application: Application) : AndroidViewModel(application) {


    private val audioDataRepository: AudioDataRepository by lazy {
        (application.applicationContext as JusAudioApp).audioDataRepository!!
    }

    val recommendedList = ArrayList<JusAudios>()

    val playList = ArrayList<JusAudios>()

    enum class DataState {
        LOADING,
        LOADED,
        ITEM_ADDED,
        ITEM_MODIFIED,
        ITEM_REMOVED
    }

    private val playListState = MutableLiveData<DataState>()
    fun observePlayHistoryState(): LiveData<DataState> {
        return playListState
    }

    private val recommendedListState = MutableLiveData<DataState>()
    fun observeRecommendedListState(): LiveData<DataState> {
        return recommendedListState
    }

    /********* audio player state ************/
    var playWhenReady: Boolean =  false
    var playbackPosition: Long = 0
    var currentWindow: Int = 0


    var recentlyModifiedRecommendedAudioPos = -1
    var recentlyModifiedPlayListAudioPos = -1
    private var offsetRecommendedListBy = 0
    private var offsetPlayHistoryBy = 0
    var currentlyPlayingSongAtPos: Int = 0

    init {
        if (recommendedList.size == 0) loadRecommendedAudios()

        if (playList.size == 0) loadPlayHistory()
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
                    recommendedListState.value = DataState.LOADED
                    Log.d("loadRecommended", "loaded")
                }
            }

            DoAsync(resultCallback)
                .execute({
                    audioDataRepository.getRecommendedAudios(offset = offsetRecommendedListBy)
                })
            recommendedListState.value = DataState.LOADING

        }
        //else we have fetched all recommended items


    }

    fun loadPlayHistory() {
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
                    playListState.value = DataState.LOADED


                    Log.d("loadPlayList", "loaded")
                }
            }

            DoAsync(resultCallback)
                .execute({
                    audioDataRepository.getHistory(offset = offsetPlayHistoryBy)
                })

            playListState.value = DataState.LOADING

        }
        //else we have fetched all playHistory items


    }

    fun toggleFavoriteAudio(
        itemPos: Int,
        audioToModify: JusAudios
    ) {
        playList[itemPos].audioIsFavorite = !playList[itemPos].audioIsFavorite
        DoAsync()
            .execute({
                audioDataRepository.toggleFavoriteItem(audioToModify)
            })
        recentlyModifiedPlayListAudioPos = itemPos
        playListState.value = DataState.ITEM_MODIFIED
    }


    fun addAudioToPlayListTop(audioClicked : JusAudios) {
        playList.add(0, audioClicked)
        recentlyModifiedPlayListAudioPos = 0
        playListState.value = DataState.ITEM_ADDED

        DoAsync()
            .execute({
                audioDataRepository.addToHistory(audioClicked)
            })
    }


    fun moveToPlayListBottom() {
        val audio = playList[currentlyPlayingSongAtPos]

        removeFromPlayList(currentlyPlayingSongAtPos,  audio)

        //bring all the way down
        playList.add(audio)
        recentlyModifiedPlayListAudioPos = playList.size - 1
        playListState.value = DataState.ITEM_MODIFIED

    }

    fun removeFromPlayList(
        itemPos: Int,
        audioToRemove: JusAudios
    ) {
        playList.remove(audioToRemove)
        recentlyModifiedPlayListAudioPos = itemPos
        playListState.value = DataState.ITEM_REMOVED
    }


}
