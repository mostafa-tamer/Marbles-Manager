package com.example.barcodeReader.fragments.offlineModeFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.barcodeReader.database.InventoryItemOfflineMode
import com.example.barcodeReader.database.TopSoftwareDatabase
import com.example.barcodeReader.databinding.FragmentOfflineModeBinding
import com.example.barcodeReader.databinding.OfflineModeFillItemViewHolderBinding
import com.example.barcodeReader.utils.*
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class OfflineModeFragment : Fragment() {

    private lateinit var binding: FragmentOfflineModeBinding
    private lateinit var viewModel: OfflineModeViewModel

    private val itemsList = CustomList<InventoryItemOfflineMode>()
    private lateinit var adapter: OfflineModeAdapter

    private lateinit var manualAlertDialog: CustomAlertDialog
    private lateinit var saveListAlertDialog: CustomAlertDialog
    private lateinit var clearListAlertDialog: CustomAlertDialog
    private lateinit var marblesObserverAlertDialog: CustomAlertDialog
    private lateinit var internetConnectionAlertDialog: CustomAlertDialog
    private lateinit var exceptionErrorMessageAlertDialog: CustomAlertDialog

    private lateinit var clearedToast: Toast
    private lateinit var listIsEmptyToast: Toast
    private lateinit var fillAllFieldsToast: Toast

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentOfflineModeBinding.inflate(layoutInflater)

        adapter = OfflineModeAdapter(itemsList)
        binding.container.adapter = adapter

        viewModelInitialization()

        alertDialogInitialization()
        toastInitialization()

        listeners()
        observers()

        return binding.root
    }

    private fun toastInitialization() {
        listIsEmptyToast = createToast("List is empty!")
        clearedToast = createToast("Cleared!")
        fillAllFieldsToast = createToast("Please fill all fields!")
    }

    private fun createToast(message: String): Toast {
        return Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        )
    }

    private fun alertDialogInitialization() {
        manualAlertDialog = CustomAlertDialog(requireContext())
        saveListAlertDialog = CustomAlertDialog(requireContext())
        clearListAlertDialog = CustomAlertDialog(requireContext())
        marblesObserverAlertDialog = CustomAlertDialog(requireContext())
        internetConnectionAlertDialog = CustomAlertDialog(requireContext())
        exceptionErrorMessageAlertDialog = CustomAlertDialog(requireContext())
    }

    private fun retDataObserver() {
        var once = true
        viewModel.retDataDB().observe(viewLifecycleOwner) {
            it.let {
                if (once) {
                    once = false
                    it?.let {
                        itemsList.addAll(it)
                        adapter.notifyItemRangeChanged(
                            0,
                            itemsList.size
                        )
                    }
                }
            }
        }
    }

    private fun listeners() {
        clearListListener()
        scanCameraBarcodeListener()
        scanManualBarcodeListener()
    }

    private fun observers() {
        retDataObserver()
        listSizeObserver()
        alertDialogErrorMessageObserver(
            viewModel.alertDialogErrorMessageLiveData,
            viewLifecycleOwner,
            exceptionErrorMessageAlertDialog
        )
    }


    private fun clearListListener() {
        binding.removeAll.setOnClickListener {
            if (itemsList.isNotEmpty()) {
                clearListAlertDialog
                    .setMessage("Are you sure you want to clear the list?")
                    .setTitle("Warning").setPositiveButton("Clear List", false) {
                        if (itemsList.isNotEmpty()) {
                            itemsList.clear()
                            adapter.notifyItemRangeChanged(
                                0,
                                itemsList.size
                            )
                            clearedToast.show()
                        } else {
                            listIsEmptyToast.show()
                        }
                        it.dismiss()
                    }.setNegativeButton("Cancel")
                    .showDialog()
            } else {
                listIsEmptyToast.show()
            }
        }
    }


    private fun listSizeObserver() {
        itemsList.sizeLiveData.observe(viewLifecycleOwner) {
            it?.let {
                updateDB()
                if (it > 0) binding.dummyText.text = ""
                else binding.dummyText.text = "List Is Empty."
                binding.textItemCount.text = "Scanned Items: ${it}"
            }
        }
    }

    private fun updateDB() {
        lifecycleScope.launch {
            delay(350)
            viewModel.deleteDataDB()
            viewModel.saveDataDB(itemsList)
        }
    }

    private fun viewModelInitialization() {
        val inventoryItemOfflineModeDao =
            TopSoftwareDatabase.getInstance(requireContext()).inventoryItemOfflineModeDao
        val viewModelFactory =
            OfflineModeViewModel.OfflineModeViewModelFactory(inventoryItemOfflineModeDao)
        viewModel = ViewModelProvider(this, viewModelFactory)[OfflineModeViewModel::class.java]
    }


    private fun scanManualBarcodeListener() {
        binding.scanButtonManual.setOnClickListener {
            fillItemData()
        }
    }

    private fun fillItemData(showBarcode: Boolean = true, cameraBarcode: String = "") {
        val offlineModeItemPropertiesViewHolderBinding =
            OfflineModeFillItemViewHolderBinding.inflate(layoutInflater)

        if (!showBarcode) {
            offlineModeItemPropertiesViewHolderBinding.barcodeContainer.visibility = View.GONE
            offlineModeItemPropertiesViewHolderBinding.barcode.setText(cameraBarcode)
        }

        manualAlertDialog
            .setBody(offlineModeItemPropertiesViewHolderBinding.root)
            .setTitle("Barcode").setNegativeButton("Cancel")
            .setPositiveButton("Ok", false) {

                if (
                    offlineModeItemPropertiesViewHolderBinding.barcode.text.toString()
                        .isNotEmpty() &&
                    offlineModeItemPropertiesViewHolderBinding.amount.text.toString()
                        .isNotEmpty() &&
                    offlineModeItemPropertiesViewHolderBinding.number.text.toString()
                        .isNotEmpty()
                ) {
                    it.dismiss()
                    val barcode =
                        makeupBarcode(offlineModeItemPropertiesViewHolderBinding.barcode.text.toString())
                    val amount =
                        offlineModeItemPropertiesViewHolderBinding.amount.text.toString()
                    val number =
                        offlineModeItemPropertiesViewHolderBinding.number.text.toString()
                    addItemsToList(barcode, amount, number)
                } else {
                    fillAllFieldsToast.show()
                }
            }.setNegativeButton("Cancel")
            .showDialog()
    }

    private fun addItemsToList(barcode: String, amount: String, number: String) {

        itemsList.add(
            InventoryItemOfflineMode(
                barcode,
                amount,
                number
            )
        )

        adapter.notifyItemInserted(itemsList.size - 1)
        binding.container.layoutManager?.scrollToPosition(itemsList.size - 1)
    }

    private fun scanCameraBarcodeListener() {
        binding.scanButtonCamera.setOnClickListener {
            val scanOptions = ScanOptions().setPrompt("Volume up to flash on").setBeepEnabled(true)
                .setOrientationLocked(true).setCaptureActivity(CaptureAct::class.java)
            barLauncher.launch(scanOptions)
        }
    }

    private var barLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents != null) {
            fillItemData(false, result.contents)
        }
    }

    override fun onStop() {
        manualAlertDialog.dismiss()
        super.onStop()
    }
}