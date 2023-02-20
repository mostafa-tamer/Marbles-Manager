package com.example.barcodeReader.fragments.inventoryScanFragment

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
import com.example.barcodeReader.databaes.InventoryItem
import com.example.barcodeReader.databinding.ItemPropertiesViewHolderBinding
import com.example.barcodeReader.fragments.mainMenuFragment.LanguageFactory
import com.example.barcodeReader.userData
import com.example.barcodeReader.utils.CustomList

class InventoryScanAdapter(
    val itemsList: CustomList<InventoryItem>
) : RecyclerView.Adapter<InventoryScanAdapter.ViewHolder>() {

    override fun getItemCount() = itemsList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return create(parent)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindValues(itemsList[position])
        holder.bindKeys(LanguageFactory().getLanguage(userData.loginLanguage))
        holder.viewsLogic(position, this)
    }

    fun create(parent: ViewGroup): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPropertiesViewHolderBinding.inflate(
            layoutInflater, parent, false
        )
        return ViewHolder(binding)
    }

    class ViewHolder(val binding: ItemPropertiesViewHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private class CustomWatcher(
            private val background: String,
            private val textColor: String,
            private val view: Button,
            private val holder: ViewHolder
        ) : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                holder.updateButton(background, textColor, view, true)
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        }

        private val amountTextWatcher =
            CustomWatcher("#ed1b24", "#ffffff", binding.amountSave, this)

        private val numberTextWatcher =
            CustomWatcher("#ed1b24", "#ffffff", binding.numberSave, this)

        private fun reset() {

            binding.amountEdit.removeTextChangedListener(amountTextWatcher)
            binding.numberEdit.removeTextChangedListener(numberTextWatcher)

            updateButton("#ffffff", "#ed1b24", binding.amountSave, false)
            updateButton("#ffffff", "#ed1b24", binding.numberSave, false)
        }

        fun bindKeys(
            property: Language
        ) {
            binding.language = property
            if (property is Arabic) binding.root.layoutDirection = View.LAYOUT_DIRECTION_RTL
            binding.executePendingBindings()
        }

        fun bindValues(
            property: InventoryItem
        ) {
            binding.inventoryItem = property
            binding.executePendingBindings()
        }

        fun viewsLogic(
            position: Int, inventoryScanAdapter: InventoryScanAdapter
        ) {
            removeItem(position, inventoryScanAdapter)
            saveEditText(position, inventoryScanAdapter)
        }

        private fun saveEditText(position: Int, inventoryScanAdapter: InventoryScanAdapter) {
            reset()

            binding.amountEdit.addTextChangedListener(amountTextWatcher)
            binding.numberEdit.addTextChangedListener(numberTextWatcher)

            binding.amountSave.setOnClickListener {
                inventoryScanAdapter.itemsList.sizeLiveData.value =
                    inventoryScanAdapter.itemsList.size
                if (binding.amountEdit.text.toString() == "") binding.amountEdit.setText("0")
                inventoryScanAdapter.itemsList[position].amount = binding.amountEdit.text.toString()
                updateButton("#399636", "#ffffff", binding.amountSave, false)
            }

            binding.numberSave.setOnClickListener {
                inventoryScanAdapter.itemsList.sizeLiveData.value =
                    inventoryScanAdapter.itemsList.size
                if (binding.numberEdit.text.toString() == "") binding.numberEdit.setText("0")
                inventoryScanAdapter.itemsList[position].number = binding.numberEdit.text.toString()
                updateButton("#399636", "#ffffff", binding.numberSave, false)
            }
        }

        private fun updateButton(
            background: String,
            textColor: String,
            view: Button,
            status: Boolean
        ) {
            view.setBackgroundColor(Color.parseColor(background))
            view.setTextColor(Color.parseColor(textColor))
            view.isEnabled = status
        }

        private fun removeItem(
            position: Int, inventoryScanAdapter: InventoryScanAdapter
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