package com.example.barcodeReader.fragments.resultFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.barcodeReader.databinding.FragmentResultRecyclerViewViewHolderBinding
import com.example.barcodeReader.network.properties.get.marble.Table


class RecyclerViewAdapter :
    ListAdapter<Table, RecyclerViewAdapter.ViewHolder>(DiffUtilCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: FragmentResultRecyclerViewViewHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        lateinit var table: Table

        fun bind(property: Table) {
            binding.table = property
            table = property
            binding.executePendingBindings()
        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentResultRecyclerViewViewHolderBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return ViewHolder(binding)
            }
        }
    }

    class DiffUtilCallBack : DiffUtil.ItemCallback<Table>() {
        override fun areItemsTheSame(oldItem: Table, newItem: Table): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Table, newItem: Table): Boolean {
            return oldItem == newItem
        }
    }
}



