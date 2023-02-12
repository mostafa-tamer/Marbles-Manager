package com.example.barcodereader.fragments.scanFragment

import com.example.barcodereader.Arabic
import com.example.barcodereader.English
import com.example.barcodereader.Language

class LanguageFactory {
    fun getLanguage(languageString: String): Language {
        if (languageString == "ar")
            return Arabic()
        return English()
    }
}