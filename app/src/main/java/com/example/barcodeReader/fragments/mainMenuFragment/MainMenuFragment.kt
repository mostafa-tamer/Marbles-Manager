package com.example.barcodeReader.fragments.mainMenuFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.barcodeReader.R
import com.example.barcodeReader.database.TopSoftwareDatabase
import com.example.barcodeReader.databinding.FragmentScanBinding
import com.example.barcodeReader.databinding.FragmentScanManualBarcodeViewHolderBinding
import com.example.barcodeReader.userData
import com.example.barcodeReader.utils.CaptureAct
import com.example.barcodeReader.utils.CustomAlertDialog
import com.example.barcodeReader.utils.alertDialogErrorMessageObserver
import com.example.barcodeReader.utils.makeupBarcode
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions


class MainMenuFragment : Fragment() {

    private lateinit var binding: FragmentScanBinding
    private lateinit var viewModel: MainMenuViewModel

    private lateinit var manualAlertDialog: CustomAlertDialog
    private lateinit var marblesErrorAlertDialog: CustomAlertDialog
    private lateinit var failToGetDataAlertDialog: CustomAlertDialog
    private lateinit var logoutInsuranceAlertDialog: CustomAlertDialog
    private lateinit var connectionStatusAlertDialog: CustomAlertDialog
    private lateinit var exceptionErrorMessageAlertDialog: CustomAlertDialog

    private lateinit var errorOccurred: Toast
    private lateinit var fillAllFieldsToast: Toast
    private lateinit var pleaseWriteBarcodeToast: Toast
    private lateinit var pleaseWaitTheInteractionToast: Toast

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanBinding.inflate(layoutInflater)
        binding.username.text = "Welcome ${userData.name}"

        viewModelInitialization()

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
        marbleObserver()
        barcodeObserver()
        groupsPillObserver()
        alertDialogErrorMessageObserver(
            viewModel.alertDialogErrorMessageLiveData,
            viewLifecycleOwner,
            exceptionErrorMessageAlertDialog
        )
        logoutStatusObserver()
        isLogoutBusyObserver()
        isRetMarbleDataBusyObserver()
        isRetBranchesAndBillsDataBusyObserver()
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
        manualAlertDialog = CustomAlertDialog(requireContext())
        marblesErrorAlertDialog = CustomAlertDialog(requireContext())
        failToGetDataAlertDialog = CustomAlertDialog(requireContext())
        logoutInsuranceAlertDialog = CustomAlertDialog(requireContext())
        connectionStatusAlertDialog = CustomAlertDialog(requireContext())
        exceptionErrorMessageAlertDialog = CustomAlertDialog(requireContext())
    }

    private fun lockButtons() {
        binding.inventory.lockButton()
        binding.logoutButton.lockButton()
        binding.scanButtonManual.lockButton()
        binding.scanButtonCamera.lockButton()
    }

    private fun unlockButtons() {
        binding.inventory.unlockButton()
        binding.logoutButton.unlockButton()
        binding.scanButtonCamera.unlockButton()
        binding.scanButtonManual.unlockButton()
    }

    private fun inventoryButtonListener() {
        binding.inventory.setOnClickListener {
            viewModel.retBranchesAndPills()
        }
    }

    private fun isRetMarbleDataBusyObserver() {
        viewModel.isRetMarbleDataBusyLiveData.observe(viewLifecycleOwner) {
            it?.let {
                busyViewModelController(it)
            }
        }
    }

    private fun isRetBranchesAndBillsDataBusyObserver() {
        viewModel.isRetBranchesAndBillsDataBusyLiveData.observe(viewLifecycleOwner) {
            it?.let {
                busyViewModelController(it)
            }
        }
    }

    private fun isLogoutBusyObserver() {
        viewModel.isLogoutBusyLiveData.observe(viewLifecycleOwner) {
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

    override fun onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment: Boolean) {
        super.onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment)
        if (::binding.isInitialized) {
            if (isPrimaryNavigationFragment) {
                unlockButtons()
            } else {
                lockButtons()
            }
        }
    }

    private fun groupsPillObserver() {
        viewModel.groupsAndPillsBodyObservable.work {
            it?.let { response ->
                if (response.statusCode == 200) {
                    findNavController().navigate(
                        MainMenuFragmentDirections.actionScanFragmentToInventoryFragment(
                            response.data.metaData
                        )
                    )
                } else {
                    failToGetDataAlertDialog
                        .setTitle("Error")
                        .setMessage(response.message)
                        .setPositiveButton("Ok")
                        .showDialog()
                }
            }
        }
    }

    private fun marbleObserver() {
        viewModel.marblesBodyObservable.work { it ->
            it?.let {
                if (it.statusCode == 200) {
                    findNavController().navigate(
                        MainMenuFragmentDirections.actionScanFragmentToResultFragment(
                            it.data.metaData,
                            userData.loginLanguage.lowercase()
                        )
                    )
                } else {
                    marblesErrorAlertDialog
                        .setTitle(getString(R.string.error))
                        .setMessage(it.message)
                        .setPositiveButton("OK")
                        .showDialog()
                }
            }
        }
    }

    private fun retApiData(barcode: String) {
        viewModel.retMarbleData(
            userData.schema, barcode, userData.loginCount, userData.employeeNumber
        )
    }

    private fun barcodeObserver() {
        viewModel.barcodeObservable.work {
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
                    if (
                        !viewModel.isRetMarbleDataBusyLiveData.value!! &&
                        !viewModel.isRetBranchesAndBillsDataBusyLiveData.value!! &&
                        !viewModel.isLogoutBusyLiveData.value!!
                    ) {
                        if (manualBarcodeViewHolderBinding.barcode.text.toString().isNotEmpty()) {
                            it.dismiss()
                            viewModel.barcodeObservable.setValue(manualBarcodeViewHolderBinding.barcode.text.toString())
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
            viewModel.barcodeObservable.setValue(result.contents)
        }
    }

    private fun logoutStatusObserver() {
        viewModel.logoutStatusObservable.work {
            it?.let {
                if (!it) {
                    errorOccurred.show()
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
            logoutInsuranceAlertDialog.setTitle(getString(R.string.warning))
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Ok") {
                    viewModel.logout()
                }.setNegativeButton(getString(R.string.cancel))
                .showDialog()
        }
    }

    override fun onStop() {
        manualAlertDialog.dismiss()
        super.onStop()
    }
}