package com.example.barcodereader.fragments.inventoryScanFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.barcodereader.databaes.InventoryItem
import com.example.barcodereader.databinding.ItemPropertiesViewHolderBinding


class InventoryScanAdapter :
    ListAdapter<InventoryItem, InventoryScanAdapter.ViewHolder>(DiffUtilCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), currentList, position)
    }

    class ViewHolder(val binding: ItemPropertiesViewHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        lateinit var inventoryItem: InventoryItem

        fun bind(property: InventoryItem, currentList: MutableList<InventoryItem>, position: Int) {
            binding.inventoryItem = property
            inventoryItem = property
            itemClickLister(position, currentList)
            binding.executePendingBindings()
        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemPropertiesViewHolderBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return ViewHolder(binding)
            }
        }

        private fun itemClickLister(position: Int, currentList: MutableList<InventoryItem>) {
            binding.removeButton.setOnClickListener {
                currentList.removeAt(position)
            }
        }
    }

    class DiffUtilCallBack : DiffUtil.ItemCallback<InventoryItem>() {
        override fun areItemsTheSame(oldItem: InventoryItem, newItem: InventoryItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: InventoryItem, newItem: InventoryItem): Boolean {
            return oldItem == newItem
        }
    }
}