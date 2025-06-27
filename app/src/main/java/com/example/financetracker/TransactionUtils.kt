package com.example.financetracker

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

object TransactionUtils {

    fun getMonthlyCategoryData(context: Context, loggedInUser: String): Map<String, Map<String, Double>> {
        val gson = Gson()
        val sharedPref = context.getSharedPreferences("transaction_data_$loggedInUser", Context.MODE_PRIVATE)
        val json = sharedPref.getString("transactions", null)
        val typeToken = object : TypeToken<MutableList<Transaction>>() {}.type
        val transactions: MutableList<Transaction> = if (json != null) {
            gson.fromJson(json, typeToken)
        } else {
            mutableListOf()
        }

        val monthlyCategoryData = mutableMapOf<String, MutableMap<String, Double>>()

        transactions.forEach { transaction ->
            val calendar = Calendar.getInstance().apply { time = transaction.date }
            val monthYear = "${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
            val categoryData = monthlyCategoryData.getOrPut(monthYear) { mutableMapOf() }
            val currentSum = categoryData.getOrDefault(transaction.category, 0.0)
            categoryData[transaction.category] = currentSum + transaction.value
        }

        return monthlyCategoryData
    }
}