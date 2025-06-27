package com.example.financetracker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financetracker.databinding.ActivityViewTransactionsBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ViewTransactionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewTransactionsBinding
    private val transactionsAdapter = TransactionsAdapter(
        onDeleteClick = { transaction -> showDeleteConfirmationDialog(transaction) }
    )
    private var selectedMonth: Int = -1
    private var selectedYear: Int = -1
    private lateinit var transactionService: TransactionService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionService = TransactionService(this)
        setupViews()
        setupListeners()
        loadTransactions()
    }

    private fun setupViews() {
        // Setup RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ViewTransactionsActivity)
            adapter = transactionsAdapter
        }

        // Setup Bottom Navigation
        binding.bottomNavigation.selectedItemId = R.id.nav_view_transactions

        // Initialize date text
        updateSelectedDateText()
    }

    private fun setupListeners() {
        // Filter buttons
        binding.btnFilter.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnApplyFilter.setOnClickListener {
            if (selectedMonth != -1 && selectedYear != -1) {
                filterTransactions(selectedMonth, selectedYear)
            } else {
                Toast.makeText(this, getString(R.string.select_date_first), Toast.LENGTH_SHORT).show()
            }
        }

        // Options menu
        binding.btnMore.setOnClickListener { view ->
            showOptionsMenu(view)
        }

        // Bottom navigation
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> navigateTo(MainActivity::class.java)
                R.id.nav_add_transaction -> navigateTo(AddTransactionActivity::class.java)
                R.id.nav_view_transactions -> true // Current screen
                R.id.nav_notifications -> navigateTo(NotificationsActivity::class.java)
                R.id.nav_settings -> navigateTo(SettingsActivity::class.java)
                else -> false
            }
        }
    }

    private fun showOptionsMenu(view: View) {
        PopupMenu(this, view).apply {
            menuInflater.inflate(R.menu.transaction_options_menu, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_export_json -> {
                        exportTransactionsAsJson()
                        true
                    }
                    R.id.menu_export_text -> {
                        exportTransactionsAsText()
                        true
                    }
                    R.id.menu_restore -> {
                        openFilePicker()
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, _ ->
                selectedYear = year
                selectedMonth = month
                updateSelectedDateText()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            1
        ).show()
    }

    private fun updateSelectedDateText() {
        if (selectedMonth != -1 && selectedYear != -1) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth)
            }
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            binding.tvSelectedDate.text = getString(R.string.selected_date_format, dateFormat.format(calendar.time))
        } else {
            binding.tvSelectedDate.text = getString(R.string.no_date_selected)
        }
    }

    private fun loadTransactions() {
        getLoggedInUser()?.let { user ->
            val transactions = transactionService.getTransactions(user)
            updateTransactionsList(transactions)
        }
    }

    private fun filterTransactions(month: Int, year: Int) {
        getLoggedInUser()?.let { user ->
            val transactions = transactionService.getTransactions(user)
            val filteredTransactions = transactions.filter {
                val calendar = Calendar.getInstance().apply { time = it.date }
                calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year
            }
            updateTransactionsList(filteredTransactions)
        }
    }

    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_transaction))
            .setMessage(getString(R.string.delete_transaction_confirmation))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteTransaction(transaction)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun deleteTransaction(transaction: Transaction) {
        getLoggedInUser()?.let { user ->
            transactionService.deleteTransaction(user, transaction)
            loadTransactions()
            Toast.makeText(this, getString(R.string.transaction_deleted), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTransactionsList(transactions: List<Transaction>) {
        transactionsAdapter.submitList(transactions)
        updateEmptyState(transactions.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun exportTransactionsAsJson() {
        getLoggedInUser()?.let { user ->
            try {
                transactionService.exportTransactionsAsJson(user)
                shareFile(File(filesDir, "transactions_backup.json"))
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.export_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportTransactionsAsText() {
        getLoggedInUser()?.let { user ->
            try {
                transactionService.exportTransactionsAsText(user)
                shareFile(File(filesDir, "transactions_backup.txt"))
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.export_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                file
            )
            Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                startActivity(Intent.createChooser(this, getString(R.string.share_file)))
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.share_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.select_backup_file)),
            REQUEST_CODE_RESTORE
        )
    }

    private fun getLoggedInUser(): String? {
        return getSharedPreferences("session", MODE_PRIVATE)
            .getString("loggedInUser", null)
            ?.also { user ->
                if (user.isEmpty()) {
                    Toast.makeText(this, getString(R.string.user_not_logged_in), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateTo(activityClass: Class<*>): Boolean {
        startActivity(Intent(this, activityClass))
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_RESTORE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val file = File(filesDir, "temp_backup.json")
                    contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    getLoggedInUser()?.let { user ->
                        transactionService.restoreTransactionsFromJson(user, file)
                        loadTransactions()
                        Toast.makeText(this, getString(R.string.backup_restored), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        getString(R.string.error_restoring_backup, e.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_RESTORE = 2
    }
}