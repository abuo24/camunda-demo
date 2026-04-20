package com.coder.camunda.repository

import com.coder.camunda.model.ProcessInstance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProcessInstanceRepository : JpaRepository<ProcessInstance, Long> {
    fun findByProcessInstanceId(processInstanceId: String): ProcessInstance?
    fun findByProcessDefinitionKey(processDefinitionKey: String): List<ProcessInstance>
    fun findByStatus(status: String): List<ProcessInstance>
    fun findByBusinessKey(businessKey: String): ProcessInstance?
}
 