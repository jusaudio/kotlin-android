package com.adrienshen_n_vlad.jus_audio.background_work

import android.os.AsyncTask
import android.util.Log
import com.adrienshen_n_vlad.jus_audio.interfaces.AsyncResultListener

private const val LOG_TAG = "DoFunAsync"

class DoFunAsync(private val resultCallback: AsyncResultListener? = null) :
    AsyncTask<() -> Any?, Void, Any?>() {

    override fun doInBackground(vararg functions: (() -> Any?)?): Any? =
        try {
            functions[0]!!()
        } catch (exc: Exception) {
            Log.d(LOG_TAG, "Exception Thrown, ${exc.message}", exc.cause)
            null
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