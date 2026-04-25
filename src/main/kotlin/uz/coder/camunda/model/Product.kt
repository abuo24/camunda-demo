package uz.coder.camunda.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "products", schema = "app")
data class Product(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 255)
    val name: String,

    @Column(nullable = false, length = 100)
    val category: String,

    @Column(nullable = false, length = 100)
    val brand: String,

    @Column(nullable = false, precision = 18, scale = 2)
    val price: BigDecimal,

//    @Column(name = "created_at", nullable = false)
//    val createdAt: LocalDateTime = LocalDateTime.now(),
//
//    @Column(name = "updated_at")
//    val updatedAt: LocalDateTime
)
 