package com.adrienshen_n_vlad.jus_audio.ui.fragments.home

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
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
import com.adrienshen_n_vlad.jus_audio.R
import com.adrienshen_n_vlad.jus_audio.background_work.AudioPlayerService
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios
import com.adrienshen_n_vlad.jus_audio.ui.rv_adapters.MyCollectionAdapter
import com.adrienshen_n_vlad.jus_audio.ui.rv_adapters.RecommendedListAdapter
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
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
    private lateinit var audioPlayerView : PlayerView
    private lateinit var audioTitleTv : TextView
    private lateinit var audioAuthorTv : TextView
    private lateinit var audioPlayerHintTv: TextView
    private lateinit var audioService: AudioPlayerService
    private var audioServiceIsBound: Boolean = false
    private val audioServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d(LOG_TAG, "audioServiceConnection.onServiceConnected()")
            val binder = service as AudioPlayerService.LocalBinder
            audioService = binder.getService()
            audioServiceIsBound = true
            notifyServiceOnPlaylistChanges()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d(LOG_TAG, "audioServiceConnection.onDisconnected()")
            audioServiceIsBound = false
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuIv = view.findViewById(R.id.menu_iv)
        recommendationsRv = view.findViewById(R.id.recommendations_rv)
        myCollectionRv = view.findViewById(R.id.my_collection_rv)
        view.findViewById<TextView>(R.id.audio_player_hint_tv)
            .setOnClickListener { expandBottomPlayer() }

        audioPlayerView = view.findViewById(R.id.audio_player_view)
        audioTitleTv = view.findViewById(R.id.audio_title_tv)
        audioAuthorTv = view.findViewById(R.id.audio_author_tv)
        audioPlayerHintTv = view.findViewById(R.id.audio_player_hint_tv)
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
                        notifyServiceOnPlaylistChanges()
                    }
                    HomeViewModel.DataState.ITEM_ADDED -> {
                        val pos = homeViewModel.recentlyModifiedMyCollectionPos
                        myCollectionRv.smoothScrollToPosition(pos)
                        myCollectionAdapter.notifyItemInserted(pos)
                        notifyServiceOnPlaylistInsertion(pos)
                    }
                    HomeViewModel.DataState.ITEM_MODIFIED -> {
                        myCollectionAdapter.notifyItemChanged(homeViewModel.recentlyModifiedMyCollectionPos)
                    }
                    HomeViewModel.DataState.ITEM_REMOVED -> {
                        val pos  = homeViewModel.recentlyModifiedMyCollectionPos
                        myCollectionAdapter.notifyItemRemoved(pos)
                        notifyServiceOnPlaylistDeletion(pos)
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


    override fun onPlayIconClicked(clickedAudio: JusAudios) {
        homeViewModel.setCurrentlyPlayingFromAudioPos(clickedAudio)
        playAudio()
    }


    /*************** AUDIO PLAYER ********************************/
    private val audioPlaybackStateListener: Player.EventListener by lazy {
        object : Player.EventListener {
            override fun onPositionDiscontinuity(@Player.DiscontinuityReason reason: Int) {
                audioTitleTv.text = audioService.getTitleAndAuthor()[0]
                audioAuthorTv.text = audioService.getTitleAndAuthor()[0]
                Log.d(LOG_TAG, "audioPBackStateListener - onPositionDiscontinuity $reason")
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    ExoPlayer.STATE_IDLE -> {
                        audioPlayerHintTv.setText(R.string.app_name)
                    }
                    ExoPlayer.STATE_BUFFERING -> {
                        audioPlayerHintTv.setText(R.string.buffering_txt)
                    }
                    ExoPlayer.STATE_READY -> {
                        audioPlayerHintTv.setText(R.string.now_playing_txt)
                    }
                    ExoPlayer.STATE_ENDED -> {
                        audioPlayerHintTv.setText(R.string.app_name)
                    }
                    else -> {
                        audioPlayerHintTv.setText(R.string.app_name)
                    }
                }

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
    private fun startAndBindToAudioService(){
        if (!audioServiceIsBound) {
            Intent(context!!, AudioPlayerService::class.java).also { intent ->
                Util.startForegroundService(context!!, intent)
                context!!.bindService(intent, audioServiceConnection, Context.BIND_AUTO_CREATE)
            }
        }
    }


    private fun notifyServiceOnPlaylistChanges(){
        Log.d(LOG_TAG,"notifyServiceOnPlaylistChanges() called")
        if(audioServiceIsBound){
            Log.d(LOG_TAG,"service is bound")
            audioService.loadPlayList(homeViewModel.myCollection)
        }
    }

    private fun notifyServiceOnPlaylistInsertion(position : Int){
        if(audioServiceIsBound){
            audioService.addToPlayList(position, homeViewModel.myCollection[position])
        }
    }

    private fun notifyServiceOnPlaylistDeletion(position : Int) {
        if (audioServiceIsBound) {
            audioService.removeFromPlayList(position)
        }

    }

    private fun expandBottomPlayer() {
        Log.d(LOG_TAG,"expandBottomPlayer() called")
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        Log.d(LOG_TAG,"expandBottomPlayer() finished at state " + bottomSheetBehavior.state.toString())
    }


    private fun playAudio() {
        if(audioServiceIsBound) {

            initAudioPlayer()
            if( homeViewModel.myCollection.size > 0 ) {
                try {
                    val audioToPlay =
                        homeViewModel.myCollection[homeViewModel.currentlyPlayingSongAtPos]
                    expandBottomPlayer()

                    Log.d(LOG_TAG, "playingAudio with exo player ${audioToPlay.audioTitle}")
                    audioService.playAudioInPlayList(audioToPlay)

                } catch (e: Exception) {
                    Log.d(LOG_TAG, "playingAudio, exception thrown ${e.message}", e.cause)
                }
            }
        }

    }

    private fun initAudioPlayer(){
        if(audioPlayerView.player == null)
            audioPlayerView.player = audioService.getPlayer()

        if(!audioService.listenerIsSet)
            audioService.setListener(audioPlaybackStateListener)
    }


    override fun onResume() {
        super.onResume()
        if(homeViewModel.observeMyCollectionState().value == HomeViewModel.DataState.LOADED)
            homeViewModel.reloadMyCollection()
        else
            Log.d( LOG_TAG,"Resumed " +   homeViewModel.observeMyCollectionState().value.toString() )
    }


    override fun onStart() {
        super.onStart()
        startAndBindToAudioService()
    }

    override fun onStop() {
        super.onStop()
        if(audioServiceIsBound) {
            audioService.removeListener(audioPlaybackStateListener)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        context!!.unbindService(audioServiceConnection)
        audioServiceIsBound = false
    }


}
