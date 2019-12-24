package com.adrienshen_n_vlad.jus_audio.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.adrienshen_n_vlad.jus_audio.R


class HomeFragment : Fragment() {

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


}
