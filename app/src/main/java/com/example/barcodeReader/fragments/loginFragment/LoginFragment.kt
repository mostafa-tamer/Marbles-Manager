package com.example.barcodeReader.fragments.loginFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.barcodeReader.R
import com.example.barcodeReader.database.SavedUsers
import com.example.barcodeReader.database.TopSoftwareDatabase
import com.example.barcodeReader.databinding.FragmentLoginBinding
import com.example.barcodeReader.databinding.FragmentLoginSavedUserButtonBinding
import com.example.barcodeReader.databinding.FragmentLoginSavedUsersContainerBinding
import com.example.barcodeReader.utils.CustomAlertDialog
import com.example.barcodeReader.utils.alertDialogErrorMessageObserver
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginFragmentViewModel

    private lateinit var loginFailedAlertDialog: CustomAlertDialog
    private lateinit var exceptionErrorMessageAlertDialog: CustomAlertDialog
    private lateinit var savedUsersAlertDialogIsEmpty: CustomAlertDialog
    private lateinit var savedUsersAlertDialogIsNotEmpty: CustomAlertDialog

    private lateinit var fillAllFieldsToast: Toast
    private lateinit var clearedToast: Toast
    private lateinit var errorOccurredToast: Toast

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)

        viewModelInitialization()

        alertDialogsInitialization()
        toastInitialization()

        observers()
        listeners()

        return binding.root
    }

    private fun toastInitialization() {
        fillAllFieldsToast = createToast("Please fill all fields!")
        clearedToast = createToast("Cleared")
        errorOccurredToast = createToast("Error on saving user")
    }

    private fun createToast(message: String): Toast {
        return Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        )
    }

    private fun observers() {
        responseObserver()
        isLoginBusyObserver()
        alertDialogErrorMessageObserver(
            viewModel.alertDialogErrorMessageLiveData,
            viewLifecycleOwner,
            exceptionErrorMessageAlertDialog
        )
    }

    private fun listeners() {
        loginButtonClickListener()
        savedUsersListener()
        offlineModeListener()
    }

    private fun offlineModeListener() {
        binding.offlineModeButton.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToOfflineModeFragment())
        }
    }

    private fun isLoginBusyObserver() {
        viewModel.isLoginBusyLiveData.observe(viewLifecycleOwner) {
            if (it == false) {
                unlockButton()
                binding.progressBar.visibility = View.INVISIBLE
            } else {
                lockButton()
                binding.progressBar.visibility = View.VISIBLE
            }
        }
    }

    private fun savedUsersListener() {
        binding.savedUsers.setOnClickListener {
            lifecycleScope.launch {
                val users = viewModel.getSavedUsersSuspend()

                if (users.isNotEmpty()) {
                    val fragmentLoginSavedUsersViewHolderBinding =
                        savedUsersLayoutInflation(users)

                    savedUsersAlertDialogIsNotEmpty
                        .setTitle("Saved Users")
                        .setPositiveButton("OK")
                        .setBody(fragmentLoginSavedUsersViewHolderBinding.root)
                        .setNegativeButton("Clear") {
                            viewModel.clearSavedUsersData()
                            clearedToast.show()
                        }.showDialog()

                } else {
                    savedUsersAlertDialogIsEmpty
                        .setTitle("Saved Users")
                        .setMessage("There is no saved users")
                        .setPositiveButton("OK")
                        .showDialog()
                }
            }
        }
    }

    private fun savedUsersLayoutInflation(users: List<SavedUsers>): FragmentLoginSavedUsersContainerBinding {
        val fragmentLoginSavedUsersViewHolderBinding =
            FragmentLoginSavedUsersContainerBinding.inflate(layoutInflater)

        users.forEach { savedUser ->
            val fragmentLoginSavedUserButtonBinding =
                FragmentLoginSavedUserButtonBinding.inflate(layoutInflater)

            fragmentLoginSavedUserButtonBinding.usernameButton.text =
                savedUser.userName

            fragmentLoginSavedUserButtonBinding.usernameButton.setOnClickListener {
                binding.username.setText(savedUser.userName)
                binding.password.setText(savedUser.password)
                binding.token.setText(savedUser.token)
                savedUsersAlertDialogIsNotEmpty.dismiss()
            }

            fragmentLoginSavedUsersViewHolderBinding.container.addView(
                fragmentLoginSavedUserButtonBinding.root
            )
        }
        return fragmentLoginSavedUsersViewHolderBinding
    }

    override fun onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment: Boolean) {
        super.onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment)
        if (::binding.isInitialized) {
            if (isPrimaryNavigationFragment) {
                unlockButton()
            } else {
                lockButton()
            }
        }
    }

    private fun lockButton() {
        binding.savedUsers.lockButton()
        binding.loginButton.lockButton()
        binding.offlineModeButton.lockButton()
    }

    private fun unlockButton() {
        binding.savedUsers.unlockButton()
        binding.loginButton.unlockButton()
        binding.offlineModeButton.unlockButton()
    }

    private fun responseObserver() {
        viewModel.loginBody.work {
            it?.let {
                if (it.statusCode == 200) {
                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToScanFragment()
                    )
                } else {
                    loginFailedAlertDialog
                        .setMessage(it.message)
                        .setTitle("Login Failed")
                        .setPositiveButton(getString(R.string.ok))
                        .showDialog()
                }
            }
        }
    }

    private fun viewModelInitialization() {
        val userDao = TopSoftwareDatabase.getInstance(requireContext()).userDao
        val savedUserDao = TopSoftwareDatabase.getInstance(requireContext()).savedUsersDao
        val loginFragmentFactory =
            LoginFragmentViewModel.LoginFragmentViewModelFactory(userDao, savedUserDao)
        viewModel =
            ViewModelProvider(this, loginFragmentFactory)[LoginFragmentViewModel::class.java]
    }

    private fun loginButtonClickListener() {
        binding.loginButton.setOnClickListener {

            val username = binding.username.text.toString()
            val password = binding.password.text.toString()
            val token = binding.token.text.toString()

            if (username.isNotEmpty() &&
                password.isNotEmpty() &&
                token.isNotEmpty()
            ) {
                viewModel.login(username, password, token)
            } else {
                fillAllFieldsToast.show()
            }
        }
    }

    private fun alertDialogsInitialization() {
        loginFailedAlertDialog = CustomAlertDialog(requireContext())
        exceptionErrorMessageAlertDialog = CustomAlertDialog(requireContext())
        savedUsersAlertDialogIsNotEmpty = CustomAlertDialog(requireContext())
        savedUsersAlertDialogIsEmpty = CustomAlertDialog(requireContext())
    }
}