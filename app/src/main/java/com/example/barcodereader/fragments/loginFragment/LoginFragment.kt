package com.example.barcodereader.fragments.loginFragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.barcodereader.databinding.FragmentLoginBinding
import com.example.barcodereader.databinding.FragmentLoginSavedUserButtonBinding
import com.example.barcodereader.databinding.FragmentLoginSavedUsersContainerBinding
import com.example.barcodereader.utils.CustomToast
import com.example.barcodereader.databaes.TopSoftwareDatabase

class LoginFragment : Fragment() {

    lateinit var binding: FragmentLoginBinding
    lateinit var viewModel: LoginFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentLoginBinding.inflate(layoutInflater)

        viewModelInitialization()

        observers()
        listeners()

        return binding.root
    }

    private fun listeners() {
        loginButtonClickListener()
        savedUsersListener()
    }

    private fun savedUsersListener() {
        var isShowing = false
        binding.savedUsers.setOnClickListener {
            var firstTry = false
            if (!isShowing) {
                isShowing = true
                viewModel.getSavedUsers().observe(viewLifecycleOwner) {
                    it?.let {
                        if (it.isNotEmpty()) {
                            val fragmentLoginSavedUsersViewHolderBinding =
                                FragmentLoginSavedUsersContainerBinding.inflate(layoutInflater)

                            val alertDialog = AlertDialog.Builder(requireContext())
                                .setTitle("Saved Users")
                                .setView(fragmentLoginSavedUsersViewHolderBinding.root)
                                .setPositiveButton("OK") { _, _ -> }
                                .setNegativeButton("Clear") { _, _ ->
                                    viewModel.clearSavedUsersData()
                                    CustomToast.show(requireContext(), "Cleared!")
                                }.setOnDismissListener {
                                    firstTry = true
                                    isShowing = false
                                }.show()

                            it.forEach { savedUser ->

                                val fragmentLoginSavedUserButtonBinding =
                                    FragmentLoginSavedUserButtonBinding.inflate(layoutInflater)

                                fragmentLoginSavedUserButtonBinding.usernameButton.text =
                                    savedUser.userName

                                fragmentLoginSavedUsersViewHolderBinding.container.addView(
                                    fragmentLoginSavedUserButtonBinding.root
                                )

                                fragmentLoginSavedUserButtonBinding.usernameButton.setOnClickListener {
                                    binding.username.setText(savedUser.userName)
                                    binding.password.setText(savedUser.password)
                                    binding.token.setText(savedUser.token)
                                    alertDialog.dismiss()
                                }
                            }

                        } else if (it.isEmpty() && !firstTry) {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Saved Users")
                                .setMessage("There is no saved users")
                                .setPositiveButton("OK") { _, _ -> }
                                .setOnDismissListener {
                                    isShowing = false
                                }.show()
                        }
                    }
                }
            }
        }
    }


    private fun observers() {
        responseObserver()
        connectionStatusObserver()
        saveUserDatabaseStatusObserver()
        saveRememberedUsersDatabaseStatusObserver()
    }


    private fun saveRememberedUsersDatabaseStatusObserver() {
        viewModel.saveRememberedUsersDatabaseStatus.observe(viewLifecycleOwner) {
            if (it == false) {
                CustomToast.show(requireContext(), "Error Occurred")
            }
        }
    }

    private fun responseObserver() {
        viewModel.response.work {
            it?.let {
                if (it.code() == 200) {
                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToScanFragment()
                    )
                } else {
                    AlertDialog.Builder(requireContext())
                        .setMessage(it.body()!!.message)
                        .setTitle("Login Failed").setPositiveButton(
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
        val savedUserDao = TopSoftwareDatabase.getInstance(requireContext()).savedUsersDao
        val loginFragmentFactory =
            LoginFragmentViewModel.LoginFragmentViewModelFactory(userDao, savedUserDao)
        viewModel =
            ViewModelProvider(this, loginFragmentFactory)[LoginFragmentViewModel::class.java]
    }

    private fun saveUserDatabaseStatusObserver() {
        viewModel.saveUserDatabaseStatus.observe(viewLifecycleOwner) {
            if (it == false) {
                CustomToast.show(requireContext(), "Error Occurred")
            }
        }
    }

    private fun connectionStatusObserver() {
        viewModel.connectionStatus.work {
            it?.let {
                if (!it) {
                    AlertDialog.Builder(requireContext())
                        .setMessage("Please check the token or the internet connection")
                        .setTitle("Login Failed")
                        .setPositiveButton("OK") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }.show()
                }
            }
        }
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
                try {
                    viewModel.login(username, password, token)
                } catch (e: Exception) {
                    CustomToast.show(requireContext(), "Please insert valid token!")
                }
            } else {
                CustomToast.show(requireContext(), "Please fill all fields!")
            }
        }
    }
}