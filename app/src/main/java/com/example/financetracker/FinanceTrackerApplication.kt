package com.example.financetracker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class FinanceTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val sharedPref = getSharedPreferences("session", MODE_PRIVATE)
        val isDarkTheme = sharedPref.getBoolean("dark_theme", false)
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}