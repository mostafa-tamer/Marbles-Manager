package com.example.barcodereader.fragments.resultFragment


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.barcodereader.databaes.UserDao

class ResultFragmentViewModel(private val dataSource: UserDao) : ViewModel() {


    class ResultFragmentViewModelFactory(private val dataSource: UserDao) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ResultFragmentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return ResultFragmentViewModel(dataSource) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}