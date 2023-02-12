package com.example.barcodereader.fragments.inventoryScanFragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.contains
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.barcodereader.databinding.FragmentInventoryScanBinding
import com.example.barcodereader.databinding.FragmentInventoryScanItemsViewHolderBinding
import com.example.barcodereader.databinding.FragmentScanManualBarcodeViewHolderBinding
import com.example.barcodereader.fragments.scanFragment.LanguageFactory
import com.example.barcodereader.network.properties.get.marble.Data
import com.example.barcodereader.utils.CaptureAct
import com.example.barcodereader.utils.CustomToast
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.udacity.asteroidradar.database.TopSoftwareDatabase

class InventoryScanFragment : Fragment() {
    private lateinit var binding: FragmentInventoryScanBinding
    private lateinit var viewModel: InventoryScanViewModel
    private lateinit var args: InventoryScanFragmentArgs
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        args = InventoryScanFragmentArgs.fromBundle(requireArguments())
        binding = FragmentInventoryScanBinding.inflate(layoutInflater)
        viewModelInitialization()

        binding.groupName.text = "Group Name: ${args.groupName}"
        binding.groupCode.text = "Group Number: ${args.groupCode}"

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

    private fun marbleObserver() {
        viewModel.marble.work { it ->
            it?.let {
                if (it.code() == 200) {
                    println(it.body())
                    if (binding.container.contains(binding.dummyText)){
                        binding.container.removeView(binding.dummyText)
                    }
                    binding.container.addView(showData(it.body()!!.data, args.languageString))
                    binding.textItemCount.text =
                        "Scanned Items: ${binding.container.size}"
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
        languageString: String
    ): FrameLayout {

        val languageFactory = LanguageFactory()
        val language = languageFactory.getLanguage(languageString)

        val binding =
            FragmentInventoryScanItemsViewHolderBinding.inflate(layoutInflater)

        binding.frz.text = language.frz + ": " + data.frz
        binding.blockNumber.text = language.blockNumber + ": " + data.blockNumber
        binding.price.text = language.price + ": " + data.price
        binding.height.text = language.height + ": " + data.zdimension
        binding.length.text = language.length + ": " + data.xdimension
        binding.width.text = language.width + ": " + data.ydimension
        binding.itemCode.text = language.itemCode + ": " + data.itemCode

        when (languageString) {
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