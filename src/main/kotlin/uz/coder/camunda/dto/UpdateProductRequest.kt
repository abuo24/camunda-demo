package com.coder.camunda.dto

import java.math.BigDecimal

data class UpdateProductRequest(
    val name: String? = null,
    val category: String? = null,
    val brand: String? = null,
    val price: BigDecimal? = null
)