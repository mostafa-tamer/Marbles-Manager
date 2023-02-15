package com.example.barcodereader

import android.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.barcodereader.databinding.ActivityMainBinding
import org.koin.core.component.getScopeName


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        this.supportActionBar?.elevation = 0f

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }


}