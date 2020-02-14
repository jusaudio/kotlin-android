package com.curiolabs.jusaudio.background_work

import android.os.AsyncTask
import android.util.Log

private const val LOG_TAG = "DoFunAsync"

class DoFunAsync(private val resultCallback: AsyncOperationListener? = null) :
    AsyncTask<() -> Any?, Void, Any?>() {


    interface AsyncOperationListener {
        fun onCompleted(
            isSuccessful: Boolean = false,
            dataFetched: Any? = null
        )
    }

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