package com.example.barcodereader.fragments.inventoryFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.barcodereader.UserData
import com.example.barcodereader.databinding.FragmentInventoryBinding
import com.example.barcodereader.databinding.FragmentInventoryCardsBinding

class InventoryFragment : Fragment() {

    lateinit var binding: FragmentInventoryBinding
    lateinit var args: InventoryFragmentArgs
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentInventoryBinding.inflate(layoutInflater)

        args = InventoryFragmentArgs.fromBundle(requireArguments())

        showData()

        return binding.root
    }

    private fun showData() {
        args.group.forEach { branch ->
            val fragmentInventoryCardBinding =
                FragmentInventoryCardsBinding.inflate(layoutInflater)
            fragmentInventoryCardBinding.factoryButton.text =
                "${branch.groupName}"

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
    }


}