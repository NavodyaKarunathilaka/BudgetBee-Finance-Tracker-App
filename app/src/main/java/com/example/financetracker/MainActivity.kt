package com.example.financetracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.financetracker.databinding.ActivityMainBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Highlight home icon
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_home

        // Check if user is logged in
        val sharedPref = getSharedPreferences("session", MODE_PRIVATE)
        val loggedInUser = sharedPref.getString("loggedInUser", null)
        if (loggedInUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Display budget for the logged-in user
        displayUserBudget()

        binding.btnAddBudget.setOnClickListener {
            val intent = Intent(this, BudgetActivity::class.java)
            startActivity(intent)
        }

        binding.btnAddTransaction.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_TRANSACTION)
        }

        binding.btnViewTransactions.setOnClickListener {
            val intent = Intent(this, ViewTransactionsActivity::class.java)
            startActivity(intent)
        }

        binding.btnNotifications.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }

        binding.btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Generate pie charts for transactions
        if (loggedInUser != null) {
            generatePieCharts(loggedInUser)
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

        // Set up refresh listener
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshContent()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_TRANSACTION) {
            // Refresh budget display
            displayUserBudget()
            // Refresh pie charts
            val sharedPref = getSharedPreferences("session", MODE_PRIVATE)
            val loggedInUser = sharedPref.getString("loggedInUser", null)
            if (loggedInUser != null) {
                generatePieCharts(loggedInUser)
            }
        }
    }

    private fun displayUserBudget() {
        val sharedPref = getSharedPreferences("session", MODE_PRIVATE)
        val loggedInUser = sharedPref.getString("loggedInUser", null)

        if (loggedInUser != null) {
            val budgetPref = getSharedPreferences("budget_data_$loggedInUser", MODE_PRIVATE)
            val budget = budgetPref.getFloat("budget", 0f)
            val month = budgetPref.getInt("month", -1)
            val year = budgetPref.getInt("year", -1)

            if (month != -1 && year != -1) {
                val currentCalendar = Calendar.getInstance()
                val currentMonth = currentCalendar.get(Calendar.MONTH)
                val currentYear = currentCalendar.get(Calendar.YEAR)

                if (month == currentMonth && year == currentYear) {
                    binding.tvBudget.text = "Budget for ${month + 1}/$year: $budget"
                } else {
                    binding.tvBudget.text = "No budget set for current month"
                }
            } else {
                binding.tvBudget.text = "No budget set"
            }
        }
    }

    private fun generatePieCharts(loggedInUser: String) {
        val monthlyCategoryData = TransactionUtils.getMonthlyCategoryData(this, loggedInUser)
        val currentMonthYear = getCurrentMonthYear()

        val depositData = monthlyCategoryData[currentMonthYear]?.filterKeys { isDepositCategory(it) } ?: emptyMap()
        val withdrawData = monthlyCategoryData[currentMonthYear]?.filterKeys { isWithdrawCategory(it) } ?: emptyMap()

        setupPieChart(binding.pieChartDeposits, "Incomes", depositData)
        setupPieChart(binding.pieChartWithdraws, "Expenses", withdrawData)
    }

    private fun getCurrentMonthYear(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
    }

    private fun isDepositCategory(category: String): Boolean {
        return listOf("Salary", "Freelance", "Business", "Investments", "Gifts", "Rental Income", "Other").contains(category)
    }

    private fun isWithdrawCategory(category: String): Boolean {
        return listOf("Rent/Mortgage", "Utilities", "Groceries", "Transportation", "Insurance", "Loan Payments", "Other").contains(category)
    }

    private fun setupPieChart(pieChart: PieChart, title: String, data: Map<String, Double>) {
        val entries = data.map { PieEntry(it.value.toFloat(), it.key) }
        val dataSet = PieDataSet(entries, title).apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextColor = Color.BLACK
            valueTextSize = 16f
        }
        val pieData = PieData(dataSet)

        pieChart.apply {
            this.data = pieData
            description.isEnabled = false
            isRotationEnabled = false
            setUsePercentValues(true)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            centerText = title
            setCenterTextSize(18f)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            setHoleColor(Color.TRANSPARENT)
            animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
            invalidate()
        }
    }

    private fun logout() {
        val sharedPref = getSharedPreferences("session", MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("loggedInUser")
            apply()
        }
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun refreshContent() {
        // Refresh budget display
        displayUserBudget()
        // Refresh pie charts
        val sharedPref = getSharedPreferences("session", MODE_PRIVATE)
        val loggedInUser = sharedPref.getString("loggedInUser", null)
        if (loggedInUser != null) {
            generatePieCharts(loggedInUser)
        }
        // Stop the refreshing animation
        binding.swipeRefreshLayout.isRefreshing = false
    }


    companion object {
        private const val REQUEST_CODE_ADD_TRANSACTION = 1
    }
}