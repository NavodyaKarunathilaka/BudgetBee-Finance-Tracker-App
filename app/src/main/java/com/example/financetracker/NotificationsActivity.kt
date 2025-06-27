package com.example.financetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financetracker.databinding.ActivityNotificationsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private val notificationsAdapter = NotificationsAdapter()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Highlight notifications icon
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_notifications

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = notificationsAdapter

        binding.btnClearAll.setOnClickListener {
            clearAllNotifications()
        }

        loadNotifications()

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
    }

    private fun loadNotifications() {
        val sharedPref = getSharedPreferences("notifications_data", MODE_PRIVATE)
        val json = sharedPref.getString("notifications", null)
        val typeToken = object : TypeToken<MutableList<String>>() {}.type
        val notifications: MutableList<String> = if (json != null) {
            gson.fromJson(json, typeToken)
        } else {
            mutableListOf()
        }

        notificationsAdapter.submitList(notifications)
    }

    private fun clearAllNotifications() {
        val sharedPref = getSharedPreferences("notifications_data", MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("notifications")
            apply()
        }
        notificationsAdapter.submitList(emptyList())
        Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show()
    }
}