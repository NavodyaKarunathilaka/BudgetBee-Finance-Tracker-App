package com.example.financetracker

import com.example.financetracker.data.TransactionEntity
import java.util.Date

data class Transaction(
    val id: Long = 0,
    val type: String,
    val detail: String,
    val value: Double,
    val date: Date,
    val category: String,
    val userId: String
) {
    fun getCompositeKey(): String {
        return "$type$detail$value${date.time}$category"
    }

    fun toEntity(): TransactionEntity {
        return TransactionEntity(
            id = id,
            amount = value,
            description = detail,
            category = category,
            type = type,
            date = date,
            userId = userId
        )
    }

    companion object {
        fun fromEntity(entity: TransactionEntity): Transaction {
            return Transaction(
                id = entity.id,
                type = entity.type,
                detail = entity.description,
                value = entity.amount,
                date = entity.date,
                category = entity.category,
                userId = entity.userId
            )
        }
    }
}