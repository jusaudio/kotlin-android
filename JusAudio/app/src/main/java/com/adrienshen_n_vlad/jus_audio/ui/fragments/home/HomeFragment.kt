package com.adrienshen_n_vlad.jus_audio.ui.fragments.home

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
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
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetBehavior


class HomeFragment : Fragment(), RecommendedListAdapter.RecommendedItemClickListener,
    PlayListAdapter.PlayListItemClickListener {

    private val homeViewModel: HomeViewModel by lazy {
        ViewModelProviders.of(this).get(HomeViewModel::class.java)
    }

    private val audioPlayer: SimpleExoPlayer by lazy {
        SimpleExoPlayer.Builder(context!!).build()
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
    private lateinit var audioPlayerView: PlayerView
    private lateinit var audioTitleTv: TextView
    private lateinit var audioPlayerHint : TextView
    private lateinit var skipToNextIBtn: ImageButton
    private lateinit var skipToPrevIBtn: ImageButton


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuIv = view.findViewById(R.id.menu_iv)
        recommendationsRv = view.findViewById(R.id.recommendations_rv)
        playlistRv = view.findViewById(R.id.playlist_rv)
        audioPlayerView = view.findViewById(R.id.audio_player_view)
        audioPlayerView.controllerHideOnTouch = false
        audioTitleTv = view.findViewById(R.id.audio_title_tv)
        audioPlayerHint = view.findViewById(R.id.audio_player_hint_tv)

        skipToPrevIBtn = view.findViewById(R.id.custom_prev_ib)
        skipToPrevIBtn.setOnClickListener { playPrevAudio() }

        skipToNextIBtn = view.findViewById(R.id.custom_next_ib)
        skipToNextIBtn.setOnClickListener { playNextAudio() }


        val audioPlayerBottomSheet =
            view.findViewById<LinearLayout>(R.id.audio_player_bottom_sheet)
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

        //observe state
        homeViewModel.observeRecommendedListState()
            .observe(viewLifecycleOwner, Observer { recommendedListState ->

                when (recommendedListState) {
                    HomeViewModel.DataState.LOADING -> {
                        currentlyLoadingRecommendedList = true
                    }
                    HomeViewModel.DataState.LOADED -> {
                        currentlyLoadingRecommendedList = false
                        recommendedListAdapter.notifyDataSetChanged()
                    }
                    HomeViewModel.DataState.ITEM_ADDED -> {
                        recommendedListAdapter.notifyItemInserted(homeViewModel.recentlyModifiedRecommendedAudioPos)
                    }
                    HomeViewModel.DataState.ITEM_MODIFIED -> {
                        recommendedListAdapter.notifyItemChanged(homeViewModel.recentlyModifiedRecommendedAudioPos)
                    }
                    HomeViewModel.DataState.ITEM_REMOVED -> {
                        recommendedListAdapter.notifyItemRemoved(homeViewModel.recentlyModifiedRecommendedAudioPos)
                    }

                    null -> {
                    }
                }

            })

        homeViewModel.observePlayHistoryState()
            .observe(viewLifecycleOwner, Observer { playListState ->

                when (playListState) {
                    HomeViewModel.DataState.LOADING -> {
                        currentlyLoadingHistoryList = true
                    }
                    HomeViewModel.DataState.LOADED -> {
                        currentlyLoadingHistoryList = false
                        playlistAdapter.notifyDataSetChanged()
                    }
                    HomeViewModel.DataState.ITEM_ADDED -> {
                        playlistRv.smoothScrollToPosition(0)
                        playlistAdapter.notifyItemInserted(homeViewModel.recentlyModifiedPlayListAudioPos)
                    }
                    HomeViewModel.DataState.ITEM_MODIFIED -> {
                        playlistAdapter.notifyItemChanged(homeViewModel.recentlyModifiedPlayListAudioPos)
                    }
                    HomeViewModel.DataState.ITEM_REMOVED -> {
                        playlistAdapter.notifyItemRemoved(homeViewModel.recentlyModifiedPlayListAudioPos)
                    }

                    null -> {
                    }
                }
            })

    }

    private fun setupPlayListRv() {
        playlistAdapter = PlayListAdapter(
            homeViewModel.playList,
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
                    homeViewModel.loadPlayHistory()
                }
            }
        })

    }

    private fun setupRecommendedListRv() {
        recommendedListAdapter = RecommendedListAdapter(
            homeViewModel.recommendedList,
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

    private fun showBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    /************ Recommended items ********/
    override fun onRecommendedAudioClicked(clickedAudio: JusAudios) {
        homeViewModel.addAudioToPlayListTop(
            clickedAudio
        )
    }

    /*********** play list items ***********/
    override fun onFavIconClicked(adapterPosition: Int, clickedAudio: JusAudios) {
        homeViewModel.toggleFavoriteAudio(
            adapterPosition,
            clickedAudio
        )
    }

    override fun onPlayIconClicked(adapterPosition: Int, clickedAudio: JusAudios) {
        homeViewModel.currentlyPlayingSongAtPos = adapterPosition
        playAudio(clickedAudio)
        showBottomSheet()
    }


    override fun onRemoveIoonClicked(adapterPosition: Int, clickedAudio: JusAudios) {
        homeViewModel.removeFromPlayList(adapterPosition, clickedAudio)
    }


    /*************** AUDIO PLAYER ********************************/
    private val audioPlaybackStateListener: Player.EventListener by lazy {
        object : Player.EventListener {
            override fun onPositionDiscontinuity(@Player.DiscontinuityReason reason: Int) {
                Log.d("audioPBackStateListener", "onPositionDiscontinuity")
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                val stateString: String
                when (playbackState) {
                    ExoPlayer.STATE_IDLE -> {
                        audioPlayerHint.setText(R.string.player_is_idle_txt)
                        stateString = "ExoPlayer.STATE_IDLE"
                    }
                    ExoPlayer.STATE_BUFFERING -> {
                        audioPlayerHint.setText(R.string.streaming_txt)
                        stateString = "ExoPlayer.STATE_BUFFERING"
                    }
                    ExoPlayer.STATE_READY -> {
                        audioPlayerHint.setText(R.string.now_playing_txt)
                        stateString = "ExoPlayer.STATE_READY"
                    }
                    ExoPlayer.STATE_ENDED -> {
                        stateString = "ExoPlayer.STATE_ENDED"
                        playNextAudio()
                    }
                    else -> {
                        stateString = "UNKNOWN_STATE"
                    }
                }

                Log.d(
                    "audioPBackStateListener", "changed state to " + stateString
                            + " playWhenReady: " + playWhenReady
                )
            }

            override fun onPlayerError(error: ExoPlaybackException) {
                super.onPlayerError(error)
                Log.d("audioPBackStateListener", "an error occurred ${error.message}", error.cause)
            }

        }


    }

    private fun buildMediaSource(audioUrlStr: String): MediaSource? {
        //todo use real audioUrlStr

        val uri = Uri.parse(getString(R.string.dummy_audio_url))

        return ProgressiveMediaSource.Factory(
            DefaultDataSourceFactory(
                context!!,
                getString(R.string.app_name)
            )
        )
            .setTag(homeViewModel.currentlyPlayingSongAtPos)
            .createMediaSource(uri)

    }

    private fun enableNextBtnState(){
        skipToNextIBtn.isEnabled = true
        skipToNextIBtn.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context!!, R.color.colorWhite))
    }


    private fun playAudio( audio: JusAudios) {

        Log.d( "bottom sheet is" ,   bottomSheetBehavior.state.toString()  )
        audioPlayerView.player = audioPlayer
        audioPlayer.addListener(audioPlaybackStateListener)
        audioTitleTv.text = audio.audioTitle

        try {
            Log.d("HomeFragment", "playingAudio with exo player ${audio.audioTitle}" )
            audioPlayer.playWhenReady = true
            audioPlayer.prepare(buildMediaSource(audio.audioStreamUrl)!!, true, true)
            enableNextBtnState()

        } catch (e: Exception) {
            Log.d("HomeFragment", "playingAudio, exception thrown ${e.message}", e.cause)
        }

    }

    private fun playNextAudio(){
        //play next song
        homeViewModel.currentlyPlayingSongAtPos += 1
        if(homeViewModel.playList.size == homeViewModel.currentlyPlayingSongAtPos)
            homeViewModel.currentlyPlayingSongAtPos = 0
        val audio = homeViewModel.playList[homeViewModel.currentlyPlayingSongAtPos]
        playAudio(audio)

    }


    private fun playPrevAudio(){

        homeViewModel.currentlyPlayingSongAtPos -= 1
        if(homeViewModel.currentlyPlayingSongAtPos == -1){
            homeViewModel.currentlyPlayingSongAtPos = homeViewModel.playList.size - 1
        }

        if(homeViewModel.playList.size > homeViewModel.currentlyPlayingSongAtPos) {
            val audio = homeViewModel.playList[homeViewModel.currentlyPlayingSongAtPos]
            playAudio(audio)
        }

    }

    /************** activity LIFE CYCLE ************/
    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        if (audioPlayer.playbackState == ExoPlayer.STATE_READY
            || audioPlayer.playbackState == ExoPlayer.STATE_BUFFERING  ) {
            //save state
            homeViewModel.playWhenReady = audioPlayer.playWhenReady
            homeViewModel.playbackPosition = audioPlayer.currentPosition
            homeViewModel.currentWindow = audioPlayer.currentWindowIndex
        }
        audioPlayer.release()
    }


}
