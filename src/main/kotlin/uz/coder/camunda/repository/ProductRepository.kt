package uz.coder.camunda.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uz.coder.camunda.model.Product


@Repository
interface ProductRepository : JpaRepository<Product, Long> {

    fun findByCategory(category: String): List<Product>

    fun findByBrand(brand: String): List<Product>

    fun findByNameContainingIgnoreCase(name: String): List<Product>
}
