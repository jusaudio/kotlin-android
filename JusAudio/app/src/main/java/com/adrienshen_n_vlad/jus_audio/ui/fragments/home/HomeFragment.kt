package com.adrienshen_n_vlad.jus_audio.ui.fragments.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.adrienshen_n_vlad.jus_audio.R
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios
import com.adrienshen_n_vlad.jus_audio.ui.rv_adapters.PlayListAdapter
import com.adrienshen_n_vlad.jus_audio.ui.rv_adapters.RecommendedListAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior


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
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var audioTitleTv: TextView
    private lateinit var audioAuthorTv: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var playIv: ImageView


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuIv = view.findViewById(R.id.menu_iv)
        recommendationsRv = view.findViewById(R.id.recommendations_rv)
        playlistRv = view.findViewById(R.id.playlist_rv)
        //audio player
        audioTitleTv = view.findViewById(R.id.audio_title_tv)
        audioAuthorTv = view.findViewById(R.id.audio_author_tv)
        seekBar = view.findViewById(R.id.seek_bar)
        seekBar.setOnTouchListener { _, _ -> true }


        view.findViewById<ImageView>(R.id.prev_iv)
            .setOnClickListener { skipToPrev() }
        view.findViewById<ImageView>(R.id.next_iv)
            .setOnClickListener { skipToNext() }
        playIv = view.findViewById(R.id.play_iv)
        playIv.setOnClickListener { togglePlayingAudio() }


        val audioPlayerBottomSheet =
            view.findViewById<ConstraintLayout>(R.id.audio_player_bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(audioPlayerBottomSheet)

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
        //todo? setupBottomSheet()

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


    /**todo? private fun setupBottomSheet() {
    bottomSheetBehavior.addBottomSheetCallback(object :
    BottomSheetBehavior.BottomSheetCallback() {

    override fun onStateChanged(bottomSheet: View, newState: Int) {

    when (newState) {
    BottomSheetBehavior.STATE_EXPANDED -> {

    }
    BottomSheetBehavior.STATE_DRAGGING -> {

    }
    BottomSheetBehavior.STATE_COLLAPSED -> {

    }
    BottomSheetBehavior.STATE_HALF_EXPANDED -> {

    }
    BottomSheetBehavior.STATE_HIDDEN -> {

    }
    BottomSheetBehavior.STATE_SETTLING -> {

    }
    }
    }

    override fun onSlide(bottomSheet: View, slideOffset: Float) {

    }

    })
    }**/

    private fun toggleBottomSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    /************ Recommended & Playlist items ********/
    override fun onAudioCoverClicked(adapterPos: Int) {
        homeViewModel.addRecommendedItemToPlayList(adapterPos)
    }


    override fun onFavIconClicked(adapterPosition: Int) {
        homeViewModel.toggleFavorite(adapterPosition)
    }

    override fun onPlayIconClicked(adapterPosition: Int) {
        val audio = playlistAdapter.getItemAtPos(adapterPosition)
        homeViewModel.currentlyPlayingSongAtPos = adapterPosition
        playItemOnPlayList(audio)
        toggleBottomSheet()
    }


    override fun onRemoveIoonClicked(adapterPosition: Int) {
        homeViewModel.removeFromPlayList(adapterPosition)
    }


    /*************** AUDIO PLAYER ********************************/

    private fun playItemOnPlayList(audio: JusAudios) {
        Log.d("play Audio", "playAudio called")
        audioTitleTv.text = audio.audioTitle
        audioAuthorTv.text = audio.audioAuthor
        seekBar.progress = 0
    }

    private fun skipToNext() {
        homeViewModel.currentlyPlayingSongAtPos += 1
        if (homeViewModel.currentlyPlayingSongAtPos == homeViewModel.getPlayList().size)
            homeViewModel.currentlyPlayingSongAtPos = 0
        val audio = playlistAdapter.getItemAtPos(homeViewModel.currentlyPlayingSongAtPos)
        playItemOnPlayList(audio)
    }

    private fun skipToPrev() {
        homeViewModel.currentlyPlayingSongAtPos -= 1
        if (homeViewModel.currentlyPlayingSongAtPos == -1)
            homeViewModel.currentlyPlayingSongAtPos = homeViewModel.getPlayList().size - 1
        val audio = playlistAdapter.getItemAtPos(homeViewModel.currentlyPlayingSongAtPos)
        playItemOnPlayList(audio)
    }

    private fun togglePlayingAudio() {
        //todo
    }


}
