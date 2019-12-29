package com.adrienshen_n_vlad.jus_audio.ui.fragments.search

import android.app.Application
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adrienshen_n_vlad.jus_audio.AudioDataRepository
import com.adrienshen_n_vlad.jus_audio.JusAudioApp
import com.adrienshen_n_vlad.jus_audio.background_work.DoAsync
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val audioDataRepository: AudioDataRepository by lazy {
        (application.applicationContext as JusAudioApp).audioDataRepository!!
    }

    private val results = ArrayList<JusAudios>()
    fun getResults(): ArrayList<JusAudios> = results
    var removedItemAtPos = 0
    var insertedItemsAtPos = 0
    var insertedItemsCount = 0
    var prevQueriedItem = ""

    enum class State {
        LOADING_NEW,
        LOADING_MORE,
        ADDED,
        LOADED,
        REMOVED
    }

    private fun setInsertedItemsRange(totalItemsInserted: Int) {
        insertedItemsAtPos = results.size
        insertedItemsCount = totalItemsInserted
    }

    private val searchState = MutableLiveData<State>()
    fun observeSearchState(): LiveData<State> = searchState

    private var offsetResultsBy = 0
    private var asyncLoader: AsyncTask<() -> Any?, Void, Any?>? = null

    private val resultCallback = object : DoAsync.AsyncOperationListener {
        override fun onCompleted(isSuccessful: Boolean, dataFetched: Any?) {

            offsetResultsBy += JusAudioConstants.QUERY_LIMIT

            when {
                isSuccessful
                        && dataFetched != null
                        && dataFetched is ArrayList<*>
                        && dataFetched.size > 0 -> {

                    val dataToAdd = dataFetched as ArrayList<JusAudios>

                    if (offsetResultsBy == JusAudioConstants.QUERY_LIMIT) {
                        //new data is being loaded
                        results.addAll(dataToAdd)
                        searchState.value = State.LOADED
                    } else {
                        //more data is being added
                        setInsertedItemsRange(dataFetched.size)
                        results.addAll(dataToAdd)
                        searchState.value = State.ADDED
                    }
                    Log.d("loadResults", "completed " + dataFetched.size.toString())

                }
                isSuccessful
                        && offsetResultsBy < 100 -> {

                    //todo? try the next batch 10 more times
                    loadResults(queryItem = prevQueriedItem, withOffset = true)

                }
                else -> searchState.value = State.LOADED
            }
            Log.d("loadResults", "loaded")
        }
    }

    fun loadResults(queryItem: String, withOffset: Boolean) {
        if (!withOffset) offsetResultsBy = 0

        //todo? batch limited to 100
        if (offsetResultsBy < 100) {

            Log.d("loadResults", "called")

            prevQueriedItem = queryItem

            if (asyncLoader != null && asyncLoader!!.status == AsyncTask.Status.RUNNING) {
                asyncLoader!!.cancel(true)
                asyncLoader = null
                Log.d("loadResults", "Previous Search Cancelled")
            }

            asyncLoader = DoAsync(resultCallback)
                .execute({
                    audioDataRepository.findAudio(queryItem, offset = offsetResultsBy)
                })

            if (offsetResultsBy == 0) {
                results.clear()
                searchState.value = State.LOADING_NEW
            } else
                searchState.value = State.LOADING_MORE

            Log.d("loadResults", "loading batch : offset $offsetResultsBy")
        }
    }

    fun addToMyCollection(itemPos : Int , audioClicked : JusAudios){
        DoAsync()
            .execute({
                audioDataRepository.addToHistory(audioClicked)
            })
        results.removeAt(itemPos)
        removedItemAtPos = itemPos
        searchState.value = State.REMOVED
    }

}
