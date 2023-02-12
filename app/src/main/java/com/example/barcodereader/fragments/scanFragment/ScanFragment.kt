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
import com.example.barcodereader.databinding.ManualBarcodeViewHolderBinding
import com.example.barcodereader.utils.AESEncryption
import com.example.barcodereader.utils.CaptureAct
import com.example.barcodereader.utils.CustomToast
import com.example.barcodereader.utils.GlobalKeys
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.udacity.asteroidradar.database.TopSoftwareDatabase


class ScanFragment : Fragment() {

    lateinit var binding: FragmentScanBinding
    lateinit var viewModel: ScanFragmentViewModel

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
        scanCameraBarcodeListiner()
        scanManualBarcodeListener()
        logoutButtonListener()
    }

    private fun observers() {
        barcodeObserver()
        statusObserver()
        retUserNameObserver()
    }

    private fun retUserNameObserver() {
        viewModel.retUser().observe(viewLifecycleOwner) {
            if (it != null) {
                binding.username.text = "Welcome " + viewModel.decrypt(it[0].userName)
            }
        }
    }

    private fun statusObserver() {
        viewModel.logoutStatus.observe(viewLifecycleOwner) {
            if (!it) {
                Toast.makeText(requireContext(), "Failed To Logout", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun viewModelInitialization() {
        val dataSource = TopSoftwareDatabase.getInstance(requireContext()).userDao
        val scanFragmentFactory = ScanFragmentViewModel.ScanFragmentViewModelFactory(dataSource)
        viewModel = ViewModelProvider(this, scanFragmentFactory)[ScanFragmentViewModel::class.java]
    }

    private fun statisticsObserver() {
        viewModel.statistics.work { it ->
            it?.let {
                if (it.code() == 200) {
                    viewModel.retUser().observe(viewLifecycleOwner) { userData ->
                        findNavController().navigate(
                            ScanFragmentDirections.actionScanFragmentToResultFragment(
                                it.body()!!.data, userData[0].loginLanguage.lowercase()
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
            val schema = it[0].schema
            val encryptedBarcode = AESEncryption.encrypt(barcode, GlobalKeys.KEY)
            val employeeNumber = AESEncryption.encrypt(it[0].employeeNumber, GlobalKeys.KEY)

            viewModel.retRetrofitData(
                schema, encryptedBarcode, it[0].loginCount, employeeNumber
            )
        }
    }

    private fun scanCameraBarcodeListiner() {
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

                statisticsObserver()
                retApiData(updatedBarcode)
            }
        }
    }

    private fun logoutButtonListener() {
        binding.logoutButton.setOnClickListener {
            findNavController().navigate(ScanFragmentDirections.actionScanFragmentToLoginFragment())
            viewModel.logout()
        }
    }

    private fun scanManualBarcodeListener() {
        binding.scanButtonManual.setOnClickListener {

            val manualBarcodeViewHolderBinding =
                ManualBarcodeViewHolderBinding.inflate(layoutInflater)

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

            statisticsObserver()
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
}