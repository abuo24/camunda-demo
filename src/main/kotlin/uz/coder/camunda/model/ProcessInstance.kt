package com.coder.camunda.model
import java.time.LocalDateTime
import jakarta.persistence.*
 
@Entity
@Table(name = "process_instances", schema = "app")
data class ProcessInstance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
 
    @Column(name = "process_definition_key", nullable = false)
    val processDefinitionKey: String,
 
    @Column(name = "process_instance_id", nullable = false, unique = true)
    val processInstanceId: String,
 
    @Column(name = "business_key")
    val businessKey: String? = null,
 
    @Column(nullable = false)
    var status: String = "ACTIVE",
 
    @Column(name = "product_id")
    val productId: Long? = null,
 
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
 
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
 