package com.adrienshen_n_vlad.jus_audio.background_tasks

import android.os.AsyncTask

class DoAsync(private val resultCallback: AsyncOperationListener? = null) :
    AsyncTask<() -> Any?, Void, Any?>() {


    interface AsyncOperationListener {
        fun onCompleted(
            isSuccessful: Boolean = false,
            dataFetched: Any? = null
        )
    }

    override fun doInBackground(vararg functions: (() -> Any?)?): Any? {
        return functions[0]!!()
    }

    override fun onPostExecute(result: Any?) {
        super.onPostExecute(result)
        if (resultCallback != null) {
            when {
                result is Boolean -> resultCallback.onCompleted(isSuccessful = result)
                result != null -> resultCallback.onCompleted(
                    isSuccessful = true,
                    dataFetched = result
                )
                else -> resultCallback.onCompleted(isSuccessful = false)
            }
        }
    }


}