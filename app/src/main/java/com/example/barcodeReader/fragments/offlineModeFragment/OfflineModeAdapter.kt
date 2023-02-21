package com.example.barcodeReader.fragments.offlineModeFragment


import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.barcodeReader.Arabic
import com.example.barcodeReader.Language
import com.example.barcodeReader.database.InventoryItemOfflineMode
import com.example.barcodeReader.databinding.OfflineModeItemPropertiesViewHolderBinding
import com.example.barcodeReader.fragments.mainMenuFragment.LanguageFactory
import com.example.barcodeReader.userData
import com.example.barcodeReader.utils.CustomList

class OfflineModeAdapter(
    val itemsList: CustomList<InventoryItemOfflineMode>
) : RecyclerView.Adapter<OfflineModeAdapter.ViewHolder>() {

    override fun getItemCount() = itemsList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindValues(itemsList[position])
        holder.bindKeys(LanguageFactory().getLanguage("en"))
        holder.viewsLogic(position, this)
    }

    fun create(parent: ViewGroup): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = OfflineModeItemPropertiesViewHolderBinding.inflate(
            layoutInflater, parent, false
        )
        return ViewHolder(binding)
    }

    class ViewHolder(val binding: OfflineModeItemPropertiesViewHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindKeys(
            property: Language
        ) {
            binding.language = property
            if (property is Arabic) binding.root.layoutDirection = View.LAYOUT_DIRECTION_RTL
            binding.executePendingBindings()
        }

        fun bindValues(
            property: InventoryItemOfflineMode
        ) {
            binding.inventoryItemOfflineMode = property
            binding.executePendingBindings()
        }

        fun viewsLogic(
            position: Int, inventoryScanAdapter: OfflineModeAdapter
        ) {
            removeItem(position, inventoryScanAdapter)
        }

        private fun removeItem(
            position: Int, inventoryScanAdapter: OfflineModeAdapter
        ) {
            var lock = true
            binding.removeButton.setOnClickListener {
                if (lock) {
                    lock = false
                    inventoryScanAdapter.itemsList.removeAt(position)
                    inventoryScanAdapter.notifyItemRemoved(position)
                    inventoryScanAdapter.notifyItemRangeChanged(
                        position,
                        inventoryScanAdapter.itemsList.size
                    )
                }
            }
        }
    }
}