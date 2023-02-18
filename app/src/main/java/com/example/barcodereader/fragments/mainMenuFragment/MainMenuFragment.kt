package com.example.barcodereader.fragments.mainMenuFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.barcodereader.databaes.TopSoftwareDatabase
import com.example.barcodereader.databinding.FragmentScanBinding
import com.example.barcodereader.databinding.FragmentScanManualBarcodeViewHolderBinding
import com.example.barcodereader.userData
import com.example.barcodereader.utils.CaptureAct
import com.example.barcodereader.utils.CustomAlertDialog
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainMenuFragment : Fragment() {

    private lateinit var binding: FragmentScanBinding
    private lateinit var viewModel: MainMenuViewModel

    private lateinit var connectionStatusAlertDialog: CustomAlertDialog
    private lateinit var marblesErrorAlertDialog: CustomAlertDialog
    private lateinit var failToGetDataAlertDialog: CustomAlertDialog
    private lateinit var manualAlertDialog: CustomAlertDialog

    private lateinit var pleaseWriteBarcodeToast: Toast
    private lateinit var pleaseWaitTheInteractionToast: Toast
    private lateinit var fillAllFieldsToast: Toast
    private lateinit var errorOccurred: Toast

    private val visibleSpinner = MutableLiveData(4)

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
        inventoryButtonListener()
        scanCameraBarcodeListener()
        scanManualBarcodeListener()
        logoutButtonListener()
    }

    private fun observers() {
        spinnerVisibilityObserver()
        barcodeObserver()
        logoutStatusObserver()
        connectionStatusObserver()
        groupsPillObserver()
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

    private fun toastInitialization() {
        fillAllFieldsToast = createToast("Please fill all fields")
        pleaseWriteBarcodeToast = createToast("Please write the barcode!")
        pleaseWaitTheInteractionToast = createToast("Please wait The interaction!")
        errorOccurred = createToast("Error Occurred!")
    }

    private fun createToast(message: String): Toast {
        return Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        )
    }

    private fun alertDialogInitialization() {
        connectionStatusAlertDialog = CustomAlertDialog(requireContext())
        marblesErrorAlertDialog = CustomAlertDialog(requireContext())
        failToGetDataAlertDialog = CustomAlertDialog(requireContext())
        manualAlertDialog = CustomAlertDialog(requireContext())
    }

    private fun spinnerVisible() {
        binding.progressBar.visibility = View.VISIBLE
        visibleSpinner.setValue(0)
    }

    private fun spinnerInvisible() {
        binding.progressBar.visibility = View.INVISIBLE
        visibleSpinner.setValue(4)
    }

    private fun lockButtons() {
        binding.inventory.lockButton()
        binding.scanButtonManual.lockButton()
        binding.scanButtonCamera.lockButton()
        binding.logoutButton.lockButton()
    }

    private fun unlockButtons() {
        binding.scanButtonCamera.unlockButton()
        binding.scanButtonManual.unlockButton()
        binding.inventory.unlockButton()
        binding.logoutButton.unlockButton()
    }

    private fun inventoryButtonListener() {
        binding.inventory.setOnClickListener {
            viewModel.branchesPills()
            spinnerVisible()
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
                    failToGetDataAlertDialog
                        .setTitle("Error")
                        .setMessage(response.body()!!.message)
                        .setPositiveButton("Ok") {
                            it.dismiss()
                        }.showDialog()
                }

                lifecycleScope.launch {
                    delay(200)
                    spinnerInvisible()
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
                lifecycleScope.launch {
                    delay(200)
                    spinnerInvisible()
                }
            }
        }
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
                    if (visibleSpinner.getValue()!! == 4) {
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
                }.showDialog()
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
                    connectionStatusAlertDialog
                        .setMessage("Please check the token or the internet connection")
                        .setTitle("Error").setPositiveButton("OK") {
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
                    errorOccurred.show()
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

//        binding.logoutButton.setOnTouchListener { _, event ->
//            lockButtons()
//            viewModel.logout()
//            event.action == MotionEvent.ACTION_UP
//        }
//
        binding.logoutButton.setOnClickListener {
            lockButtons()
            viewModel.logout()
        }
    }

    override fun onResume() {
        super.onResume()
        if (visibleSpinner.getValue()!! == 4) {
            unlockButtons()
        }
    }

    override fun onStop() {
        manualAlertDialog.dismiss()
        super.onStop()
    }
}