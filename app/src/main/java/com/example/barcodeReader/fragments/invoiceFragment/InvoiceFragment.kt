package com.example.barcodeReader.fragments.invoiceFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.barcodeReader.databinding.FragmentInventoryCardsBinding
import com.example.barcodeReader.databinding.FragmentInvoiceBinding
import com.example.barcodeReader.network.properties.get.groups.PillType
import com.example.barcodeReader.userData

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
        args.metaData.pillTypeList.forEach { pill ->
            val fragmentInventoryCardBinding =
                FragmentInventoryCardsBinding.inflate(layoutInflater)
            fragmentInventoryCardBinding.factoryButton.text =
                pillNameLanguage(pill)
            fragmentInventoryCardBinding.factoryButton.setOnClickListener {
                if (!lock) {
                    lock = true
                    findNavController().navigate(
                        InvoiceFragmentDirections.actionInvoiceFragmentToScanInventoryFragment(
                            args.groupName,
                            args.groupCode,
                            args.groupMgr,
                            pillNameLanguage(pill),
                            pill.code
                        )
                    )
                }
            }
            binding.container.addView(fragmentInventoryCardBinding.root)
        }
    }

    private fun pillNameLanguage(pillType: PillType): String {

        return when (userData.loginLanguage) {
            "ar" -> {
                pillType.nameAr
            }
            "en" -> {
                pillType.nameEn
            }
            "de" -> {
                pillType.nameDe
            }
            "es" -> {
                pillType.nameEs
            }
            "fr" -> {
                pillType.nameFr
            }
            "it" -> {
                pillType.nameIt
            }
            "ru" -> {
                pillType.nameRu
            }
            else -> {
                pillType.nameTr
            }
        }
    }

    override fun onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment: Boolean) {
        lock = !isPrimaryNavigationFragment
        super.onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment)
    }
}