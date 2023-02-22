package com.example.barcodeReader.fragments.inventoryScanFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.barcodeReader.database.InventoryItem
import com.example.barcodeReader.database.TopSoftwareDatabase
import com.example.barcodeReader.databinding.FragmentInventoryScanBinding
import com.example.barcodeReader.databinding.FragmentScanManualBarcodeViewHolderBinding
import com.example.barcodeReader.network.properties.get.marble.Table
import com.example.barcodeReader.userData
import com.example.barcodeReader.utils.*
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import java.text.SimpleDateFormat
import java.util.*


class InventoryScanFragment : Fragment() {
    private lateinit var binding: FragmentInventoryScanBinding
    private lateinit var viewModel: InventoryScanViewModel
    private lateinit var args: InventoryScanFragmentArgs

    private val itemsList = CustomList<InventoryItem>()
    private lateinit var adapter: InventoryScanAdapter

    private lateinit var manualAlertDialog: CustomAlertDialog
    private lateinit var saveListAlertDialog: CustomAlertDialog
    private lateinit var clearListAlertDialog: CustomAlertDialog
    private lateinit var marblesObserverAlertDialog: CustomAlertDialog
    private lateinit var internetConnectionAlertDialog: CustomAlertDialog
    private lateinit var exceptionErrorMessageAlertDialog: CustomAlertDialog

    private lateinit var listIsEmptyToast: Toast
    private lateinit var clearedToast: Toast
    private lateinit var itemNotFoundToast: Toast
    private lateinit var pleaseWriteBarcodeToast: Toast
    private lateinit var pleaseWaitTheInteractionToast: Toast

    lateinit var groupName: String
    lateinit var groupCode: String
    lateinit var pillName: String
    lateinit var pillCode: String
    lateinit var groupMgr: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentInventoryScanBinding.inflate(layoutInflater)
        args = InventoryScanFragmentArgs.fromBundle(requireArguments())

        setAttr()

        viewModelInitialization()

        alertDialogInitialization()
        toastInitialization()

        adapter = InventoryScanAdapter(itemsList)

        binding.container.adapter = adapter
        binding.groupName.text = "${groupName}"
        binding.pillName.text = "${pillName}"

        listeners()
        observers()

