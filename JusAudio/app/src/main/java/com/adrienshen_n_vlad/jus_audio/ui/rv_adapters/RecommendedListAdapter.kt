package com.adrienshen_n_vlad.jus_audio.ui.rv_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adrienshen_n_vlad.jus_audio.R
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios

class RecommendedListAdapter(
    private val recommendedItems: ArrayList<JusAudios>,
    private val recommendedItemClickListener: RecommendedItemClickListener
) : RecyclerView.Adapter<RecommendedListAdapter.RecommendedListViewHolder>() {

    interface RecommendedItemClickListener {
        fun onRecommendedAudioClicked(clickedAudio : JusAudios)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendedListViewHolder {
        val viewItem = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_recommended, parent, false)
        return RecommendedListViewHolder(viewItem, recommendedItemClickListener)
    }

    override fun getItemCount(): Int {
        return recommendedItems.size
    }

    override fun onBindViewHolder(holder: RecommendedListViewHolder, position: Int) {
        val recommendedItem = recommendedItems[position]
        holder.bindData(recommendedItem)
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }



    class RecommendedListViewHolder(
        itemView: View,
        private val recommendedItemClickListener: RecommendedItemClickListener

    ) : RecyclerView.ViewHolder(itemView) {

        private val audioCoverIv =
            itemView.findViewById<ImageView>(R.id.audio_cover_iv)
        private val audioTitleTv = itemView.findViewById<TextView>(R.id.audio_title_tv)
        private val audioAuthorTv =
            itemView.findViewById<TextView>(R.id.audio_author_tv)

        fun bindData(recommendedItem: JusAudios) {
            /*todo Glide.with(itemView.context)
                 .load(recommendedItem.audioCoverThumbnailUrl)
                 .placeholder(R.drawable.ic_music_note_black)
                 .error(R.drawable.ic_music_note_black)
                 .into(audioCoverIv) */

            audioTitleTv.text = recommendedItem.audioTitle
            audioAuthorTv.text = recommendedItem.audioAuthor

            audioCoverIv.setOnClickListener {
                recommendedItemClickListener.onRecommendedAudioClicked(recommendedItem)
            }
        }
    }
}