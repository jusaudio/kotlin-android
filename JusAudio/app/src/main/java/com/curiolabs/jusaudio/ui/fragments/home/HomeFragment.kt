package com.curiolabs.jusaudio.ui.fragments.home

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.curiolabs.jusaudio.R
import com.curiolabs.jusaudio.persistence.entities.JusAudios
import com.curiolabs.jusaudio.ui.rv_adapters.MyCollectionAdapter
import com.curiolabs.jusaudio.ui.rv_adapters.RecommendedListAdapter
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetBehavior

private const val LOG_TAG = "HomeFragment"

class HomeFragment : Fragment(), RecommendedListAdapter.RecommendedItemClickListener,
    MyCollectionAdapter.MyCollectionItemClickListener {

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
    private lateinit var myCollectionRv: RecyclerView
    private lateinit var recommendedListAdapter: RecommendedListAdapter
    private lateinit var myCollectionAdapter: MyCollectionAdapter
    private var currentlyLoadingRecommendedList: Boolean = true
    private var currentlyLoadingHistoryList: Boolean = true
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var audioPlayerView: PlayerView
    private lateinit var audioTitleTv: TextView
    private lateinit var skipToNextIBtn: ImageButton
    private lateinit var skipToPrevIBtn: ImageButton
    private lateinit var exoPlayerPlayListConcatenatingMediaSource: ConcatenatingMediaSource
    private var audioPlayer: SimpleExoPlayer? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuIv = view.findViewById(R.id.menu_iv)
        recommendationsRv = view.findViewById(R.id.recommendations_rv)
        myCollectionRv = view.findViewById(R.id.my_collection_rv)
        audioPlayerView = view.findViewById(R.id.audio_player_view)
        audioPlayerView.controllerHideOnTouch = false
        audioTitleTv = view.findViewById(R.id.audio_title_tv)
        view.findViewById<TextView>(R.id.audio_player_hint_tv)
            .setOnClickListener { expandBottomPlayer() }

        skipToPrevIBtn = view.findViewById(R.id.custom_prev_ib)
        skipToPrevIBtn.setOnClickListener { playPrevAudio() }

        skipToNextIBtn = view.findViewById(R.id.custom_next_ib)
        skipToNextIBtn.setOnClickListener { playNextAudio() }

        view.findViewById<ImageButton>(R.id.exo_play).setOnClickListener { playAudio() }


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
        setupMyCollectionRv()
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

        homeViewModel.observeMyCollectionState()
            .observe(viewLifecycleOwner, Observer { myCollectionState ->

                when (myCollectionState) {
                    HomeViewModel.DataState.LOADING -> {
                        currentlyLoadingHistoryList = true
                    }
                    HomeViewModel.DataState.LOADED -> {
                        currentlyLoadingHistoryList = false
                        myCollectionAdapter.notifyDataSetChanged()
                    }
                    HomeViewModel.DataState.ITEM_ADDED -> {
                        val pos = homeViewModel.recentlyModifiedMyCollectionPos
                        myCollectionRv.smoothScrollToPosition(pos)
                        myCollectionAdapter.notifyItemInserted(pos)
                    }
                    HomeViewModel.DataState.ITEM_MODIFIED -> {
                        myCollectionAdapter.notifyItemChanged(homeViewModel.recentlyModifiedMyCollectionPos)
                    }
                    HomeViewModel.DataState.ITEM_REMOVED -> {
                        myCollectionAdapter.notifyItemRemoved(homeViewModel.recentlyModifiedMyCollectionPos)
                    }

                    null -> {
                    }
                }
            })

    }

    /************ RECYCLER VIEWS **************/
    private fun setupMyCollectionRv() {
        myCollectionAdapter = MyCollectionAdapter(
            homeViewModel.myCollection,
            myCollectionItemClickListener = this
        )
        val myCollectionRvLayoutManager = LinearLayoutManager(context, VERTICAL, false)
        myCollectionRv.layoutManager = myCollectionRvLayoutManager
        myCollectionRv.adapter = myCollectionAdapter


        myCollectionRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView.layoutManager!!.itemCount
                val lastVisibleItemPosition =
                    myCollectionRvLayoutManager.findLastVisibleItemPosition()
                if (!currentlyLoadingHistoryList
                    && totalItemCount == lastVisibleItemPosition + 1
                ) {
                    Log.d(LOG_TAG, "OnScroll loading more in myCollection")
                    homeViewModel.loadMyCollection()
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
                    Log.d(LOG_TAG, "OnScroll() loading more recommendations")
                    homeViewModel.loadRecommendedAudios()
                }
            }
        })
    }


    /************ LIST ITEM CLICK LISTENERS ********/
    override fun onRecommendedAudioClicked(adapterPos: Int, clickedAudio: JusAudios) {
        //save in view_model
        homeViewModel.updateRecommendedAudioForCollection(adapterPos, clickedAudio)

    }

    override fun onFavIconClicked(adapterPosition: Int, clickedAudio: JusAudios) {
        homeViewModel.toggleFavoriteAudio(
            adapterPosition,
            clickedAudio
        )
    }

    override fun onRemoveIoonClicked(adapterPosition: Int, clickedAudio: JusAudios) {
        homeViewModel.removeFromMyCollection(adapterPosition, clickedAudio)
    }


    override fun onPlayIconClicked(adapterPosition: Int, clickedAudio: JusAudios) {
        homeViewModel.currentlyPlayingSongAtPos = adapterPosition
        playAudio()
    }


    /*************** AUDIO PLAYER ********************************/

    private fun expandBottomPlayer() {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private val audioPlaybackStateListener: Player.EventListener by lazy {
        object : Player.EventListener {
            override fun onPositionDiscontinuity(@Player.DiscontinuityReason reason: Int) {
                Log.d(LOG_TAG, "audioPBackStateListener - onPositionDiscontinuity")
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                val stateString: String
                when (playbackState) {
                    ExoPlayer.STATE_IDLE -> {
                        stateString = "ExoPlayer.STATE_IDLE"
                    }
                    ExoPlayer.STATE_BUFFERING -> {
                        stateString = "ExoPlayer.STATE_BUFFERING"
                    }
                    ExoPlayer.STATE_READY -> {
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
                    LOG_TAG,
                    "audioPBackStateListener- changed state to " + stateString
                            + " playWhenReady: " + playWhenReady
                )
            }

            override fun onPlayerError(error: ExoPlaybackException) {
                super.onPlayerError(error)
                Log.d(
                    LOG_TAG,
                    "audioPBackStateListener() an error occurred ${error.message}",
                    error.cause
                )
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

    private fun initAudioPlayer(){
        //player state
        audioPlayer = SimpleExoPlayer.Builder(context!!).build()
        exoPlayerPlayListConcatenatingMediaSource = ConcatenatingMediaSource()
        audioPlayerView.player = audioPlayer
        audioPlayer!!.addListener(audioPlaybackStateListener)
    }


    private fun playAudio() {
        if(audioPlayer == null) initAudioPlayer()
        if (homeViewModel.myCollection.size > 0) {
            val audioToPlay = homeViewModel.myCollection[homeViewModel.currentlyPlayingSongAtPos]
            expandBottomPlayer()

            audioTitleTv.text = audioToPlay.audioTitle

            try {
                Log.d(LOG_TAG, "playingAudio with exo player ${audioToPlay.audioTitle}")
                audioPlayer!!.playWhenReady = true
                val mediaSourceToPlay =
                    buildMediaSource(homeViewModel.myCollection[homeViewModel.currentlyPlayingSongAtPos].audioStreamUrl)
                audioPlayer!!.prepare(mediaSourceToPlay!!, true, true)

            } catch (e: Exception) {
                Log.d(LOG_TAG, "playingAudio, exception thrown ${e.message}", e.cause)
            }
        }

    }

    private fun playNextAudio() {
        //play next song
        homeViewModel.currentlyPlayingSongAtPos += 1
        if (homeViewModel.myCollection.size <= homeViewModel.currentlyPlayingSongAtPos)
            homeViewModel.currentlyPlayingSongAtPos = 0
        playAudio()

    }


    private fun playPrevAudio() {

        homeViewModel.currentlyPlayingSongAtPos -= 1
        if (homeViewModel.currentlyPlayingSongAtPos < 0) {
            homeViewModel.currentlyPlayingSongAtPos = homeViewModel.myCollection.size - 1
        }

        if (homeViewModel.myCollection.size > homeViewModel.currentlyPlayingSongAtPos) {
            playAudio()
        }

    }

    override fun onResume() {
        super.onResume()
        if(homeViewModel.observeMyCollectionState().value == HomeViewModel.DataState.LOADED)
            homeViewModel.reloadMyCollection()
        else
            Log.d(LOG_TAG,"Resumed " +   homeViewModel.observeMyCollectionState().value.toString() )
    }


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
        if(audioPlayer != null) {
            audioPlayer!!.release()
            audioPlayer = null
        }
    }



}