        return binding.root
    }

    private fun setAttr() {
        groupName = args.groupName
        groupCode = args.groupCode
        pillName = args.pillName
        pillCode = args.pillCode
        groupMgr = args.groupMgr
    }

    private fun toastInitialization() {
        listIsEmptyToast = createToast("List is empty!")
        clearedToast = createToast("Cleared!")
        itemNotFoundToast = createToast("Item not found!")
        pleaseWriteBarcodeToast = createToast("Please write the barcode!")
        pleaseWaitTheInteractionToast = createToast("Please wait The interaction!")
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
        viewModel.retData(groupCode, pillCode).observe(viewLifecycleOwner) {
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
        sendDataButtonListener()
        scanCameraBarcodeListener()
        scanManualBarcodeListener()
    }

    private fun observers() {
        marbleObserver()
        retDataObserver()
        barcodeObserver()
        listSizeObserver()
        alertDialogErrorMessageObserver(
            viewModel.alertDialogErrorMessageLiveData,
            viewLifecycleOwner,
            exceptionErrorMessageAlertDialog
        )
        isSendDataBusyObserver()
        sentDataResponseObserver()
        isRetMarbleDataBusyObserver()
    }

    private fun isRetMarbleDataBusyObserver() {
        viewModel.isRetMarbleDataBusy.observe(viewLifecycleOwner) {
            it?.let {
                busyViewModelController(it)
            }
        }
    }

    private fun isSendDataBusyObserver() {
        viewModel.isSendDataBusy.observe(viewLifecycleOwner) {
            it?.let {
                busyViewModelController(it)
            }
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

    private fun sendDataButtonListener() {
        binding.save.setOnClickListener {
            if (itemsList.isNotEmpty()) {
                saveListAlertDialog
                    .setMessage("Are you sure you want to send this items?")
                    .setTitle("Warning")
                    .setPositiveButton("Send data", false) {
                        if (!viewModel.isSendDataBusy.value!! && !viewModel.isRetMarbleDataBusy.value!!) {
                            if (itemsList.isNotEmpty()) {
                                viewModel.sendData(
                                    itemsList,
                                    userData.schema,
                                    pillCode,
                                    pillName,
                                    userData.employeeNumber,
                                    groupCode,
                                    groupMgr,
                                    getCurrentData()
                                )
                            } else {
                                listIsEmptyToast.show()
                            }
                            it.dismiss()
                        } else {
                            pleaseWaitTheInteractionToast.show()
                        }
                    }.setNegativeButton("Cancel")
                    .showDialog()
            } else {
                listIsEmptyToast.show()
            }
        }
    }

    private fun clearListListener() {
        binding.removeAll.setOnClickListener {
            if (itemsList.isNotEmpty()) {
                clearListAlertDialog
                    .setMessage("Are you sure you want to clear the list?")
                    .setTitle("Warning").setPositiveButton("Clear List", false) {
                        if (!viewModel.isSendDataBusy.value!! && !viewModel.isRetMarbleDataBusy.value!!) {
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
                        } else {
                            pleaseWaitTheInteractionToast.show()
                        }
                    }.setNegativeButton("Cancel")
                    .showDialog()
            } else {
                listIsEmptyToast.show()
            }
        }
    }

    private fun sentDataResponseObserver() {
        viewModel.sentDataResponse.work {
            it?.let {
                internetConnectionAlertDialog
                    .setTitle("Info")
                    .setMessage(it.message)
                    .setPositiveButton("OK") {
                        it.dismiss()
                    }.showDialog()

                if (it.statusCode == 200) {
                    itemsList.clear()
                    adapter.notifyItemRangeChanged(
                        0,
                        itemsList.size
                    )
                }
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
        viewModel.updateDB(itemsList, groupCode, pillCode)
    }

    private fun marbleObserver() {
        viewModel.marblesBody.work { it ->
            it?.let { marblesBody ->
                if (it.statusCode == 200) {
                    val group: Table? =
                        marblesBody.data.metaData.table.find { groupCode == it.brandCode }

                    if (group != null) {
                        val data = marblesBody.data

                        val inventoryItem = InventoryItem(
                            data.metaData.itemCode,
                            nameFilter(data.metaData),
                            data.metaData.blockNumber,
                            group.amount,
                            group.number,
                            data.metaData.frz,
                            uniteFilter(data.metaData),
                            data.metaData.unitCode,
                            data.metaData.zdimension,
                            data.metaData.xdimension,
                            data.metaData.ydimension,
                            groupCode,
                            pillCode,
                            userData.employeeNumber
                        )

                        itemsList.add(
                            inventoryItem
                        )

                        adapter.notifyItemInserted(itemsList.size - 1)
                        binding.container.layoutManager?.scrollToPosition(itemsList.size - 1)
                    } else {
                        itemNotFoundToast.show()
                    }
                } else {
                    marblesObserverAlertDialog
                        .setTitle("Error")
                        .setMessage(it.message)
                        .setPositiveButton(
                            "OK"
                        ) {
                            it.dismiss()
                        }.showDialog()
                }
            }
        }
    }

    private fun getCurrentData(): String {
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val calendar = Calendar.getInstance()
        return dateTimeFormat.format(calendar.time)
    }

    private fun viewModelInitialization() {
        val inventoryItemDao = TopSoftwareDatabase.getInstance(requireContext()).inventoryItemDao
        val viewModelFactory =
            InventoryScanViewModel.InventoryScanViewModelFactory(inventoryItemDao)
        viewModel = ViewModelProvider(this, viewModelFactory)[InventoryScanViewModel::class.java]
    }

    private fun retApiData(barcode: String) {

        viewModel.retMarbleData(
            userData.schema, barcode, userData.loginCount, userData.employeeNumber
        )
    }

    private fun barcodeObserver() {
        viewModel.barcode.work {
            it?.let {
                val updatedBarcode = makeupBarcode(it)
                retApiData(updatedBarcode)
            }
        }
    }

    private fun scanManualBarcodeListener() {
        binding.scanButtonManual.setOnClickListener {

            val manualBarcodeViewHolderBinding =
                FragmentScanManualBarcodeViewHolderBinding.inflate(layoutInflater)
            manualAlertDialog
                .setBody(manualBarcodeViewHolderBinding.root)
                .setTitle("Barcode").setNegativeButton("Cancel")
                .setPositiveButton("Ok", false) {
                    if (!viewModel.isSendDataBusy.value!! && !viewModel.isRetMarbleDataBusy.value!!) {
                        if (manualBarcodeViewHolderBinding.barcode.text.toString().isNotEmpty()) {
                            it.dismiss()
                            viewModel.barcode.setValue(manualBarcodeViewHolderBinding.barcode.text.toString())
                        } else {
                            pleaseWriteBarcodeToast.show()
                        }
                    } else {
                        pleaseWaitTheInteractionToast.show()
                    }
                }.setNegativeButton("Cancel")
                .showDialog()
        }
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
            viewModel.barcode.setValue(result.contents)
        }
    }

    override fun onStop() {
        manualAlertDialog.dismiss()
        super.onStop()
    }
}