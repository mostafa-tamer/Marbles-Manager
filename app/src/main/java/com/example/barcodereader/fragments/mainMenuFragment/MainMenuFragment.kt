package com.example.barcodereader.fragments.mainMenuFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.barcodereader.databaes.TopSoftwareDatabase
import com.example.barcodereader.databinding.FragmentScanBinding
import com.example.barcodereader.databinding.FragmentScanManualBarcodeViewHolderBinding
import com.example.barcodereader.userData
import com.example.barcodereader.utils.CaptureAct
import com.example.barcodereader.utils.CustomAlertDialog

import com.example.barcodereader.utils.Lock
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions


class MainMenuFragment : Fragment() {

    private lateinit var binding: FragmentScanBinding
    private lateinit var viewModel: MainMenuViewModel

    private val logoutButtonLock = Lock()
    private val cameraButtonLock = Lock()
    private val manualButtonLock = Lock()
    private val inventoryButtonLock = Lock()

    private lateinit var alertDialogConnectionLoading: CustomAlertDialog
    private lateinit var alertDialogManual: CustomAlertDialog
    private lateinit var connectionStatusAlertDialog: CustomAlertDialog
    private lateinit var marblesErrorAlertDialog: CustomAlertDialog


    private lateinit var failToGetDataToast: Toast
    private lateinit var fillAllFieldsToast: Toast

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanBinding.inflate(layoutInflater)
        viewModelInitialization()

        binding.username.text = "Welcome ${userData.name}"

        alertDialogInitialization()
        toastInitialization()

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
//        userLoginObserve()
        barcodeObserver()
        logoutStatusObserver()
        connectionStatusObserver()
        groupsPillObserver()
    }
    private fun toastInitialization() {
        fillAllFieldsToast = createToast("Please fill all fields")
        failToGetDataToast = createToast("Failed to get Data")
    }

    private fun createToast(message: String): Toast {
        return Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        )
    }

    private fun alertDialogInitialization() {
        alertDialogConnectionLoading = CustomAlertDialog(requireContext())
        alertDialogManual = CustomAlertDialog(requireContext())
        connectionStatusAlertDialog = CustomAlertDialog(requireContext())
        marblesErrorAlertDialog = CustomAlertDialog(requireContext())
    }

    private fun userLoginObserve() {
        viewModel.checkConnection()
        viewModel.loginStatus.observe(viewLifecycleOwner) {
            it?.let {

            }
        }
    }

    private fun inventoryButtonListener() {
        binding.inventory.setOnClickListener {
            if (!inventoryButtonLock.status) {
                lockButtons()
                binding.progressBar.visibility = View.VISIBLE
                viewModel.branchesPills()
            }
        }
    }

    private fun groupsPillObserver() {
        viewModel.groups.work {
            it?.let { response ->
                if (response.code() == 200) {
                    findNavController().navigate(
                        MainMenuFragmentDirections.actionScanFragmentToInventoryFragment(
                            response.body()!!.data
                        )
                    )
                } else {
                    failToGetDataToast.show()
                }
            }
        }
    }

    private fun marbleObserver() {
        viewModel.marbles.work { it ->
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
                    marblesErrorAlertDialog
                        .setMessage(it.body()?.message.toString())
                        .setPositiveButton("OK") {
                            it.dismiss()
                        }.showDialog()
                }
                binding.progressBar.visibility = View.GONE
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
            if (!cameraButtonLock.status) {
                lockButtons()
                binding.progressBar.visibility = View.VISIBLE

                val scanOptions = ScanOptions()
                    .setPrompt("Volume up to flash on")
                    .setBeepEnabled(true)
                    .setOrientationLocked(true)
                    .setCaptureActivity(CaptureAct::class.java)
                barLauncher.launch(scanOptions)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!binding.progressBar.isVisible) {
            unlockButtons()
        }
    }

    private fun barcodeObserver() {
        viewModel.barcode.work {
            it?.let {
                val updatedBarcode = makeupBarcode(it)
                marbleObserver()
                binding.progressBar.visibility = View.VISIBLE
                retApiData(updatedBarcode)
            }
        }
    }

    private fun scanManualBarcodeListener() {
        binding.scanButtonManual.setOnClickListener {
            var unLock = true
            if (!manualButtonLock.status) {
                lockButtons()
                val manualBarcodeViewHolderBinding =
                    FragmentScanManualBarcodeViewHolderBinding.inflate(layoutInflater)
                alertDialogManual.setBody(manualBarcodeViewHolderBinding.root)
                    .setTitle("Barcode")
                    .setNegativeButton("Cancel") {
                        it.dismiss()
                    }.setPositiveButton("Ok") {
                        if (manualBarcodeViewHolderBinding.barcode.text.toString().isNotEmpty()) {
                            viewModel.barcode.setValue(manualBarcodeViewHolderBinding.barcode.text.toString())
                            it.dismiss()
                            unLock = false
                        } else {
                            fillAllFieldsToast.show()
                        }
                    }.setOnDismiss {
                        if (unLock)
                            unlockButtons()
                    }.showDialog()
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
        viewModel.connectionStatus.work {
            it?.let {
                if (!it) {
                    binding.progressBar.visibility = View.GONE
                    unlockButtons()
                    connectionStatusAlertDialog
                        .setMessage("Please check the token or the internet connection")
                        .setTitle("Error")
                        .setPositiveButton("OK") {
                            it.dismiss()
                        }.showDialog()
                }
            }
        }
    }

    private fun logoutStatusObserver() {
        viewModel.logoutStatus.work {
            it?.let {
                if (!it) {
                    Toast.makeText(requireContext(), "Failed To Logout", Toast.LENGTH_SHORT).show()
                    unlockButtons()
                } else {
                    findNavController().navigate(MainMenuFragmentDirections.actionScanFragmentToLoginFragment())
                }
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
            if (!logoutButtonLock.status) {
                lockButtons()
                viewModel.logout()
            }
        }
    }

    private fun unlockButtons() {
        inventoryButtonLock.status = false
        logoutButtonLock.status = false
        manualButtonLock.status = false
        cameraButtonLock.status = false
    }

    private fun lockButtons() {
        inventoryButtonLock.status = true
        logoutButtonLock.status = true
        manualButtonLock.status = true
        cameraButtonLock.status = true
    }
}