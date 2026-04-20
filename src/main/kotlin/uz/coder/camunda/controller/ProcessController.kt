package com.coder.camunda.controller

import com.coder.camunda.dto.CreateProductRequest
import com.coder.camunda.dto.OrderProcessRequest
import com.coder.camunda.dto.UpdateProductRequest
import com.coder.camunda.model.Product
import com.coder.camunda.repository.ProductRepository
import com.coder.camunda.service.CamundaProcessService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = ["*"])
class ProcessController(
    private val processService: CamundaProcessService,
    private val productRepository: ProductRepository
) {

    // ==================== PROCESS ENDPOINTS ====================

    /**
     * POST /api/v1/processes/order
     * Start an order processing workflow
     */
    @PostMapping("/processes/order")
    fun startOrderProcess(@RequestBody request: OrderProcessRequest): ResponseEntity<Map<String, Any>> {
        return try {
            val product = productRepository.findById(request.productId)
                .orElseThrow { throw IllegalArgumentException("Product not found") }

            val variables = mapOf(
                "productId" to request.productId,
                "productName" to product.name,
                "price" to product.price,
                "quantity" to request.quantity,
                "email" to request.email,
                "customerId" to request.customerId
            )

            val processInstance = processService.startProcess(
                processDefinitionKey = "order-process",
                businessKey = "order-${System.currentTimeMillis()}",
                variables = variables
            )

            ResponseEntity.status(HttpStatus.CREATED).body(
                mapOf(
                    "processInstanceId" to processInstance.processInstanceId,
                    "status" to processInstance.status,
                    "createdAt" to processInstance.createdAt
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to e.message))
        }
    }

    /**
     * GET /api/v1/processes/{processInstanceId}
     * Get process instance details
     */
    @GetMapping("/processes/{processInstanceId}")
    fun getProcessInstance(@PathVariable processInstanceId: String): ResponseEntity<Map<String, Any>> {
        val process = processService.getProcessInstance(processInstanceId)
        return if (process != null) {
            ResponseEntity.ok(
                mapOf(
                    "processInstanceId" to process.processInstanceId,
                    "processDefinitionKey" to process.processDefinitionKey,
                    "businessKey" to process.businessKey,
                    "status" to process.status,
                    "createdAt" to process.createdAt,
                    "updatedAt" to process.updatedAt
                )
            )
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * GET /api/v1/processes
     * Get active processes
     */
    @GetMapping("/processes")
    fun getActiveProcesses(
        @RequestParam(required = false) processDefinitionKey: String?
    ): ResponseEntity<List<Map<String, Any>>> {
        val processes = if (!processDefinitionKey.isNullOrBlank()) {
            processService.getActiveProcesses(processDefinitionKey)
        } else {
            processService.getActiveProcesses("order-process")
        }

        val result = processes.map {
            mapOf(
                "processInstanceId" to it.processInstanceId,
                "processDefinitionKey" to it.processDefinitionKey,
                "status" to it.status,
                "businessKey" to it.businessKey,
                "createdAt" to it.createdAt
            )
        }

        return ResponseEntity.ok(result)
    }

    /**
     * DELETE /api/v1/processes/{processInstanceId}
     * Cancel a process instance
     */
    @DeleteMapping("/processes/{processInstanceId}")
    fun cancelProcess(@PathVariable processInstanceId: String): ResponseEntity<Map<String, String>> {
        val success = processService.cancelProcess(processInstanceId)
        return if (success) {
            ResponseEntity.ok(mapOf("message" to "Process cancelled successfully"))
        } else {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to cancel process"))
        }
    }

    /**
     * GET /api/v1/processes/stats
     * Get process statistics
     */
    @GetMapping("/processes/stats")
    fun getProcessStatistics(): ResponseEntity<Map<String, Any>> {
        val stats = processService.getProcessStatistics()
        return ResponseEntity.ok(stats)
    }

    // ==================== PRODUCT ENDPOINTS ====================

    /**
     * POST /api/v1/products
     * Create a new product
     */
    @PostMapping("/products")
    fun createProduct(@RequestBody request: CreateProductRequest): ResponseEntity<Product> {
        return try {
            val product = Product(
                name = request.name,
                category = request.category,
                brand = request.brand,
                price = request.price
            )
            val saved = productRepository.save(product)
            ResponseEntity.status(HttpStatus.CREATED).body(saved)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    /**
     * GET /api/v1/products
     * Get all products
     */
    @GetMapping("/products")
    fun getAllProducts(): ResponseEntity<List<Product>> {
        val products = productRepository.findAll()
        return ResponseEntity.ok(products)
    }

    /**
     * GET /api/v1/products/{id}
     * Get product by ID
     */
    @GetMapping("/products/{id}")
    fun getProduct(@PathVariable id: Long): ResponseEntity<Product> {
        return productRepository.findById(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    /**
     * GET /api/v1/products/category/{category}
     * Get products by category
     */
    @GetMapping("/products/category/{category}")
    fun getProductsByCategory(@PathVariable category: String): ResponseEntity<List<Product>> {
        val products = productRepository.findByCategory(category)
        return ResponseEntity.ok(products)
    }

    /**
     * GET /api/v1/products/brand/{brand}
     * Get products by brand
     */
    @GetMapping("/products/brand/{brand}")
    fun getProductsByBrand(@PathVariable brand: String): ResponseEntity<List<Product>> {
        val products = productRepository.findByBrand(brand)
        return ResponseEntity.ok(products)
    }

    /**
     * PUT /api/v1/products/{id}
     * Update product
     */
    @PutMapping("/products/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @RequestBody request: UpdateProductRequest
    ): ResponseEntity<Product> {
        return productRepository.findById(id)
            .map { product ->
                val updated = product.copy(
                    name = request.name ?: product.name,
                    category = request.category ?: product.category,
                    brand = request.brand ?: product.brand,
                    price = request.price ?: product.price
                )
                ResponseEntity.ok(productRepository.save(updated))
            }
            .orElse(ResponseEntity.notFound().build())
    }

    /**
     * DELETE /api/v1/products/{id}
     * Delete product
     */
    @DeleteMapping("/products/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Unit> {
        return if (productRepository.existsById(id)) {
            productRepository.deleteById(id)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * GET /api/v1/health
     * Health check endpoint
     */
    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("status" to "UP"))
    }
}

