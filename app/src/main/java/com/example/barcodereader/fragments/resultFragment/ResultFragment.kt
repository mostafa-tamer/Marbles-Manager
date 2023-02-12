package com.example.barcodereader.fragments.resultFragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.barcodereader.R
import com.example.barcodereader.databinding.FragmentResultBinding
import com.example.barcodereader.fragments.scanFragment.LanguageFactory
import com.example.barcodereader.network.properties.get.marble.BrandLanguages
import com.example.barcodereader.network.properties.get.marble.Data
import com.example.barcodereader.network.properties.get.marble.Table
import com.udacity.asteroidradar.database.TopSoftwareDatabase


class ResultFragment : Fragment() {

    private lateinit var binding: FragmentResultBinding
    private lateinit var viewModel: ResultFragmentViewModel
    lateinit var args: ResultFragmentArgs
    private val adapter = RecyclerViewAdapter()
    private val list = mutableListOf<Table>(
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).let {
            it.window?.statusBarColor = resources.getColor(R.color.red)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as AppCompatActivity).let {
            it.window?.statusBarColor = resources.getColor(R.color.white)
        }
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_RTL
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentResultBinding.inflate(layoutInflater)
        args = ResultFragmentArgs.fromBundle(requireArguments())
        viewModelInitialization()
        binding.root.layoutDirection = View.LAYOUT_DIRECTION_LTR

        showData(args.data, args.languageString)

        binding.recyclerView.adapter = adapter

        return binding.root
    }


    private fun testData() {
        for (i in 0..1000) {
            list += Table(
                "25", BrandLanguages("", "", "", "", "", "", ""), "Factory Name", "50",
                ""
            )
        }
        adapter.submitList(list)
    }

    private fun viewModelInitialization() {
        val dataSource = TopSoftwareDatabase.getInstance(requireContext()).userDao
        val resultFragmentFactory =
            ResultFragmentViewModel.ResultFragmentViewModelFactory(dataSource)
        viewModel =
            ViewModelProvider(this, resultFragmentFactory)[ResultFragmentViewModel::class.java]
    }


    private fun showData(data: Data, languageString: String) {

        val languageFactory = LanguageFactory()
        val language = languageFactory.getLanguage(languageString)

        binding.frz.text = language.frz + ": " + data.frz
        binding.blockNumber.text = language.blockNumber + ": " + data.blockNumber
        binding.price.text = language.price + ": " + data.price
        binding.height.text = language.height + ": " + data.zdimension
        binding.length.text = language.length + ": " + data.xdimension
        binding.width.text = language.width + ": " + data.ydimension
        binding.itemCode.text = language.itemCode + ": " + data.itemCode

        when (languageString) {
            "ar" -> {
                binding.unit.text = language.unit + ": " + data.unit
                binding.itemName.text = language.itemName + ": " + data.itemName
                binding.root.layoutDirection = View.LAYOUT_DIRECTION_RTL
            }
            "en" -> {
                binding.unit.text = language.unit + ": " + data.unitLanguages.En
                binding.itemName.text = language.itemName + ": " + data.itemNameLanguages.En
            }
            "de" -> {
                binding.unit.text = language.unit + ": " + data.unitLanguages.De
                binding.itemName.text = language.itemName + ": " + data.itemNameLanguages.De
            }
            "es" -> {
                binding.unit.text = language.unit + ": " + data.unitLanguages.Es
                binding.itemName.text = language.itemName + ": " + data.itemNameLanguages.Es
            }
            "fr" -> {
                binding.unit.text = language.unit + ": " + data.unitLanguages.Fr
                binding.itemName.text = language.itemName + ": " + data.itemNameLanguages.Fr
            }
            "it" -> {
                binding.unit.text = language.unit + ": " + data.unitLanguages.It
                binding.itemName.text = language.itemName + ": " + data.itemNameLanguages.It
            }
            "ru" -> {
                binding.unit.text = language.unit + ": " + data.unitLanguages.Ru
                binding.itemName.text = language.itemName + ": " + data.itemNameLanguages.Ru
            }
            "tr" -> {
                binding.unit.text = language.unit + ": " + data.unitLanguages.Tr
                binding.itemName.text = language.itemName + ": " + data.itemNameLanguages.Tr
            }
        }

        adapter.submitList(data.table)
    }

}