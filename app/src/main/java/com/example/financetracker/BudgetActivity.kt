package com.example.financetracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.financetracker.databinding.ActivityBudgetBinding
import java.util.*

class BudgetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetBinding
    private var selectedMonth: Int = -1
    private var selectedYear: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get current month and year
        val calendar = Calendar.getInstance()
        selectedYear = calendar.get(Calendar.YEAR)
        selectedMonth = calendar.get(Calendar.MONTH)
        binding.tvSelectedDate.text = "${selectedMonth + 1}/$selectedYear"

        binding.btnAddBudget.setOnClickListener {
            val budget = binding.etBudget.text.toString().toDoubleOrNull()
            if (budget != null) {
                saveBudgetData(budget, selectedMonth, selectedYear)
            } else {
                Toast.makeText(
                    this,
                    "Please enter a valid budget",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnResetBudget.setOnClickListener {
            resetBudgetData()
        }
    }

    private fun saveBudgetData(budget: Double, month: Int, year: Int) {
        val sessionPref = getSharedPreferences("session", MODE_PRIVATE)
        val loggedInUser = sessionPref.getString("loggedInUser", null)

        if (loggedInUser != null) {
            val budgetPref = getSharedPreferences("budget_data_$loggedInUser", MODE_PRIVATE)
            with(budgetPref.edit()) {
                putFloat("budget", budget.toFloat())
                putInt("month", month)
                putInt("year", year)
                apply()
            }

            // Show success message and finish the activity
            Toast.makeText(this, "Budget saved: $budget for ${month + 1}/$year", Toast.LENGTH_SHORT)
                .show()
            finish()
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetBudgetData() {
        val sessionPref = getSharedPreferences("session", MODE_PRIVATE)
        val loggedInUser = sessionPref.getString("loggedInUser", null)

        if (loggedInUser != null) {
            val budgetPref = getSharedPreferences("budget_data_$loggedInUser", MODE_PRIVATE)
            with(budgetPref.edit()) {
                remove("budget")
                remove("month")
                remove("year")
                apply()
            }

            // Show success message and finish the activity
            Toast.makeText(this, "Budget reset successfully", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}