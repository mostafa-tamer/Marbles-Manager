package com.example.barcodereader.fragments.scanFragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.barcodereader.databinding.FragmentScanBinding
import com.example.barcodereader.databinding.FragmentScanManualBarcodeViewHolderBinding
import com.example.barcodereader.utils.CaptureAct
import com.example.barcodereader.utils.CustomToast
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.example.barcodereader.databaes.TopSoftwareDatabase


class MainMenuFragment : Fragment() {

    private lateinit var binding: FragmentScanBinding
    private lateinit var viewModel: MainMenuViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanBinding.inflate(layoutInflater)
        viewModelInitialization()

        observers()
        listeners()

        return binding.root
    }

    private fun listeners() {
        scanCameraBarcodeListener()
        scanManualBarcodeListener()
        logoutButtonListener()
        inventoryButtonListener()
    }

    private fun observers() {
        barcodeObserver()
        logoutStatusObserver()
        connectionStatusObserver()
        retUserNameObserver()
        groupsObserver()
    }

    private fun inventoryButtonListener() {
        binding.inventory.setOnClickListener {
            viewModel.getBranches()
        }
    }

    private fun groupsObserver() {
        viewModel.groups.work {
            it?.let { response ->
                if (response.code() == 200) {
                    findNavController().navigate(
                        MainMenuFragmentDirections.actionScanFragmentToInventoryFragment(
                            response.body()!!.data
                        )
                    )
                } else {
                    CustomToast.show(requireContext(), "Failed to get Data")
                }
            }
        }
    }

    private fun marbleObserver() {
        viewModel.marble.work { it ->
            it?.let {
                if (it.code() == 200) {
                    viewModel.retUser().observe(viewLifecycleOwner) { userData ->
                        findNavController().navigate(
                            MainMenuFragmentDirections.actionScanFragmentToResultFragment(
                                it.body()!!.data, userData.loginLanguage.lowercase()
                            )
                        )
                    }
                } else {
                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setMessage(it.body()?.message).setPositiveButton(
                            "OK"
                        ) { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }.show()
                }
            }
        }
    }

    private fun retApiData(barcode: String) {
        viewModel.retUser().observe(viewLifecycleOwner) {
            viewModel.retRetrofitData(
                it.schema, barcode, it.loginCount, it.employeeNumber
            )
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

    private fun retUserNameObserver() {
        viewModel.retUser().observe(viewLifecycleOwner) {
            if (it != null) binding.username.text = "Welcome ${viewModel.decrypt(it.userName)}"
        }
    }

    private fun logoutStatusObserver() {
        viewModel.logoutStatus.observe(viewLifecycleOwner) {
            if (!it) {
                Toast.makeText(requireContext(), "Failed To Logout", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun viewModelInitialization() {
        val dataSource = TopSoftwareDatabase.getInstance(requireContext()).userDao
        val scanFragmentFactory =
            MainMenuViewModel.ScanFragmentViewModelFactory(dataSource)
        viewModel = ViewModelProvider(this, scanFragmentFactory)[MainMenuViewModel::class.java]
    }


    private fun logoutButtonListener() {
        binding.logoutButton.setOnClickListener {
            findNavController().navigate(MainMenuFragmentDirections.actionScanFragmentToLoginFragment())
            viewModel.logout()
        }
    }
}