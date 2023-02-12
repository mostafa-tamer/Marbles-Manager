package com.example.barcodereader.fragments.inventoryFragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.barcodereader.UserData
import com.example.barcodereader.databinding.FragmentInventoryBinding
import com.example.barcodereader.databinding.FragmentInventoryCardsBinding
import com.example.barcodereader.utils.CustomToast
import com.udacity.asteroidradar.database.TopSoftwareDatabase

class InventoryFragment : Fragment() {

    lateinit var binding: FragmentInventoryBinding

    lateinit var viewModel: InventoryViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentInventoryBinding.inflate(layoutInflater)
        viewModelInitialization()

        viewModel.getBranches()

        groupsObserver()
        connectionStatusObserver()


        return binding.root
    }

    private fun connectionStatusObserver() {
        viewModel.connectionStatus.observe(viewLifecycleOwner) {
            it?.let {
                if (!it) {
                    AlertDialog.Builder(requireContext())
                        .setMessage("Please check the token or the internet connection")
                        .setTitle("Error")
                        .setPositiveButton("OK") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }.show()
                }
            }
        }
    }

    private fun groupsObserver() {
        viewModel.groups.observe(viewLifecycleOwner) {
            it?.let { response ->
                if (response.code() == 200) {
                    response.body()!!.data.forEach { branch ->
                        val fragmentInventoryCardBinding =
                            FragmentInventoryCardsBinding.inflate(layoutInflater)
                        fragmentInventoryCardBinding.factoryButton.text =
                            "${branch.groupName}\n${branch.groupCode}"

                        fragmentInventoryCardBinding.factoryButton.setOnClickListener {
                            findNavController().navigate(
                                InventoryFragmentDirections.actionInventoryFragmentToScanInventoryFragment(
                                    branch.groupName,
                                    branch.groupCode,
                                    UserData.data.loginLanguage
                                )
                            )
                        }

                        binding.container.addView(fragmentInventoryCardBinding.root)
                    }
                } else {
                    CustomToast.show(requireContext(), "Failed to get Data")
                }
            }
        }
    }

    private fun viewModelInitialization() {
        val userDao = TopSoftwareDatabase.getInstance(requireContext()).userDao
        val viewModelFactory = InventoryViewModel.InventoryFragmentViewModelFactory(userDao)
        viewModel = ViewModelProvider(this, viewModelFactory)[InventoryViewModel::class.java]
    }

}