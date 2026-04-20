package com.coder.camunda.dto

data class OrderProcessRequest(
    val productId: Long,
    val quantity: Int = 1,
    val customerId: String,
    val email: String
)
