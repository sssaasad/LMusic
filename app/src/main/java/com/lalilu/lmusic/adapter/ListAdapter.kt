package com.lalilu.lmusic.adapter

import androidx.recyclerview.widget.DiffUtil
import com.lalilu.R
import com.lalilu.databinding.ItemListItemBinding
import com.lalilu.lmusic.domain.entity.MSong
import javax.inject.Inject

class ListAdapter @Inject constructor() :
    BaseAdapter<MSong, ItemListItemBinding>(R.layout.item_list_item) {

    override val itemCallback: DiffUtil.ItemCallback<MSong>
        get() = object : DiffUtil.ItemCallback<MSong>() {
            override fun areItemsTheSame(oldItem: MSong, newItem: MSong): Boolean {
                return oldItem.songId == newItem.songId
            }

            override fun areContentsTheSame(oldItem: MSong, newItem: MSong): Boolean {
                return oldItem.songId == newItem.songId &&
                        oldItem.songTitle == newItem.songTitle
            }
        }

    override fun onBind(binding: ItemListItemBinding, item: MSong) {
        binding.song = item
    }
}
