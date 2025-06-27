package com.example.financetracker

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter

class TransactionService(private val context: Context) {

    private val gson = Gson()

    fun getTransactions(loggedInUser: String): List<Transaction> {
        val sharedPref = context.getSharedPreferences("transaction_data_$loggedInUser", Context.MODE_PRIVATE)
        val json = sharedPref.getString("transactions", null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<List<Transaction>>() {}.type)
        } else {
            emptyList()
        }
    }

    fun deleteTransaction(loggedInUser: String, transaction: Transaction) {
        val sharedPref = context.getSharedPreferences("transaction_data_$loggedInUser", Context.MODE_PRIVATE)
        val json = sharedPref.getString("transactions", null)
        val transactions: MutableList<Transaction> = if (json != null) {
            gson.fromJson(json, object : TypeToken<MutableList<Transaction>>() {}.type)
        } else {
            mutableListOf()
        }

        transactions.remove(transaction)

        with(sharedPref.edit()) {
            putString("transactions", gson.toJson(transactions))
            apply()
        }
    }

    fun exportTransactionsAsJson(loggedInUser: String): String {
        val sharedPref = context.getSharedPreferences("transaction_data_$loggedInUser", Context.MODE_PRIVATE)
        val json = sharedPref.getString("transactions", null)
        val transactions = if (json != null) {
            gson.fromJson<MutableList<Transaction>>(json, object : TypeToken<MutableList<Transaction>>() {}.type)
        } else {
            mutableListOf()
        }
        val exportJson = gson.toJson(transactions)
        saveToFile("transactions_backup.json", exportJson)
        return exportJson
    }

    fun exportTransactionsAsText(loggedInUser: String): String {
        val transactions = getTransactions(loggedInUser)
        val exportText = buildString {
            appendLine("Transactions Export")
            appendLine("Date: ${java.time.LocalDateTime.now()}")
            appendLine("User: $loggedInUser")
            appendLine("-".repeat(50))

            transactions.forEach { transaction ->
                appendLine("Type: ${transaction.type}")
                appendLine("Detail: ${transaction.detail}")
                appendLine("Category: ${transaction.category}")
                appendLine("Value: ${transaction.value}")
                appendLine("Date: ${transaction.date}")
                appendLine("-".repeat(50))
            }
        }
        saveToFile("transactions_backup.txt", exportText)
        return exportText
    }

    fun restoreTransactionsFromJson(loggedInUser: String, file: File) {
        try {
            FileReader(file).use { reader ->
                val transactions: List<Transaction> = gson.fromJson(reader, object : TypeToken<List<Transaction>>() {}.type)
                val sharedPref = context.getSharedPreferences("transaction_data_$loggedInUser", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("transactions", gson.toJson(transactions))
                    apply()
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to restore transactions: ${e.message}")
        }
    }

    private fun saveToFile(fileName: String, content: String) {
        try {
            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { fos ->
                fos.write(content.toByteArray())
            }
        } catch (e: Exception) {
            throw Exception("Failed to save file: ${e.message}")
        }
    }

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
    }
}