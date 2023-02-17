package com.example.barcodereader.fragments.inventoryFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.barcodereader.databinding.FragmentGroupBinding
import com.example.barcodereader.databinding.FragmentInventoryCardsBinding

class GroupFragment : Fragment() {

    lateinit var binding: FragmentGroupBinding
    lateinit var args: GroupFragmentArgs

    private   var lock = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupBinding.inflate(layoutInflater)
        args = GroupFragmentArgs.fromBundle(requireArguments())

        showData()

        return binding.root
    }

    private fun showData() {
        args.group.branchList.forEach { branch ->
            val fragmentInventoryCardBinding =
                FragmentInventoryCardsBinding.inflate(layoutInflater)
            fragmentInventoryCardBinding.factoryButton.text =
                "${branch.groupName}"

            fragmentInventoryCardBinding.factoryButton.setOnClickListener {
                if (!lock) {
                    lock = true
                    findNavController().navigate(
                        GroupFragmentDirections.actionInventoryFragmentToInvoiceFragment(
                            branch.groupName,
                            branch.groupCode,
                            branch.groupMgr,
                            args.group
                        )
                    )
                }
            }

            binding.container.addView(fragmentInventoryCardBinding.root)
        }
    }

    override fun onResume() {
        super.onResume()
        lock = false
    }
}