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

class MyCollectionAdapter(
    private val myCollectionItems: ArrayList<JusAudios>,
    private val myCollectionItemClickListener: MyCollectionItemClickListener
) : RecyclerView.Adapter<MyCollectionAdapter.MyCollectionViewHolder>() {

    interface MyCollectionItemClickListener {
        fun onFavIconClicked(adapterPosition: Int, clickedAudio: JusAudios) {}
        fun onPlayIconClicked(adapterPosition: Int, clickedAudio: JusAudios) {}
        fun onRemoveIoonClicked(adapterPosition: Int, clickedAudio: JusAudios) {}

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCollectionViewHolder {
        val viewItem = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_my_collection, parent, false)
        return MyCollectionViewHolder(viewItem, myCollectionItemClickListener)
    }

    override fun getItemCount(): Int {
        return myCollectionItems.size
    }

    override fun onBindViewHolder(holder: MyCollectionViewHolder, position: Int) {
        val product = myCollectionItems[position]
        holder.bindData(product)
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun getItemAtPos(adapterPosition: Int): JusAudios = myCollectionItems[adapterPosition]

    class MyCollectionViewHolder(
        itemView: View,
        private val myCollectionItemClickListener: MyCollectionItemClickListener

    ) : RecyclerView.ViewHolder(itemView) {

        private val audioCoverIv =
            itemView.findViewById<ImageView>(R.id.audio_cover_iv)
        private val audioTitleTv = itemView.findViewById<TextView>(R.id.audio_title_tv)
        private val audioAuthorTv =
            itemView.findViewById<TextView>(R.id.audio_author_tv)
        private val removeFromPlaylistIv =
            itemView.findViewById<ImageView>(R.id.remove_from_playlist_iv)
        private val playNowIv =
            itemView.findViewById<ImageView>(R.id.add_or_remove_collection)
        private val addToFavIv = itemView.findViewById<ImageView>(R.id.add_to_fav_iv)

        fun bindData(myCollectionItem: JusAudios) {
            /*todo Glide.with(itemView.context)
                .load(myCollectionItem.audioCoverThumbnailUrl)
                .placeholder(R.drawable.ic_music_note_black)
                .error(R.drawable.ic_music_note_black)
                .into(audioCoverIv)*/

            audioTitleTv.text = myCollectionItem.audioTitle
            audioAuthorTv.text = myCollectionItem.audioAuthor


            playNowIv.setOnClickListener {
                myCollectionItemClickListener.onPlayIconClicked(adapterPosition, myCollectionItem)
            }

            removeFromPlaylistIv.setOnClickListener {
                myCollectionItemClickListener.onRemoveIoonClicked(adapterPosition, myCollectionItem)
            }

            if (myCollectionItem.audioIsFavorite) {
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
                myCollectionItemClickListener.onFavIconClicked(adapterPosition, myCollectionItem)
            }
        }
    }

}