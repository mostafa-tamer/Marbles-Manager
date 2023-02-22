package com.example.barcodeReader.fragments.offlineModeFragment


import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.barcodeReader.Arabic
import com.example.barcodeReader.Language
import com.example.barcodeReader.database.InventoryItemOfflineMode
import com.example.barcodeReader.databinding.OfflineModeItemPropertiesViewHolderBinding
import com.example.barcodeReader.fragments.inventoryScanFragment.InventoryScanAdapter
import com.example.barcodeReader.fragments.mainMenuFragment.LanguageFactory
import com.example.barcodeReader.utils.CustomList

class OfflineModeAdapter(
    val itemsList: CustomList<InventoryItemOfflineMode>,
    private val isUpdatingDbBusy: MutableLiveData<Boolean>,
) : RecyclerView.Adapter<OfflineModeAdapter.ViewHolder>() {

    private val isRemovingItemBusy = MutableLiveData(false)

    override fun getItemCount() = itemsList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return create(parent)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindValues(itemsList[position])
        holder.bindKeys(LanguageFactory().getLanguage("en"))
        holder.viewsLogic(position, this, isUpdatingDbBusy, isRemovingItemBusy)
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

        private val numberTextWatcher =
            CustomWatcher("#ed1b24", "#ffffff", binding.numberSave, this)

        private fun reset() {

            binding.numberEdit.removeTextChangedListener(numberTextWatcher)

            updateButton("#ffffff", "#ed1b24", binding.numberSave, false)
        }
        private fun saveEditText(
            position: Int,
            inventoryScanAdapter: OfflineModeAdapter,
            updatingDbBusy: MutableLiveData<Boolean>
        ) {
            reset()
            binding.numberEdit.addTextChangedListener(numberTextWatcher)

            binding.numberSave.setOnClickListener {
                if (!updatingDbBusy.value!!) {
                    inventoryScanAdapter.itemsList.sizeLiveData.value =
                        inventoryScanAdapter.itemsList.size
                    if (binding.numberEdit.text.toString() == "") binding.numberEdit.setText("0")
                    inventoryScanAdapter.itemsList[position].number =
                        binding.numberEdit.text.toString()
                    updateButton("#399636", "#ffffff", binding.numberSave, false)
                }
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
            position: Int,
            inventoryScanAdapter: OfflineModeAdapter,
            isUpdatingDbBusy: MutableLiveData<Boolean>,
            isRemovingItemBusy: MutableLiveData<Boolean>
        ) {
            saveEditText(position,inventoryScanAdapter,isUpdatingDbBusy)
            removeItem(
                position,
                inventoryScanAdapter,
                isUpdatingDbBusy,
                isRemovingItemBusy
            )
        }

        private fun removeItem(
            position: Int,
            inventoryScanAdapter: OfflineModeAdapter,
            isUpdatingDbBusy: MutableLiveData<Boolean>,
            isRemovingItemBusy: MutableLiveData<Boolean>
        ) {
            var lock = false
            binding.removeButton.setOnClickListener {
                if (!lock && !isRemovingItemBusy.value!! && !isUpdatingDbBusy.value!!) {
                    lock = true
                    isRemovingItemBusy.value = true
                    inventoryScanAdapter.itemsList.removeAt(position)
                    inventoryScanAdapter.notifyItemRemoved(position)
                    inventoryScanAdapter.notifyItemRangeChanged(
                        position,
                        inventoryScanAdapter.itemsList.size
                    )
                    isRemovingItemBusy.value = false
                }
            }
        }
    }
}