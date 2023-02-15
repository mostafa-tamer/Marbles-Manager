package com.example.barcodereader.fragments.inventoryScanFragment

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.barcodereader.Arabic
import com.example.barcodereader.Language
import com.example.barcodereader.databaes.InventoryItem
import com.example.barcodereader.databinding.ItemPropertiesViewHolderBinding
import com.example.barcodereader.fragments.scanFragment.LanguageFactory
import com.example.barcodereader.userData


class InventoryScanAdapter(
    val itemsList: MutableList<InventoryItem>
) : ListAdapter<InventoryItem, InventoryScanAdapter.ViewHolder>(DiffUtilCallBack()) {

    init {
        submitList(itemsList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindValues(getItem(position))
        holder.bindKeys(LanguageFactory().getLanguage(userData.loginLanguage))
        holder.viewsLogic(position, this)
    }

    class ViewHolder(val binding: ItemPropertiesViewHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        lateinit var inventoryItem: InventoryItem
        lateinit var language: Language
        fun bindKeys(
            property: Language
        ) {
            binding.language = property
            language = property
            if (language is Arabic) binding.root.layoutDirection = View.LAYOUT_DIRECTION_RTL
            binding.executePendingBindings()
        }

        fun bindValues(
            property: InventoryItem
        ) {
            binding.inventoryItem = property
            inventoryItem = property
            binding.executePendingBindings()
        }

        fun viewsLogic(
            position: Int, inventoryScanAdapter: InventoryScanAdapter
        ) {
            removeItem(position, inventoryScanAdapter)
            saveEditText(position, inventoryScanAdapter)
        }

        private fun saveEditText(position: Int, inventoryScanAdapter: InventoryScanAdapter) {


            binding.amountEdit.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {
                    updateButton("#ed1b24", binding.amountSave, true)
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int, before: Int, count: Int
                ) {

                }
            })

            binding.numberEdit.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {

                    updateButton("#ed1b24", binding.numberSave, true)
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int
                ) {

                }

                override fun onTextChanged(
                    s: CharSequence, start: Int, before: Int, count: Int
                ) {

                }
            })

            binding.amountSave.setOnClickListener {
                if (binding.amountEdit.text.toString() == "") binding.amountEdit.setText("0")
                updateButton("#399636", binding.amountSave, false)
                inventoryScanAdapter.itemsList[position].amount = binding.amountEdit.text.toString()
            }

            binding.numberSave.setOnClickListener {
                if (binding.numberEdit.text.toString() == "") binding.numberEdit.setText("0")
                inventoryScanAdapter.itemsList[position].number = binding.numberEdit.text.toString()
                updateButton("#399636", binding.numberSave, false)
            }
        }

        private fun updateButton(background: String, view: Button, status: Boolean) {
            view.setBackgroundColor(Color.parseColor(background))
            view.setTextColor(Color.parseColor("#ffffff"))
            view.isEnabled = status
        }

        private fun removeItem(
            position: Int, inventoryScanAdapter: InventoryScanAdapter
        ) {
            binding.removeButton.setOnClickListener {
                inventoryScanAdapter.itemsList.removeAt(position)
                inventoryScanAdapter.notifyDataSetChanged()
            }
        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemPropertiesViewHolderBinding.inflate(
                    layoutInflater, parent, false
                )
                return ViewHolder(binding)
            }
        }
    }

    class DiffUtilCallBack : DiffUtil.ItemCallback<InventoryItem>() {
        override fun areItemsTheSame(oldItem: InventoryItem, newItem: InventoryItem): Boolean {
            return oldItem.itemCode == newItem.itemCode
        }

        override fun areContentsTheSame(oldItem: InventoryItem, newItem: InventoryItem): Boolean {
            return oldItem == newItem
        }
    }
}