package com.curiolabs.jusaudio.ui.fragments.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.curiolabs.jusaudio.JusAudioApp
import com.curiolabs.jusaudio.background_work.DoFunAsync
import com.curiolabs.jusaudio.persistence.entities.JusAudios
import com.curiolabs.jusaudio.repository.AudioDataRepository
import com.curiolabs.jusaudio.utility_classes.JusAudioConstants.QUERY_LIMIT

private const val LOG_TAG = "HomeViewModel"

class HomeViewModel(application: Application) : AndroidViewModel(application) {


    private val audioDataRepository: AudioDataRepository by lazy {
        (application.applicationContext as JusAudioApp).audioDataRepository!!
    }


    val recommendedList = ArrayList<JusAudios>()
    val myCollection = ArrayList<JusAudios>()

    enum class DataState {
        LOADING,
        LOADED,
        ITEM_ADDED,
        ITEM_MODIFIED,
        ITEM_REMOVED
    }

    private val myCollectionState = MutableLiveData<DataState>()
    fun observeMyCollectionState(): LiveData<DataState> = myCollectionState

    private val recommendedListState = MutableLiveData<DataState>()
    fun observeRecommendedListState(): LiveData<DataState> = recommendedListState

    /** trackers **/
    var recentlyModifiedRecommendedAudioPos = -1
    var recentlyModifiedMyCollectionPos = -1


    /** searching ***/
    private var offsetRecommendedListBy = 0
    private var offsetMyCollectionBy = 0


    var currentlyPlayingSongAtPos: Int = 0

    init {
        loadRecommendedAudios()
        loadMyCollection()
    }

    fun loadRecommendedAudios() {
        Log.d(LOG_TAG, "loadRecommended() called")
        if (recommendedList.size == offsetMyCollectionBy) {

            Log.d(LOG_TAG, "loadRecommended() loading")
            val resultCallback = object : DoFunAsync.AsyncOperationListener {
                override fun onCompleted(isSuccessful: Boolean, dataFetched: Any?) {
                    if (isSuccessful) {
                        offsetRecommendedListBy += QUERY_LIMIT
                        if (dataFetched != null && dataFetched is ArrayList<*> && dataFetched.size > 0) {
                            recommendedList.clear()
                            recommendedList.addAll((dataFetched as ArrayList<JusAudios>))
                            Log.d(
                                LOG_TAG,
                                "loadRecommended() completed " + dataFetched.size.toString()
                            )
                        } else
                            Log.d(
                                LOG_TAG,
                                "loadRecommended() completed successful returned null or empty"
                            )
                    }
                    recommendedListState.value =
                        DataState.LOADED
                    Log.d(LOG_TAG, "loadRecommended() loaded")
                }
            }

            DoFunAsync(resultCallback)
                .execute({
                    audioDataRepository.getRecommendedAudios(offset = offsetRecommendedListBy)
                })
            recommendedListState.value =
                DataState.LOADING

        } else Log.d(LOG_TAG, "loadRecommended() we have fetched all recommended items")


    }

    fun loadMyCollection() {
        Log.d(
            LOG_TAG,
            "loadMyCollection() called with myCollection at size " + myCollection.size.toString()
        )
        if (myCollection.size == offsetMyCollectionBy) {


            Log.d(LOG_TAG, "loadMyCollection() loading")

            val resultCallback = object : DoFunAsync.AsyncOperationListener {
                override fun onCompleted(isSuccessful: Boolean, dataFetched: Any?) {
                    if (isSuccessful) {
                        offsetMyCollectionBy += QUERY_LIMIT
                        if (dataFetched != null && dataFetched is ArrayList<*> && dataFetched.size > 0) {
                            myCollection.clear()
                            myCollection.addAll((dataFetched as ArrayList<JusAudios>))
                            Log.d(
                                LOG_TAG,
                                "loadMyCollection() completed , returned size " + dataFetched.size.toString()
                            )
                        } else
                            Log.d(
                                LOG_TAG,
                                "loadMyCollection() completed successful returned null or empty"
                            )
                    }
                    myCollectionState.value =
                        DataState.LOADED
                    Log.d(LOG_TAG, "loadMyCollection() completed")
                }
            }

            DoFunAsync(resultCallback)
                .execute({
                    audioDataRepository.getMyCollection(offset = offsetMyCollectionBy)
                })

            myCollectionState.value =
                DataState.LOADING

        } else Log.d(LOG_TAG, "loadMyCollection() fetched all items")


    }

