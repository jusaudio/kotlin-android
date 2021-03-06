package com.curiolabs.jusaudio.ui.fragments.search

import android.app.Application
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.curiolabs.jusaudio.JusAudioApp
import com.curiolabs.jusaudio.background_work.DoFunAsync
import com.curiolabs.jusaudio.persistence.entities.JusAudios
import com.curiolabs.jusaudio.repository.AudioDataRepository
import com.curiolabs.jusaudio.utility_classes.JusAudioConstants

private const val LOG_TAG = "SearchViewModel"

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val audioDataRepository: AudioDataRepository by lazy {
        (application.applicationContext as JusAudioApp).audioDataRepository!!
    }

    private val results = ArrayList<JusAudios>()
    fun getResults(): ArrayList<JusAudios> = results
    var insertedItemsAtPos = 0
    var modifiedItemAtPos = 0
    var insertedItemsCount = 0
    var prevQueriedItem = ""

    enum class State {
        LOADING_NEW,
        LOADING_MORE,
        LOADED,
        ADDED,
        MODIFIED_AUDIO
    }

    private fun setInsertedItemsRange(totalItemsInserted: Int) {
        insertedItemsAtPos = results.size
        insertedItemsCount = totalItemsInserted
    }

    private val searchState = MutableLiveData<State>()
    fun observeSearchState(): LiveData<State> = searchState

    private var offsetResultsBy = 0
    private var asyncLoader: AsyncTask<() -> Any?, Void, Any?>? = null

    private val resultCallback = object : DoFunAsync.AsyncOperationListener {
        override fun onCompleted(isSuccessful: Boolean, dataFetched: Any?) {
            offsetResultsBy += JusAudioConstants.QUERY_LIMIT
            if (isSuccessful
                && dataFetched != null
                && dataFetched is ArrayList<*>
                && dataFetched.size > 0
            ) {

                val dataToAdd = dataFetched as ArrayList<JusAudios>

                if (searchState.value == State.LOADING_NEW) {
                    //new data is being loaded
                    results.addAll(dataToAdd)
                    searchState.value =
                        State.LOADED

                } else if (searchState.value == State.LOADING_MORE) {
                    //more data is being added
                    setInsertedItemsRange(dataFetched.size)
                    results.addAll(dataToAdd)
                    searchState.value =
                        State.ADDED
                }
                Log.d(
                    LOG_TAG,
                    "loadResults()  returned data size " + dataFetched.size.toString()
                )

            } else if (isSuccessful) {

                Log.d(LOG_TAG, "loadResults()  returned no data")
                if (offsetResultsBy < 1000) {
                    loadResults(queryItem = prevQueriedItem, withOffset = true)
                }else
                    searchState.value =
                        State.LOADED
            } else {
                Log.d(LOG_TAG, "loadResults() an error occurred")
                searchState.value =
                    State.LOADED
            }
        }
    }


    fun loadResults(queryItem: String, withOffset: Boolean) {
        if (!withOffset) offsetResultsBy = 0


        Log.d(LOG_TAG, "loadResults() called")

        prevQueriedItem = queryItem

        if (asyncLoader != null && asyncLoader!!.status == AsyncTask.Status.RUNNING) {
            asyncLoader!!.cancel(true)
            asyncLoader = null
            Log.d(LOG_TAG, "loadResults() Previous Search Cancelled")
        }

        asyncLoader = DoFunAsync(resultCallback)
            .execute({
                audioDataRepository.findAudio(queryItem, offset = offsetResultsBy)
            })

        if (offsetResultsBy == 0) {
            results.clear()
            searchState.value =
                State.LOADING_NEW
        } else
            searchState.value =
                State.LOADING_MORE

        Log.d(LOG_TAG, "loadResults() loading batch : offset $offsetResultsBy")
    }


    fun addToOrRemoveFromMyCollection(adapterPosition: Int, clickedAudio: JusAudios) {
        Log.d(LOG_TAG, "addToOrRemoveFromMyCollection() called")

        val itemPos = results.indexOf(clickedAudio)

        //toggle and update
        val newAudio = clickedAudio.copy()
        newAudio.audioIsInMyCollection = !clickedAudio.audioIsInMyCollection
        DoFunAsync(resultCallback)
            .execute({
                audioDataRepository.updateAudio(ogAudio = clickedAudio, modifiedAudio = newAudio)
            })

        results[itemPos] = newAudio
        modifiedItemAtPos = adapterPosition
        searchState.value =
            State.MODIFIED_AUDIO

    }

}
