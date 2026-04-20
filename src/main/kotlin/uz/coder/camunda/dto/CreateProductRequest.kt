package com.coder.camunda.dto

import java.math.BigDecimal

data class CreateProductRequest(
    val name: String,
    val category: String,
    val brand: String,
    val price: BigDecimal
)