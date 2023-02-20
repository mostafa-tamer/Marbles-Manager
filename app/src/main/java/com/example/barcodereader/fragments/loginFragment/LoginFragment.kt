package com.example.barcodereader.fragments.loginFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.barcodereader.R
import com.example.barcodereader.databaes.TopSoftwareDatabase
import com.example.barcodereader.databinding.FragmentLoginBinding
import com.example.barcodereader.databinding.FragmentLoginSavedUserButtonBinding
import com.example.barcodereader.databinding.FragmentLoginSavedUsersContainerBinding
import com.example.barcodereader.utils.CustomAlertDialog
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginFragmentViewModel

    private lateinit var loginFailedAlertDialog: CustomAlertDialog
    private lateinit var tokenInternetAlertDialog: CustomAlertDialog
    private lateinit var savedUsersAlertDialogIsNotEmpty: CustomAlertDialog
    private lateinit var savedUsersAlertDialogIsEmpty: CustomAlertDialog

    private lateinit var fillAllFieldsToast: Toast
    private lateinit var clearedToast: Toast
    private lateinit var errorOccurredToast: Toast

    private var isNavigating = false

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
        errorOccurredToast = createToast("Error Occurred")
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
        isNetworkBusyObserver()
        connectionStatusObserver()
        saveUserDatabaseStatusObserver()
        saveRememberedUsersDatabaseStatusObserver()
    }

    private fun listeners() {
        loginButtonClickListener()
        savedUsersListener()
    }


    private fun isNetworkBusyObserver() {
        viewModel.isBusy.observe(viewLifecycleOwner) {
            if (it == false) {
                binding.progressBar.visibility = View.INVISIBLE
            } else {
                binding.progressBar.visibility = View.VISIBLE
            }
        }
    }

    private fun savedUsersListener() {
        binding.savedUsers.setOnClickListener {
            if (!isNavigating) {
                lifecycleScope.launch {
                    val users = viewModel.getSavedUsersSuspend()

                    if (users.isNotEmpty()) {
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

                        savedUsersAlertDialogIsNotEmpty
                            .setTitle("Saved Users")
                            .setPositiveButton("OK") {
                                it.dismiss()
                                it.dismiss()
                            }
                            .setBody(fragmentLoginSavedUsersViewHolderBinding.root)
                            .setNegativeButton("Clear") {
                                viewModel.clearSavedUsersData()
                                clearedToast.show()
                                it.dismiss()
                            }.showDialog()

                    } else {
                        savedUsersAlertDialogIsEmpty
                            .setTitle("Saved Users")
                            .setMessage("There is no saved users")
                            .setPositiveButton("OK") {
                                it.dismiss()
                            }.showDialog()
                    }
                }
            }
        }
    }

    private fun saveRememberedUsersDatabaseStatusObserver() {
        viewModel.saveRememberedUsersDatabaseStatus.observe(viewLifecycleOwner) {
            if (it == false) {
                errorOccurredToast.show()
            }
        }
    }

    private fun responseObserver() {
        viewModel.loginResponse.work {
            it?.let {
                if (it.code() == 200) {
                    isNavigating = true
                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToScanFragment()
                    )
                } else {
                    loginFailedAlertDialog
                        .setMessage(it.body()!!.message)
                        .setTitle("Login Failed")
                        .setPositiveButton(getString(R.string.ok)) {
                            it.dismiss()
                        }.showDialog()
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

    private fun saveUserDatabaseStatusObserver() {
        viewModel.saveUserDatabaseStatus.observe(viewLifecycleOwner) {
            if (it == false) {
                errorOccurredToast.show()
            }
        }
    }

    private fun connectionStatusObserver() {
        viewModel.connectionStatus.work {
            it?.let { connectionStatus ->
                if (!connectionStatus) {
                    tokenInternetAlertDialog
                        .setMessage("Please check the token or the internet connection")
                        .setTitle("Login Failed")
                        .setPositiveButton("OK") {
                            it.dismiss()
                        }.showDialog()
                }
            }
        }
    }


    private fun alertDialogsInitialization() {
        loginFailedAlertDialog = CustomAlertDialog(requireContext())
        tokenInternetAlertDialog = CustomAlertDialog(requireContext())
        savedUsersAlertDialogIsNotEmpty = CustomAlertDialog(requireContext())
        savedUsersAlertDialogIsEmpty = CustomAlertDialog(requireContext())
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
}