package com.adrienshen_n_vlad.jus_audio.ui.rv_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adrienshen_n_vlad.jus_audio.R
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios

class SearchResultsAdapter(
    private val foundItems: ArrayList<JusAudios>,
    private val foundItemClickListener: SearchResultClickListener
) : RecyclerView.Adapter<SearchResultsAdapter.SearchResultsViewHolder>() {

    interface SearchResultClickListener {
        fun onPlayIconClicked(adapterPosition: Int) {}
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultsViewHolder {
        val viewItem = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_search_result, parent, false)
        return SearchResultsViewHolder(viewItem, foundItemClickListener)
    }

    override fun getItemCount(): Int {
        return foundItems.size
    }

    override fun onBindViewHolder(holder: SearchResultsViewHolder, position: Int) {
        val product = foundItems[position]
        holder.bindData(product)
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class SearchResultsViewHolder(
        itemView: View,
        private val foundItemClickListener: SearchResultClickListener

    ) : RecyclerView.ViewHolder(itemView) {

        private val audioCoverIv =
            itemView.findViewById<ImageView>(R.id.audio_cover_iv)
        private val audioTitleTv = itemView.findViewById<TextView>(R.id.audio_title_tv)
        private val audioAuthorTv =
            itemView.findViewById<TextView>(R.id.audio_author_tv)
        private val playNowIv =
            itemView.findViewById<ImageView>(R.id.play_now_iv)

        fun bindData(foundItem: JusAudios) {
            /*todo Glide.with(itemView.context)
                .load(foundItem.audioCoverThumbnailUrl)
                .placeholder(R.drawable.ic_music_note_black)
                .error(R.drawable.ic_music_note_black)
                .into(audioCoverIv)*/

            audioTitleTv.text = foundItem.audioTitle
            audioAuthorTv.text = foundItem.audioAuthor


            playNowIv.setOnClickListener {
                foundItemClickListener.onPlayIconClicked(adapterPosition)
            }
        }
    }
}