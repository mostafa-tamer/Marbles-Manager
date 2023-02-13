package com.example.barcodereader.fragments.inventoryScanFragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.barcodereader.databaes.InventoryItem
import com.example.barcodereader.databaes.TopSoftwareDatabase
import com.example.barcodereader.databinding.FragmentInventoryScanBinding
import com.example.barcodereader.databinding.FragmentScanManualBarcodeViewHolderBinding
import com.example.barcodereader.databinding.ItemPropertiesViewHolderBinding
import com.example.barcodereader.fragments.scanFragment.LanguageFactory
import com.example.barcodereader.network.properties.get.marble.Data
import com.example.barcodereader.network.properties.get.marble.Table
import com.example.barcodereader.userData
import com.example.barcodereader.utils.CaptureAct
import com.example.barcodereader.utils.CustomToast
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class InventoryScanFragment : Fragment() {
    private lateinit var binding: FragmentInventoryScanBinding
    private lateinit var viewModel: InventoryScanViewModel
    private lateinit var args: InventoryScanFragmentArgs

    private val adapter = InventoryScanAdapter()
    private val itemsList = mutableListOf<InventoryItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        args = InventoryScanFragmentArgs.fromBundle(requireArguments())
        binding = FragmentInventoryScanBinding.inflate(layoutInflater)
        viewModelInitialization()

        binding.container.adapter = adapter

        adapter.submitList(itemsList)

        binding.groupName.text = "${args.groupName}"

        listeners()
        observers()
        return binding.root
    }

    private fun listeners() {
        scanCameraBarcodeListener()
        scanManualBarcodeListener()
    }

    private fun observers() {
        barcodeObserver()
        connectionStatusObserver()
    }

    private fun updateListSize() {
        if (itemsList.size > 0) binding.dummyText.text = ""
        else binding.dummyText.text = "List Is Empty."
        binding.textItemCount.text =
            "Scanned Items: ${itemsList.size}"
    }

    private fun marbleObserver() {
        viewModel.marble.work { it ->
            it?.let {
                if (it.code() == 200) {
                    val body = it.body()!!

                    val table: Table? = body.data.table.find { args.groupCode == it.brandCode }
                    if (table != null) {

                        val data = body.data

                        val inventoryItem = InventoryItem(
                            data.itemCode,
                            data.itemName,
                            data.blockNumber,
                            table.amount,
                            table.number,
                            data.frz,
                            data.unit,
                            data.zdimension,
                            data.xdimension,
                            data.ydimension
                        )

                        itemsList.add(
                            inventoryItem
                        )



                        binding.container.layoutManager?.scrollToPosition(itemsList.size - 1)

                        updateListSize()

//                        showData(data, table)

//                        itemPropertiesViewHolderBinding.removeButton.setOnClickListener {
//                            binding.container.removeView(itemPropertiesViewHolderBinding)
//                            updateListSize()
//                            CustomToast.show(requireContext(), "Item Removed!")
//                        }
                    } else {
                        CustomToast.show(requireContext(), "Item Not Found!")
                    }
                } else {
                    AlertDialog.Builder(requireContext())
                        .setMessage(it.body()?.message).setPositiveButton(
                            "OK"
                        ) { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }.show()
                }
            }
        }
    }

    private fun viewModelInitialization() {
        val userDao = TopSoftwareDatabase.getInstance(requireContext()).userDao
        val viewModelFactory = InventoryScanViewModel.InventoryScanViewModelFactory(userDao)
        viewModel = ViewModelProvider(this, viewModelFactory)[InventoryScanViewModel::class.java]
    }

    private fun showData(
        data: Data,
        table: Table
    ): View {

        val languageFactory = LanguageFactory()
        val language = languageFactory.getLanguage(userData.loginLanguage)

        val binding =
            ItemPropertiesViewHolderBinding.inflate(layoutInflater)


        val amount = table.amount
        val number = table.number

        binding.frz.text = "${language.frz}: "
        binding.frzEdit.setText(data.frz)

        binding.amount.text = "${language.amount}: "
        binding.amountEdit.setText(amount)

        binding.number.text = "${language.number}: "
        binding.numberEdit.setText(number)

        binding.blockNumber.text = "${language.blockNumber}: ${data.blockNumber}"
        binding.height.text = "${language.height}: ${data.zdimension}"
        binding.length.text = "${language.length}: ${data.xdimension}"
        binding.width.text = "${language.width}: ${data.ydimension}"
        binding.itemCode.text = "${language.itemCode}: ${data.itemCode}"

        when (userData.loginLanguage) {
            "ar" -> {
                binding.unit.text = language.unit + ": " + data.unit
                binding.itemName.text = language.itemName + ": " + data.itemName
                binding.root.layoutDirection = View.LAYOUT_DIRECTION_RTL
            }
            "en" -> {
                binding.unit.text = language.unit + ": " + data.unitLanguages.En
                binding.itemName.text = language.itemName + ": " + data.itemNameLanguages.En
            }
            "de" -> {
                binding.unit.text = language.unit + ": " + data.unitLanguages.De
                binding.itemName.text = language.itemName + ": " + data.itemNameLanguages.De
            }
            "es" -> {
                binding.unit.text = language.unit + ": " + data.unitLanguages.Es
                binding.itemName.text = language.itemName + ": " + data.itemNameLanguages.Es
            }
            "fr" -> {
                binding.unit.text = language.unit + ": " + data.unitLanguages.Fr
                binding.itemName.text = language.itemName + ": " + data.itemNameLanguages.Fr
            }
            "it" -> {
                binding.unit.text = language.unit + ": " + data.unitLanguages.It
                binding.itemName.text = language.itemName + ": " + data.itemNameLanguages.It
            }
            "ru" -> {
                binding.unit.text = language.unit + ": " + data.unitLanguages.Ru
                binding.itemName.text = language.itemName + ": " + data.itemNameLanguages.Ru
            }
            "tr" -> {
                binding.unit.text = language.unit + ": " + data.unitLanguages.Tr
                binding.itemName.text = language.itemName + ": " + data.itemNameLanguages.Tr
            }
        }

        return binding.root
    }

    private fun retApiData(barcode: String) {
        viewModel.retUser().observe(viewLifecycleOwner) {
            viewModel.retRetrofitData(
                it[0].schema, barcode, it[0].loginCount, it[0].employeeNumber
            )
        }
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

            val builder =
                AlertDialog.Builder(requireContext()).setView(manualBarcodeViewHolderBinding.root)
                    .setTitle("Barcode").setNegativeButton("Cancel") { _, _ -> }
                    .setPositiveButton("Ok") { _, _ -> }

            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (manualBarcodeViewHolderBinding.barcode.text.toString().isNotEmpty()) {
                    alertDialog.dismiss()
                    viewModel.barcode.setValue(manualBarcodeViewHolderBinding.barcode.text.toString())
                } else {
                    CustomToast.show(requireContext(), "Please fill the barcode!")
                }
            }
        }
    }

    private fun scanCameraBarcodeListener() {
        binding.scanButtonCamera.setOnClickListener {
            val scanOptions = ScanOptions()
                .setPrompt("Volume up to flash on")
                .setBeepEnabled(true)
                .setOrientationLocked(true)
                .setCaptureActivity(CaptureAct::class.java)
            barLauncher.launch(scanOptions)
        }
    }

    private var barLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents != null) {

            val updatedBarcode: String = makeupBarcode(result.contents)

            marbleObserver()
            retApiData(updatedBarcode)
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
        viewModel.connectionStatus.observe(viewLifecycleOwner) {
            it?.let {
                if (!it) {
                    AlertDialog.Builder(requireContext())
                        .setMessage("Please check the token or the internet connection")
                        .setTitle("Error")
                        .setPositiveButton("OK") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }.show()
                }
            }
        }
    }
}