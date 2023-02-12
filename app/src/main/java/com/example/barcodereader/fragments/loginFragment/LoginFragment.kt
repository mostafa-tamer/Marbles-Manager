package com.example.barcodereader.fragments.loginFragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.barcodereader.R
import com.example.barcodereader.databinding.FragmentLoginBinding
import com.example.barcodereader.databinding.SavedUseresViewHolderBinding
import com.example.barcodereader.utils.CustomToast
import com.udacity.asteroidradar.database.TopSoftwareDatabase

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
                            val savedUsersViewHolderBinding =
                                SavedUseresViewHolderBinding.inflate(layoutInflater)
                            val alertDialog = AlertDialog.Builder(requireContext())
                                .setTitle("Saved Users")
                                .setView(savedUsersViewHolderBinding.root)
                                .setPositiveButton("OK") { _, _ -> }
                                .setNegativeButton("Clear") { _, _ ->
                                    viewModel.clearSavedUsersData()
                                    CustomToast.show(requireContext(), "Done!")
                                }.setOnDismissListener {
                                    firstTry = true
                                    isShowing = false
                                }.show()

                            for (i in it) {
                                val button = Button(requireContext())
                                val frameLayout = FrameLayout(requireContext())
                                frameLayout.setPadding(15, 15, 15, 15)
                                button.text = i.userName
                                button.setTextColor(resources.getColor(R.color.white))
                                button.background =
                                    resources.getDrawable(R.drawable.saved_users_dialog_buttons)

                                savedUsersViewHolderBinding.container.addView(frameLayout)
                                savedUsersViewHolderBinding.container.addView(button)

                                button.setOnClickListener {
                                    binding.username.setText(i.userName)
                                    binding.password.setText(i.password)
                                    binding.token.setText(i.token)
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
                        .setMessage("Please check your IP or the internet connection")
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