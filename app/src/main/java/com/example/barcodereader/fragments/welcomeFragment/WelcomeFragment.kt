package com.example.barcodereader.fragments.welcomeFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.barcodereader.UserData
import com.example.barcodereader.databinding.FragmentWelcomeBinding
import com.udacity.asteroidradar.database.TopSoftwareDatabase
import com.udacity.asteroidradar.database.User

class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentWelcomeBinding.inflate(layoutInflater)

        val dataSource = TopSoftwareDatabase.getInstance(requireContext()).userDao
        val welcomeFragmentViewModelFactory =
            WelcomeFragmentViewModel.WelcomeFragmentViewModelFactory(dataSource)
        val welcomeFragmentViewModel = ViewModelProvider(
            this,
            welcomeFragmentViewModelFactory
        )[WelcomeFragmentViewModel::class.java]

        welcomeFragmentViewModel.status.observe(viewLifecycleOwner) {
            if (it == false) {
                Toast.makeText(requireContext(), "Error Occurs", Toast.LENGTH_SHORT).show()
            }
        }

        welcomeFragmentViewModel.retUser()?.observe(viewLifecycleOwner) {

            UserData.data = it[0]
            println(UserData.data)
            if (it.isNotEmpty()) {
                findNavController().navigate(
                    WelcomeFragmentDirections.actionWelcomeFragmentToScanFragment()
                )
            } else {
                findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToLoginFragment())
            }
        }


        return binding.root
    }

}