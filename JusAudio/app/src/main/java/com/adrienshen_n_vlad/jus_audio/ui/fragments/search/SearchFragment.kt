package com.adrienshen_n_vlad.jus_audio.ui.fragments.search

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adrienshen_n_vlad.jus_audio.R
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios
import com.adrienshen_n_vlad.jus_audio.ui.rv_adapters.SearchResultsAdapter
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.SEARCH_QUERY_TXT_MIN_LENGTH


private const val LOG_TAG = "SearchFragment"

class SearchFragment : Fragment(), SearchResultsAdapter.SearchResultClickListener {


    private val searchViewModel: SearchViewModel by lazy {
        ViewModelProviders.of(this).get(SearchViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    private lateinit var searchBarEt: EditText
    private lateinit var searchResultsRv: RecyclerView
    private lateinit var searchResultsAdapter: SearchResultsAdapter
    private lateinit var progressBar: ProgressBar
    private var currentlyLoading = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchBarEt = view.findViewById(R.id.search_bar_et)
        searchResultsRv = view.findViewById(R.id.search_results_rv)
        progressBar = view.findViewById(R.id.progress_bar)
        searchBarEt.afterTextChangedDelayed { searchQuery ->
            search(searchQuery)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        searchBarEt.requestFocus()
        setupRecycler()

        searchViewModel.observeSearchState().observe(viewLifecycleOwner, Observer { resultsState ->

            when (resultsState) {

                SearchViewModel.State.LOADING_NEW -> {
                    searchResultsAdapter.notifyDataSetChanged()
                }


                SearchViewModel.State.LOADED -> {
                    currentlyLoading = false
                    progressBar.visibility = View.INVISIBLE
                    searchResultsAdapter.notifyDataSetChanged()
                }

                SearchViewModel.State.ADDED -> {
                    currentlyLoading = false
                    progressBar.visibility = View.INVISIBLE
                    searchResultsAdapter.notifyItemRangeInserted(
                        searchViewModel.insertedItemsAtPos,
                        searchViewModel.insertedItemsCount
                    )
                }

                SearchViewModel.State.MODIFIED_AUDIO -> {
                    searchResultsAdapter.notifyItemChanged(
                        searchViewModel.modifiedItemAtPos
                    )
                }

                else -> {
                }

            }
        })

    }

    private fun setupRecycler() {
        searchResultsAdapter = SearchResultsAdapter(
            searchViewModel.getResults(),
            foundItemClickListener = this
        )
        val searchResultsRvLayoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        searchResultsRv.layoutManager = searchResultsRvLayoutManager
        searchResultsRv.adapter = searchResultsAdapter


        searchResultsRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView.layoutManager!!.itemCount
                val lastVisibleItemPosition =
                    searchResultsRvLayoutManager.findLastVisibleItemPosition()
                if (totalItemCount == lastVisibleItemPosition + 1
                ) {
                    Log.d(LOG_TAG, "Scrolled Down : loading more results")
                    search(withOffset = true)
                }
            }
        })
    }


    private fun search(
        searchQuery: String = searchBarEt.text.toString(),
        withOffset: Boolean = false
    ) {
        if (!currentlyLoading && searchQuery.length > SEARCH_QUERY_TXT_MIN_LENGTH) {
            progressBar.visibility = View.VISIBLE
            currentlyLoading = true
            searchViewModel.loadResults(queryItem = searchQuery, withOffset = withOffset)
        }
    }

    private fun EditText.afterTextChangedDelayed(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            var timer: CountDownTimer? = null

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                timer?.cancel()
                timer = object : CountDownTimer(1000, 1500) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        afterTextChanged.invoke(editable.toString())
                    }
                }.start()
            }
        })
    }


    override fun onAddToOrRemoveFromCollectionClicked(
        adapterPosition: Int,
        clickedItem: JusAudios
    ) {
        searchViewModel.addToOrRemoveFromMyCollection(adapterPosition, clickedItem)
    }
}
