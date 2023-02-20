package com.example.barcodeReader.fragments.inventoryFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.barcodeReader.databinding.FragmentGroupBinding
import com.example.barcodeReader.databinding.FragmentInventoryCardsBinding
import com.example.barcodeReader.network.properties.get.groups.Branch
import com.example.barcodeReader.userData

class GroupFragment : Fragment() {

    lateinit var binding: FragmentGroupBinding
    lateinit var args: GroupFragmentArgs

    private var lock = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupBinding.inflate(layoutInflater)
        args = GroupFragmentArgs.fromBundle(requireArguments())

        showData()

        return binding.root
    }

    private fun showData() {
        args.metaData.branchList.forEach { branch ->
            val fragmentInventoryCardBinding =
                FragmentInventoryCardsBinding.inflate(layoutInflater)
            fragmentInventoryCardBinding.factoryButton.text =
                groupNameLanguage(branch)

            fragmentInventoryCardBinding.factoryButton.setOnClickListener {
                if (!lock) {
                    lock = true
                    findNavController().navigate(
                        GroupFragmentDirections.actionInventoryFragmentToInvoiceFragment(
                            groupNameLanguage(branch),
                            branch.groupCode,
                            branch.groupMgr,
                            args.metaData
                        )
                    )
                }
            }

            binding.container.addView(fragmentInventoryCardBinding.root)
        }
    }

    private fun groupNameLanguage(branch: Branch): String {
        return when (userData.loginLanguage) {
            "ar" -> {
                branch.groupName
            }
            "en" -> {
                branch.groupNameEn
            }
            "de" -> {
                branch.groupNameDe
            }
            "es" -> {
                branch.groupNameEs
            }
            "fr" -> {
                branch.groupNameFr
            }
            "it" -> {
                branch.groupNameIt
            }
            "ru" -> {
                branch.groupNameRu
            }
            else -> {
                branch.groupNameTr
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lock = false
    }
}