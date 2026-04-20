package com.coder.camunda.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "user_tasks", schema = "app")
data class UserTask(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "task_id", nullable = false, unique = true)
    val taskId: String,

    @Column(name = "process_instance_id", nullable = false)
    val processInstanceId: String,

    @Column(name = "task_key", nullable = false)
    val taskKey: String,

    @Column(name = "assignee")
    var assignee: String? = null,

    @Column(nullable = false)
    var status: String = "PENDING",

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null
)
 