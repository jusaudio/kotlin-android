package com.adrienshen_n_vlad.jus_audio.ui.fragments.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.adrienshen_n_vlad.jus_audio.R
import com.adrienshen_n_vlad.jus_audio.ui.rv_adapters.PlayListAdapter
import com.adrienshen_n_vlad.jus_audio.ui.rv_adapters.RecommendedListAdapter


class HomeFragment : Fragment(), RecommendedListAdapter.RecommendedItemClickListener,
    PlayListAdapter.PlayListItemClickListener {

    private val homeViewModel: HomeViewModel by lazy {
        ViewModelProviders.of(this).get(HomeViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    private lateinit var menuIv: ImageView
    private lateinit var recommendationsRv: RecyclerView
    private lateinit var playlistRv: RecyclerView
    private lateinit var recommendedListAdapter: RecommendedListAdapter
    private lateinit var playlistAdapter: PlayListAdapter
    private var currentlyLoadingRecommendedList: Boolean = true
    private var currentlyLoadingHistoryList: Boolean = true
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuIv = view.findViewById(R.id.menu_iv)
        recommendationsRv = view.findViewById(R.id.recommendations_rv)
        playlistRv = view.findViewById(R.id.playlist_rv)

        //user clicks search
        view.findViewById<TextView>(R.id.search_bar_tv)
            .setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
            }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //init recyclers
        setupPlayListRv()
        setupRecommendedListRv()

        //observe state
        homeViewModel.observeRecommendedListState()
            .observe(viewLifecycleOwner, Observer { recommendedListState ->

                when (recommendedListState) {
                    HomeViewModel.State.LOADING -> {
                        currentlyLoadingRecommendedList = true
                    }
                    HomeViewModel.State.LOADED -> {
                        currentlyLoadingRecommendedList = false
                        recommendedListAdapter.notifyDataSetChanged()
                    }
                    HomeViewModel.State.ITEM_ADDED -> {
                        recommendedListAdapter.notifyItemInserted(homeViewModel.recentlyModifiedRecommendedAudioPos)
                    }
                    HomeViewModel.State.ITEM_MODIFIED -> {
                        recommendedListAdapter.notifyItemChanged(homeViewModel.recentlyModifiedRecommendedAudioPos)
                    }
                    HomeViewModel.State.ITEM_REMOVED -> {
                        recommendedListAdapter.notifyItemRemoved(homeViewModel.recentlyModifiedRecommendedAudioPos)
                    }

                    null -> {
                    }
                }

            })

        homeViewModel.observePlayListState().observe(viewLifecycleOwner, Observer { playListState ->

            when (playListState) {
                HomeViewModel.State.LOADING -> {
                    currentlyLoadingHistoryList = true
                }
                HomeViewModel.State.LOADED -> {
                    currentlyLoadingHistoryList = false
                    playlistAdapter.notifyDataSetChanged()
                }
                HomeViewModel.State.ITEM_ADDED -> {
                    playlistRv.smoothScrollToPosition(0)
                    playlistAdapter.notifyItemInserted(homeViewModel.recentlyModifiedPlayListAudioPos)
                }
                HomeViewModel.State.ITEM_MODIFIED -> {
                    playlistAdapter.notifyItemChanged(homeViewModel.recentlyModifiedPlayListAudioPos)
                }
                HomeViewModel.State.ITEM_REMOVED -> {
                    playlistAdapter.notifyItemRemoved(homeViewModel.recentlyModifiedPlayListAudioPos)
                }

                null -> {
                }
            }
        })

    }

    private fun setupPlayListRv() {
        playlistAdapter = PlayListAdapter(
            homeViewModel.getPlayList(),
            playListItemClickListener = this
        )
        val playlistRvLayoutManager = LinearLayoutManager(context, VERTICAL, false)
        playlistRv.layoutManager = playlistRvLayoutManager
        playlistRv.adapter = playlistAdapter


        playlistRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView.layoutManager!!.itemCount
                val lastVisibleItemPosition = playlistRvLayoutManager.findLastVisibleItemPosition()
                if (!currentlyLoadingHistoryList
                    && totalItemCount == lastVisibleItemPosition + 1
                ) {
                    Log.d("OnScroll", "loading more playlist")
                    homeViewModel.loadPlayList()
                }
            }
        })

    }

    private fun setupRecommendedListRv() {
        recommendedListAdapter = RecommendedListAdapter(
            homeViewModel.getRecommendedList(),
            recommendedItemClickListener = this
        )
        val hLinearLayoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        recommendationsRv.layoutManager = hLinearLayoutManager
        recommendationsRv.adapter = recommendedListAdapter

        //scroll to add more
        recommendationsRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView.layoutManager!!.itemCount
                val lastVisibleItemPosition = hLinearLayoutManager.findLastVisibleItemPosition()
                if (!currentlyLoadingRecommendedList
                    && totalItemCount == lastVisibleItemPosition + 1
                ) {
                    Log.d("OnScroll", "loading more recommended")
                    homeViewModel.loadRecommendedAudios()
                }
            }
        })

    }

    override fun onAudioCoverClicked(adapterPos: Int) {
        homeViewModel.addRecommendedItemToPlayList(adapterPos)
    }


    override fun onFavIconClicked(adapterPosition: Int) {
        homeViewModel.toggleFavorite(adapterPosition)
    }

    override fun onPlayIconClicked(adapterPosition: Int) {
        //todo
    }

    override fun onRemoveIoonClicked(adapterPosition: Int) {
        homeViewModel.removeFromPlayList(adapterPosition)
    }


}
