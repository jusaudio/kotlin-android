package com.curiolabs.jusaudio.ui.rv_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.curiolabs.jusaudio.R
import com.curiolabs.jusaudio.persistence.entities.JusAudios

class RecommendedListAdapter(
    private val recommendedItems: ArrayList<JusAudios>,
    private val recommendedItemClickListener: RecommendedItemClickListener
) : RecyclerView.Adapter<RecommendedListAdapter.RecommendedListViewHolder>() {

    interface RecommendedItemClickListener {
        fun onRecommendedAudioClicked(adapterPos: Int, clickedAudio: JusAudios)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendedListViewHolder {
        val viewItem = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_recommended, parent, false)
        return RecommendedListViewHolder(
            viewItem,
            recommendedItemClickListener
        )
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

        private val toggleInCollectionIv =
            itemView.findViewById<ImageView>(R.id.toggle_in_collection_iv)

        fun bindData(recommendedItem: JusAudios) {
            /*todo Glide.with(itemView.context)
                 .load(recommendedItem.audioCoverThumbnailUrl)
                 .placeholder(R.drawable.ic_music_note_black)
                 .error(R.drawable.ic_music_note_black)
                 .into(audioCoverIv) */

            audioTitleTv.text = recommendedItem.audioTitle
            audioAuthorTv.text = recommendedItem.audioAuthor

            if (recommendedItem.audioIsInMyCollection)
                toggleInCollectionIv.setImageDrawable(
                    ContextCompat.getDrawable(
                        toggleInCollectionIv.context,
                        R.drawable.ic_playlist_added_black_24dp
                    )
                )
            else
                toggleInCollectionIv.setImageDrawable(
                    ContextCompat.getDrawable(
                        toggleInCollectionIv.context,
                        R.drawable.ic_playlist_add_black_24dp
                    )
                )


            audioCoverIv.setOnClickListener {
                recommendedItemClickListener.onRecommendedAudioClicked(
                    adapterPosition,
                    recommendedItem
                )
            }
        }
    }
}