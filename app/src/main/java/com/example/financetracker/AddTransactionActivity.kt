package com.example.financetracker

import android.Manifest
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.financetracker.databinding.ActivityAddTransactionBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()

        // Highlight add transaction icon
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_add_transaction

        binding.btnSelectDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnConfirm.setOnClickListener {
            handleTransaction()
        }

        // Add test notification button
        binding.btnTestNotification.setOnClickListener {
            sendNotification("Test notification")
        }

        binding.rgTransactionType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbDeposit -> showCategories(getDepositCategories())
                R.id.rbWithdraw -> showCategories(getWithdrawCategories())
            }
        }

        // Request notification permission on Android 13 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_NOTIFY_PERMISSION)
            }
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
    }

    private fun showDatePickerDialog() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate.set(selectedYear, selectedMonth, selectedDay)
            binding.tvSelectedDate.text = "${selectedMonth + 1}/$selectedDay/$selectedYear"
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun handleTransaction() {
        val detail = binding.etTransactionDetail.text.toString().trim()
        val value = binding.etTransactionValue.text.toString().toDoubleOrNull()
        val type = when (binding.rgTransactionType.checkedRadioButtonId) {
            R.id.rbDeposit -> "deposit"
            R.id.rbWithdraw -> "withdraw"
            else -> null
        }
        val category = binding.spTransactionCategory.selectedItem?.toString()

        if (detail.isNotEmpty() && value != null && type != null && category != null) {
            saveTransactionData(detail, value, type, category)
        } else {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveTransactionData(detail: String, value: Double, type: String, category: String) {
        val sessionPref = getSharedPreferences("session", MODE_PRIVATE)
        val loggedInUser = sessionPref.getString("loggedInUser", null)

        if (loggedInUser != null) {
            val transaction = Transaction(
                type = type,
                detail = detail,
                value = value,
                date = selectedDate.time,
                category = category,
                userId = loggedInUser
            )
            val gson = Gson()
            val sharedPref = getSharedPreferences("transaction_data_$loggedInUser", MODE_PRIVATE)
            val json = sharedPref.getString("transactions", null)
            val typeToken = object : TypeToken<MutableList<Transaction>>() {}.type
            val transactions: MutableList<Transaction> = if (json != null) {
                gson.fromJson(json, typeToken)
            } else {
                mutableListOf()
            }

            transactions.add(transaction)
            with(sharedPref.edit()) {
                putString("transactions", gson.toJson(transactions))
                apply()
            }

            // Update budget
            val budgetPref = getSharedPreferences("budget_data_$loggedInUser", MODE_PRIVATE)
            val currentBudget = budgetPref.getFloat("budget", 0f)
            val updatedBudget = if (type == "deposit") currentBudget + value.toFloat() else currentBudget - value.toFloat()
            with(budgetPref.edit()) {
                putFloat("budget", updatedBudget)
                apply()
            }

            // Check budget thresholds and send notifications
            checkBudgetThresholds(updatedBudget)

            Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkBudgetThresholds(updatedBudget: Float) {
        val thresholds = listOf(0.75f, 0.90f, 1.00f)
        val thresholdMessages = listOf(
            "Transaction added successfully",
            "You have reached 90% of your budget.",
            "You have reached 100% of your budget."
        )

        for ((index, threshold) in thresholds.withIndex()) {
            if (updatedBudget >= threshold) {
                sendNotification(thresholdMessages[index])
                break
            }
        }
    }

    private fun sendNotification(message: String) {
        val notificationId = 1
        val builder = NotificationCompat.Builder(this, "budget_warning_channel")
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("Budget Warning")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Check permission before sending notification
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            with(NotificationManagerCompat.from(this)) {
                notify(notificationId, builder.build())
            }
            // Save notification to shared preferences
            saveNotification(message)
        } else {
            // Optionally, request permission or show a message
            Toast.makeText(this, "Notification permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveNotification(message: String) {
        val sharedPref = getSharedPreferences("notifications_data", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPref.getString("notifications", null)
        val typeToken = object : TypeToken<MutableList<String>>() {}.type
        val notifications: MutableList<String> = if (json != null) {
            gson.fromJson(json, typeToken)
        } else {
            mutableListOf()
        }

        notifications.add(message)
        with(sharedPref.edit()) {
            putString("notifications", gson.toJson(notifications))
            apply()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Budget Warning Channel"
            val descriptionText = "Channel for budget warnings"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("budget_warning_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFY_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, proceed with notifications
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCategories(categories: List<String>) {
        binding.spTransactionCategory.visibility = View.VISIBLE
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spTransactionCategory.adapter = adapter
    }

    private fun getDepositCategories(): List<String> {
        return listOf(
            "Salary",
            "Freelance",
            "Business",
            "Investments",
            "Gifts",
            "Rental Income",
            "Other"
        )
    }

    private fun getWithdrawCategories(): List<String> {
        return listOf(
            "Rent/Mortgage",
            "Utilities",
            "Groceries",
            "Transportation",
            "Insurance",
            "Loan Payments",
            "Other"
        )
    }

    companion object {
        private const val REQUEST_CODE_NOTIFY_PERMISSION = 1
    }
}