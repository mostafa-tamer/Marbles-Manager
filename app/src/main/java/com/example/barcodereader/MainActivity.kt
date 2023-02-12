package com.example.barcodereader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.barcodereader.databinding.ActivityMainBinding
import com.example.barcodereader.utils.errorOccurred


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