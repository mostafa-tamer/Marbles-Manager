package com.example.barcodereader.fragments.inventoryScanFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.barcodereader.databaes.InventoryItem
import com.example.barcodereader.databaes.TopSoftwareDatabase
import com.example.barcodereader.databinding.FragmentInventoryScanBinding
import com.example.barcodereader.databinding.FragmentScanManualBarcodeViewHolderBinding
import com.example.barcodereader.network.properties.get.marble.Data
import com.example.barcodereader.network.properties.get.marble.Table
import com.example.barcodereader.userData
import com.example.barcodereader.utils.CaptureAct
import com.example.barcodereader.utils.CustomAlertDialog
import com.example.barcodereader.utils.CustomList
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    private lateinit var listIsEmptyToast: Toast
    private lateinit var clearedToast: Toast
    private lateinit var itemNotFoundToast: Toast
    private lateinit var pleaseWriteBarcodeToast: Toast
    private lateinit var pleaseWaitTheInteractionToast: Toast

    private val visibleSpinner = MutableLiveData(4)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        args = InventoryScanFragmentArgs.fromBundle(requireArguments())
        binding = FragmentInventoryScanBinding.inflate(layoutInflater)
        viewModelInitialization()


        alertDialogInitialization()
        toastInitialization()

        adapter = InventoryScanAdapter(itemsList)

        binding.container.adapter = adapter
        binding.groupName.text = "${args.groupName}"
        binding.pillName.text = "${args.pillName}"

        listeners()
        observers()

        return binding.root
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
    }


    private fun retDataObserver() {
        var once = true
        viewModel.retData(args.groupCode, args.pillCode).observe(viewLifecycleOwner) {
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
        scanCameraBarcodeListener()
        scanManualBarcodeListener()
        clearListListener()
        sendDataButtonListener()
    }

    private fun observers() {
        retDataObserver()
        barcodeObserver()
        connectionStatusObserver()
        sentDataResponseObserver()
        listSizeObserver()
        spinnerVisibilityObserver()
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

    private fun spinnerVisibilityObserver() {
        visibleSpinner.observe(viewLifecycleOwner) {
            if (it == 0) {
                lockButtons()
            } else {
                unlockButtons()
            }
        }
    }

    private fun spinnerVisible() {
        binding.progressBar.visibility = View.VISIBLE
        visibleSpinner.value = 0
    }

    private fun spinnerInvisible() {
        binding.progressBar.visibility = View.INVISIBLE
        visibleSpinner.value = 4
    }

    private fun sendDataButtonListener() {
        binding.save.setOnClickListener {
            if (itemsList.isNotEmpty()) {
                saveListAlertDialog
                    .setMessage("Are you sure you want to send this items?")
                    .setTitle("Warning").setPositiveButton("Send data") {
                        if (visibleSpinner.value!! == 4) {
                            if (itemsList.isNotEmpty()) {
                                spinnerVisible()
                                viewModel.sendData(
                                    itemsList,
                                    userData.schema,
                                    args.pillCode,
                                    args.pillName,
                                    userData.employeeNumber,
                                    args.groupCode,
                                    args.groupMgr,
                                    getCurrentData()
                                )
                            } else {
                                listIsEmptyToast.show()
                            }
                        } else {
                            pleaseWaitTheInteractionToast.show()
                        }
                        it.dismiss()
                    }.setNegativeButton("Cancel")
                    {
                        it.dismiss()
                    }.showDialog()
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
                    .setTitle("Warning").setPositiveButton("Clear List") {
                        if (visibleSpinner.value!! == 4) {
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
                        } else {
                            pleaseWaitTheInteractionToast.show()
                        }
                        it.dismiss()
                    }.setNegativeButton("Cancel") {
                        it.dismiss()
                    }.showDialog()
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
                    .setMessage(it.body()!!.message)
                    .setPositiveButton("OK") {
                        it.dismiss()
                    }.showDialog()

                if (it.code() == 200)
                    itemsList.clear()
                adapter.notifyItemRangeChanged(
                    0,
                    itemsList.size
                )
                spinnerInvisible()
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
            delay(50)
            viewModel.deleteDataDB(args.groupCode, args.pillCode)
            viewModel.saveDataDB(itemsList)
        }
    }

    private fun nameFilter(data: Data): String {
        return when (userData.loginLanguage) {
            "ar" -> {
                data.itemName
            }
            "en" -> {
                data.itemNameLanguages.En
            }
            "de" -> {
                data.itemNameLanguages.De
            }
            "es" -> {
                data.itemNameLanguages.Es
            }
            "fr" -> {
                data.itemNameLanguages.Fr
            }
            "it" -> {
                data.itemNameLanguages.It
            }
            "ru" -> {
                data.itemNameLanguages.Ru
            }
            else -> {
                data.itemNameLanguages.Tr
            }
        }
    }

    private fun uniteFilter(data: Data): String {
        return when (userData.loginLanguage) {
            "ar" -> {
                data.unit
            }
            "en" -> {
                data.unitLanguages.En
            }
            "de" -> {
                data.unitLanguages.De
            }
            "es" -> {
                data.unitLanguages.Es
            }
            "fr" -> {
                data.unitLanguages.Fr
            }
            "it" -> {
                data.unitLanguages.It
            }
            "ru" -> {
                data.unitLanguages.Ru
            }
            else -> {
                data.unitLanguages.Tr
            }
        }
    }

    private fun marbleObserver() {
        viewModel.marbles.work { it ->
            it?.let {
                spinnerInvisible()
                if (it.code() == 200) {
                    val body = it.body()!!
                    val table: Table? = body.data.table.find { args.groupCode == it.brandCode }

                    if (table != null) {
                        val data = body.data

                        val inventoryItem = InventoryItem(
                            data.itemCode,
                            nameFilter(data),
                            data.blockNumber,
                            table.amount,
                            table.number,
                            data.frz,
                            uniteFilter(data),
                            data.unitCode,
                            data.zdimension,
                            data.xdimension,
                            data.ydimension,
                            args.groupCode,
                            args.pillCode,
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
                        .setMessage(it.body()!!.message)
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
        val userDao = TopSoftwareDatabase.getInstance(requireContext()).userDao
        val inventoryItemDao = TopSoftwareDatabase.getInstance(requireContext()).inventoryItemDao
        val viewModelFactory =
            InventoryScanViewModel.InventoryScanViewModelFactory(userDao, inventoryItemDao)
        viewModel = ViewModelProvider(this, viewModelFactory)[InventoryScanViewModel::class.java]
    }

    private fun retApiData(barcode: String) {
        spinnerVisible()
        viewModel.retRetrofitData(
            userData.schema, barcode, userData.loginCount, userData.employeeNumber
        )
    }

    private fun barcodeObserver() {
        viewModel.barcode.work {
            it?.let {
                val updatedBarcode = makeupBarcode(it)
                marbleObserver()
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
                .setPositiveButton("Ok") {
                    if (visibleSpinner.value!! == 4) {
                        if (manualBarcodeViewHolderBinding.barcode.text.toString().isNotEmpty()) {
                            it.dismiss()
                            viewModel.barcode.setValue(manualBarcodeViewHolderBinding.barcode.text.toString())
                        } else {
                            pleaseWriteBarcodeToast.show()
                        }
                    } else {
                        pleaseWaitTheInteractionToast.show()
                    }
                }.setNegativeButton("Cancel") {
                    it.dismiss()
                }

            manualAlertDialog.showDialog()
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

    private fun makeupBarcode(barcode: String): String {
        return if (barcode[0] == '0') {
            var counter = 0
            for (i in 0..barcode.length) {
                if (barcode[i] == '0') {
                    counter++
                } else {
                    break
                }
            }
            barcode.substring(counter, barcode.length - 1)
        } else {
            barcode
        }
    }

    private fun connectionStatusObserver() {
        viewModel.connectionStatus.work {
            it?.let {
                if (!it) {
                    spinnerInvisible()
                    internetConnectionAlertDialog
                        .setMessage("Please check the token or the internet connection")
                        .setTitle("Error").setPositiveButton("OK") {
                            it.dismiss()
                        }.showDialog()
                }
            }
        }
    }

    override fun onStop() {
        manualAlertDialog.dismiss()
        super.onStop()
    }
}