package com.adrienshen_n_vlad.jus_audio.interfaces

interface AsyncResultListener {

    fun onCompleted(
        isSuccessful: Boolean = false,
        dataFetched: Any? = null,
        dataIsToBeSavedLocally : Boolean = false
    )
}
