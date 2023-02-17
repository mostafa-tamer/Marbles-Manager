package com.example.barcodereader.fragments.invoiceFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.barcodereader.databinding.FragmentInventoryCardsBinding
import com.example.barcodereader.databinding.FragmentInvoiceBinding

class InvoiceFragment : Fragment() {

    lateinit var binding: FragmentInvoiceBinding
    lateinit var args: InvoiceFragmentArgs

    private var lock = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInvoiceBinding.inflate(layoutInflater)
        args = InvoiceFragmentArgs.fromBundle(requireArguments())
        showData()
        return binding.root
    }

    private fun showData() {
        args.group.pillTypeList.forEach { pill ->
            val fragmentInventoryCardBinding =
                FragmentInventoryCardsBinding.inflate(layoutInflater)
            fragmentInventoryCardBinding.factoryButton.text =
                "${pill.nameAr}"
            fragmentInventoryCardBinding.factoryButton.setOnClickListener {
                if (!lock) {
                    lock = true
                    findNavController().navigate(
                        InvoiceFragmentDirections.actionInvoiceFragmentToScanInventoryFragment(
                            args.groupName,
                            args.groupCode,
                            args.groupMgr,
                            pill.nameAr,
                            pill.code
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