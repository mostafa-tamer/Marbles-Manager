package com.example.barcodereader.fragments.welcomeFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.barcodereader.databaes.TopSoftwareDatabase
import com.example.barcodereader.databinding.FragmentWelcomeBinding
import com.example.barcodereader.userData

class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val binding = FragmentWelcomeBinding.inflate(layoutInflater)

        val dataSource = TopSoftwareDatabase.getInstance(requireContext()).userDao
        val welcomeFragmentViewModelFactory =
            WelcomeFragmentViewModel.WelcomeFragmentViewModelFactory(dataSource)
        val viewModel = ViewModelProvider(
            this, welcomeFragmentViewModelFactory
        )[WelcomeFragmentViewModel::class.java]

        viewModel.status.observe(viewLifecycleOwner) {
            if (it == false) {
                Toast.makeText(requireContext(), "Error Occurs", Toast.LENGTH_SHORT).show()
            }
        }

        val user = viewModel.retUserSuspend()
        if (user != null) {
            userData = user
            println("Welcome Fragment $userData")
            findNavController().navigate(
                WelcomeFragmentDirections.actionWelcomeFragmentToScanFragment()
            )
        } else {
            findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToLoginFragment())
        }

        return binding.root
    }

}