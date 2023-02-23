package com.example.barcodeReader.fragments.offlineModeFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.barcodeReader.database.InventoryItemOfflineMode
import com.example.barcodeReader.database.TopSoftwareDatabase
import com.example.barcodeReader.databinding.FragmentOfflineModeBinding
import com.example.barcodeReader.databinding.OfflineModeFillItemViewHolderBinding
import com.example.barcodeReader.utils.*
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.runBlocking


class OfflineModeFragment : Fragment() {

    private lateinit var binding: FragmentOfflineModeBinding
    private lateinit var viewModel: OfflineModeViewModel

    private val itemsList = CustomList<InventoryItemOfflineMode>()
    private lateinit var adapter: OfflineModeAdapter

    private lateinit var manualAlertDialog: CustomAlertDialog
    private lateinit var clearListAlertDialog: CustomAlertDialog
    private lateinit var exceptionErrorMessageAlertDialog: CustomAlertDialog

    private lateinit var clearedToast: Toast
    private lateinit var listIsEmptyToast: Toast

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModelInitialization()
        alertDialogInitialization()
        toastInitialization()
        fillItemsListFromDb()

        binding = FragmentOfflineModeBinding.inflate(layoutInflater)

        adapter = OfflineModeAdapter(itemsList, viewModel.isUpdatingDbBusy)
        binding.container.adapter = adapter




        listeners()
        observers()

        return binding.root
    }

    private fun toastInitialization() {
        listIsEmptyToast = createToast("List is empty!")
        clearedToast = createToast("Cleared!")
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
        clearListAlertDialog = CustomAlertDialog(requireContext())
        exceptionErrorMessageAlertDialog = CustomAlertDialog(requireContext())
    }

    private fun fillItemsListFromDb() {
        runBlocking {
            itemsList.addAll(viewModel.retDataDB())
        }
    }

    private fun listeners() {
        clearListListener()
        scanCameraBarcodeListener()
        scanManualBarcodeListener()
        saveExcelSheet()
    }

    private fun saveExcelSheet() {
        binding.save.setOnClickListener {
            viewModel.exportExcelSheet(itemsList, requireContext())
        }
    }

    private fun observers() {

        listSizeObserver()
        isSavingDataBusyObserver()
        isUpdatingDbBusyObserver()
        alertDialogErrorMessageObserver(
            viewModel.alertDialogErrorMessageLiveData,
            viewLifecycleOwner,
            exceptionErrorMessageAlertDialog
        )
    }

    private fun isSavingDataBusyObserver() {
        viewModel.isSavingDataBusy.observe(viewLifecycleOwner) {
            busyViewModelController(it)
        }
    }

    private fun isUpdatingDbBusyObserver() {
        viewModel.isUpdatingDbBusy.observe(viewLifecycleOwner) {
            busyViewModelController(it)
        }
    }

    private fun busyViewModelController(it: Boolean) {
        if (!it) {
            binding.progressBar.visibility = View.INVISIBLE
            unlockButtons()
        } else {
            binding.progressBar.visibility = View.VISIBLE
            lockButtons()
        }
    }

    private fun lockButtons() {
        binding.scanButtonManual.lockButton()
        binding.scanButtonCamera.lockButton()
        binding.removeAll.lockButton()
        binding.save.lockButton()
    }

    private fun unlockButtons() {
        binding.scanButtonManual.unlockButton()
        binding.scanButtonCamera.unlockButton()
        binding.removeAll.unlockButton()
        binding.save.unlockButton()
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
            viewModel.updateDB(itemsList)
            if (it > 0) binding.dummyText.text = ""
            else binding.dummyText.text = "List Is Empty."
            binding.textItemCount.text = "Scanned Items: ${it}"
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
            .setPositiveButton("Ok") {
                var barcode =
                    makeupBarcode(offlineModeItemPropertiesViewHolderBinding.barcode.text.toString())

                var number =
                    offlineModeItemPropertiesViewHolderBinding.number.text.toString()

                if (barcode.isEmpty())
                    barcode = "0"

                if (number.isEmpty())
                    number = "0"

                addItemsToList(barcode, number)

            }.setNegativeButton("Cancel")
            .showDialog()
    }

    private fun addItemsToList(barcode: String, number: String) {

        itemsList.add(
            InventoryItemOfflineMode(
                barcode,
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