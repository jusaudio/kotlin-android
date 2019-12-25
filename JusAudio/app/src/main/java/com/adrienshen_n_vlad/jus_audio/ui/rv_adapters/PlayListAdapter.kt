package com.adrienshen_n_vlad.jus_audio.ui.rv_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.adrienshen_n_vlad.jus_audio.R
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios

class PlayListAdapter(
    private val playListItems: ArrayList<JusAudios>,
    private val playListItemClickListener: PlayListItemClickListener
) : RecyclerView.Adapter<PlayListAdapter.PlayListViewHolder>() {

    interface PlayListItemClickListener {
        fun onFavIconClicked(adapterPosition: Int) {}
        fun onPlayIconClicked(adapterPosition: Int) {}
        fun onRemoveIoonClicked(adapterPosition: Int) {}

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListViewHolder {
        val viewItem = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_playlist, parent, false)
        return PlayListViewHolder(viewItem, playListItemClickListener)
    }

    override fun getItemCount(): Int {
        return playListItems.size
    }

    override fun onBindViewHolder(holder: PlayListViewHolder, position: Int) {
        val product = playListItems[position]
        holder.bindData(product)
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class PlayListViewHolder(
        itemView: View,
        private val playListItemClickListener: PlayListItemClickListener

    ) : RecyclerView.ViewHolder(itemView) {

        private val audioCoverIv =
            itemView.findViewById<ImageView>(R.id.audio_cover_iv)
        private val audioTitleTv = itemView.findViewById<TextView>(R.id.audio_title_tv)
        private val audioAuthorTv =
            itemView.findViewById<TextView>(R.id.audio_author_tv)
        private val removeFromPlaylistIv =
            itemView.findViewById<ImageView>(R.id.remove_from_playlist_iv)
        private val playNowIv =
            itemView.findViewById<ImageView>(R.id.play_now_iv)
        private val addToFavIv = itemView.findViewById<ImageView>(R.id.add_to_fav_iv)

        fun bindData(playListItem: JusAudios) {
            /*todo Glide.with(itemView.context)
                .load(playListItem.audioCoverThumbnailUrl)
                .placeholder(R.drawable.ic_music_note_black)
                .error(R.drawable.ic_music_note_black)
                .into(audioCoverIv)*/

            audioTitleTv.text = playListItem.audioTitle
            audioAuthorTv.text = playListItem.audioAuthor


            playNowIv.setOnClickListener {
                playListItemClickListener.onPlayIconClicked(adapterPosition)
            }

            removeFromPlaylistIv.setOnClickListener {
                playListItemClickListener.onRemoveIoonClicked(adapterPosition)
            }

            if (playListItem.audioIsFavorite) {
                addToFavIv.setImageDrawable(
                    ContextCompat.getDrawable(
                        addToFavIv.context,
                        R.drawable.ic_favorite_black_24dp
                    )
                )

            } else {
                addToFavIv.setImageDrawable(
                    ContextCompat.getDrawable(
                        addToFavIv.context,
                        R.drawable.ic_favorite_border_black_24dp
                    )
                )
            }

            addToFavIv.setOnClickListener {
                playListItemClickListener.onFavIconClicked(adapterPosition)
            }
        }
    }

}