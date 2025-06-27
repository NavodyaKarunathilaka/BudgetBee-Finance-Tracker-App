package com.example.financetracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionsAdapter(
    private val onDeleteClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionsAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        fun bind(transaction: Transaction) {
            binding.apply {
                // Set transaction icon based on type
                ivType.setImageResource(
                    when (transaction.type.lowercase()) {
                        "expense" -> R.drawable.ic_expense
                        "income" -> R.drawable.ic_income
                        else -> R.drawable.ic_transfer
                    }
                )

                // Set transaction details
                tvDetail.text = transaction.detail
                tvCategory.text = transaction.category
                tvDate.text = dateFormatter.format(transaction.date)

                // Format and set the value with color (without currency symbol)
                tvValue.text = String.format("%.2f", transaction.value)
                tvValue.setTextColor(
                    when (transaction.type.lowercase()) {
                        "expense" -> Color.RED
                        "income" -> Color.GREEN
                        else -> Color.GRAY
                    }
                )

                // Set delete button click listener
                btnDelete.setOnClickListener {
                    showDeleteConfirmationDialog(transaction)
                }
            }
        }

        private fun showDeleteConfirmationDialog(transaction: Transaction) {
            AlertDialog.Builder(itemView.context)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete") { _, _ ->
                    onDeleteClick(transaction)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            // Compare all fields since we don't have a unique ID
            return oldItem.getCompositeKey() == newItem.getCompositeKey()
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem  // Data class equals() comparison
        }
    }
}