package com.example.barcodeReader.fragments.mainMenuFragment

import com.example.barcodeReader.Arabic
import com.example.barcodeReader.English
import com.example.barcodeReader.Language

class LanguageFactory {
    fun getLanguage(languageString: String): Language {
        if (languageString == "ar")
            return Arabic()
        return English()
    }
}