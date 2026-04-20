package com.coder.camunda.repository

import io.camunda.common.auth.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findByCategory(category: String): List<Product>
    fun findByBrand(brand: String): List<Product>
    fun findByNameContainingIgnoreCase(name: String): List<Product>
}