    fun toggleFavoriteAudio(
        myCollectionAdapterPos: Int,
        clickedAudio: JusAudios
    ) {
        Log.d(LOG_TAG, "toggleFavoriteAudio() is called")
        val itemPos = myCollection.indexOf(clickedAudio)
        val updatedAudio = clickedAudio.copy()
        updatedAudio.audioIsFavorite = !clickedAudio.audioIsFavorite


        myCollection[itemPos] = updatedAudio

        DoFunAsync()
            .execute({
                audioDataRepository.updateAudio(
                    ogAudio = clickedAudio,
                    modifiedAudio = clickedAudio
                )
            })
        recentlyModifiedMyCollectionPos = myCollectionAdapterPos
        myCollectionState.value =
            DataState.ITEM_MODIFIED
    }


    fun removeFromMyCollection(
        myCollectionAdapterPos: Int,
        clickedAudio: JusAudios
    ) {

        if(recommendedList.remove(clickedAudio)){
            //re-load
            val updatedAudio = clickedAudio.copy()
                updatedAudio.audioIsInMyCollection = false
            recommendedList.add(0, updatedAudio)
            recommendedListState.value =
                DataState.LOADED
        }

        Log.d(LOG_TAG, "removeFromMyCollection() is called")
        myCollection.remove(clickedAudio)
        val updatedAudio = clickedAudio.copy()
        updatedAudio.audioIsInMyCollection = false
        updatedAudio.audioIsFavorite = false  //since we are removing from collection

        DoFunAsync().execute({
            audioDataRepository.updateAudio(ogAudio = clickedAudio, modifiedAudio = updatedAudio)
        })
        recentlyModifiedMyCollectionPos = myCollectionAdapterPos
        myCollectionState.value =
            DataState.ITEM_REMOVED
    }

    fun updateRecommendedAudioForCollection(recommendedRvAdapterPos: Int, clickedAudio: JusAudios) {

        Log.d(LOG_TAG, "updateRecommendedAudioForCollection() is called")
        //toggle 
        val updatedAudio = clickedAudio.copy()
        val ogItemIsInCollection = clickedAudio.audioIsInMyCollection
        updatedAudio.audioIsInMyCollection = !ogItemIsInCollection


        //modifying item in recommendations
        val itemPos = recommendedList.indexOf(clickedAudio)
        recommendedList[itemPos] = updatedAudio
        recentlyModifiedRecommendedAudioPos = recommendedRvAdapterPos
        recommendedListState.value =
            DataState.ITEM_MODIFIED

        if (ogItemIsInCollection) {
            Log.d(LOG_TAG, "updateRecommendedAudioForCollection() - removing from collection")
            //removing audio from collection
            updatedAudio.audioIsFavorite = false  //since we are removing from collection
            myCollection.remove(clickedAudio)
            myCollectionState.value =
                DataState.LOADED //we are not sure of the adapter position

        } else {
            Log.d(LOG_TAG, "updateRecommendedAudioForCollection() - adding to collection")
            //adding audio to collection top
            myCollection.add(0, updatedAudio)
            recentlyModifiedMyCollectionPos = 0
            myCollectionState.value =
                DataState.ITEM_ADDED
        }

        DoFunAsync().execute({
            audioDataRepository.updateAudio(ogAudio = clickedAudio, modifiedAudio = updatedAudio)
        })


    }


    /*
    *** reload for any updates
     */
    fun reloadMyCollection() {
        Log.d(
            LOG_TAG,
            "reloadMyCollection() called with myCollection at size " + myCollection.size.toString()
        )
        Log.d(LOG_TAG, "reloadMyCollection() loading")
        val limit = QUERY_LIMIT + myCollection.size
        val resultCallback = object : DoFunAsync.AsyncOperationListener {
            override fun onCompleted(isSuccessful: Boolean, dataFetched: Any?) {
                if (isSuccessful) {
                    if (dataFetched != null && dataFetched is ArrayList<*> && dataFetched.size > 0) {
                        myCollection.clear()
                        myCollection.addAll((dataFetched as ArrayList<JusAudios>))
                        Log.d(
                            LOG_TAG,
                            "reloadMyCollection() completed , returned size " + dataFetched.size.toString()
                        )
                    } else
                        Log.d(
                            LOG_TAG,
                            "reloadMyCollection() completed successful returned null or empty"
                        )
                }
                offsetMyCollectionBy = myCollection.size
                myCollectionState.value =
                    DataState.LOADED
                Log.d(LOG_TAG, "reloadMyCollection() completed")
            }
        }

        DoFunAsync(resultCallback)
            .execute({
                audioDataRepository.getMyCollection(offset = 0, limit = limit)
            })

        myCollectionState.value =
            DataState.LOADING
    }


}
