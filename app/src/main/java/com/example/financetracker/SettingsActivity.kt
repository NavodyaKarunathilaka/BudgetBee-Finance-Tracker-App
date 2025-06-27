package com.example.financetracker

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.financetracker.databinding.ActivitySettingsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences("session", MODE_PRIVATE)

        // Highlight settings icon
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_settings

        // Display current user email
        val loggedInUser = sharedPref.getString("loggedInUser", null)
        val userEmail = sharedPref.getString("userEmail", "Not available")
        binding.tvUserEmail.text = userEmail

        binding.btnChangePassword.setOnClickListener {
            changePassword()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.btnResetApp.setOnClickListener {
            resetApp()
        }

        // Set up bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Navigate to Home (MainActivity)
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_add_transaction -> {
                    // Navigate to Add Transaction
                    startActivity(Intent(this, AddTransactionActivity::class.java))
                    true
                }
                R.id.nav_view_transactions -> {
                    // Navigate to View Transactions
                    startActivity(Intent(this, ViewTransactionsActivity::class.java))
                    true
                }
                R.id.nav_notifications -> {
                    // Navigate to Notifications
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    // Navigate to Settings
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Initialize dark theme switch
        val isDarkTheme = sharedPref.getBoolean("dark_theme", false)
        binding.switchDarkTheme.isChecked = isDarkTheme
        binding.switchDarkTheme.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                saveThemePreference(true)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                saveThemePreference(false)
            }
        }
    }

    private fun saveThemePreference(isDarkTheme: Boolean) {
        with(sharedPref.edit()) {
            putBoolean("dark_theme", isDarkTheme)
            apply()
        }
    }

    private fun changePassword() {
        val currentPassword = binding.etCurrentPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmNewPassword = binding.etConfirmNewPassword.text.toString().trim()

        if (newPassword != confirmNewPassword) {
            Toast.makeText(this, "New password and confirmation do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Placeholder for actual password change logic
        // Implement your password change logic here
        // Example validation: currentPassword.equals(storedPassword)
        val sharedPref = getSharedPreferences("user_data", MODE_PRIVATE)
        val storedPassword = sharedPref.getString("password", null)

        if (currentPassword == storedPassword) {
            with(sharedPref.edit()) {
                putString("password", newPassword)
                apply()
            }
            Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        with(sharedPref.edit()) {
            remove("loggedInUser")
            remove("userEmail")
            apply()
        }
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun resetApp() {
        // Clear all SharedPreferences
        val sessionPref = getSharedPreferences("session", MODE_PRIVATE)
        val userPref = getSharedPreferences("user_data", MODE_PRIVATE)
        val transactionPref = getSharedPreferences("transaction_data", MODE_PRIVATE)

        with(sessionPref.edit()) {
            clear()
            apply()
        }

        with(userPref.edit()) {
            clear()
            apply()
        }

        with(transactionPref.edit()) {
            clear()
            apply()
        }

        // Optionally, clear other app data if needed

        Toast.makeText(this, "App reset successfully", Toast.LENGTH_SHORT).show()

        // Restart the app or navigate to the initial screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}