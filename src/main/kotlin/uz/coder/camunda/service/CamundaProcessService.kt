package uz.coder.camunda.service

import uz.coder.camunda.model.ProcessInstance
import uz.coder.camunda.model.UserTask
import uz.coder.camunda.repository.ProcessInstanceRepository
import uz.coder.camunda.repository.UserTaskRepository
import io.camunda.zeebe.client.ZeebeClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Collections.emptyMap

/**
 * Service to manage Camunda process instances and tasks
 */
@Service
@Transactional
class CamundaProcessService(
    private val zeebeClient: ZeebeClient,
    private val processInstanceRepository: ProcessInstanceRepository,
    private val userTaskRepository: UserTaskRepository
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Start a new process instance
     */
    fun startProcess(
        processDefinitionKey: String,
        businessKey: String? = null,
        variables: Map<String, Any> = emptyMap()
    ): ProcessInstance {
        try {
            logger.info("Starting process: $processDefinitionKey with business key: $businessKey")

            // Start process in Camunda
            val response = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(processDefinitionKey)
                .latestVersion()
                .variables(variables)
                .apply {
                    if (!businessKey.isNullOrBlank()) {
//                        this.businessKey(businessKey)
                    }
                }
                .send()
                .join()

            // Save to database
            val processInstance = ProcessInstance(
                processDefinitionKey = processDefinitionKey,
                processInstanceId = response.processInstanceKey.toString(),
                businessKey = businessKey,
                status = "ACTIVE",
                productId = (variables["productId"] as? Number)?.toLong()
            )

            val saved = processInstanceRepository.save(processInstance)
            logger.info("Process started: ${saved.processInstanceId}")

            return saved

        } catch (e: Exception) {
            logger.error("Failed to start process: $processDefinitionKey", e)
            throw RuntimeException("Failed to start process: ${e.message}")
        }
    }

    /**
     * Get process instance details
     */
    fun getProcessInstance(processInstanceId: String): ProcessInstance? {
        return processInstanceRepository.findByProcessInstanceId(processInstanceId)
    }

    /**
     * Get all active processes for a process definition
     */
    fun getActiveProcesses(processDefinitionKey: String): List<ProcessInstance> {
        return processInstanceRepository
            .findByProcessDefinitionKey(processDefinitionKey)
            .filter { it.status == "ACTIVE" }
    }

    /**
     * Cancel a process instance
     */
    fun cancelProcess(processInstanceId: String): Boolean {
        return try {
            logger.info("Cancelling process: $processInstanceId")

            zeebeClient.newCancelInstanceCommand(processInstanceId.toLong())
                .send()
                .join()

            // Update status in database
            val processInstance = processInstanceRepository.findByProcessInstanceId(processInstanceId)
            if (processInstance != null) {
                processInstance.status = "CANCELLED"
                processInstance.updatedAt = LocalDateTime.now()
                processInstanceRepository.save(processInstance)
            }

            logger.info("Process cancelled: $processInstanceId")
            true

        } catch (e: Exception) {
            logger.error("Failed to cancel process: $processInstanceId", e)
            false
        }
    }

    /**
     * Get user tasks for an assignee
     */
    fun getUserTasks(assignee: String): List<UserTask> {
        return userTaskRepository.findByStatusAndAssignee("PENDING", assignee)
    }

    /**
     * Complete a user task
     */
    fun completeUserTask(taskId: String, variables: Map<String, Any> = emptyMap()): Boolean {
        return try {
            logger.info("Completing user task: $taskId")

            // Update task in database
            val userTask = userTaskRepository.findByTaskId(taskId)
            if (userTask != null) {
                userTask.status = "COMPLETED"
                userTask.completedAt = LocalDateTime.now()
                userTaskRepository.save(userTask)
            }

            logger.info("User task completed: $taskId")
            true

        } catch (e: Exception) {
            logger.error("Failed to complete user task: $taskId", e)
            false
        }
    }

    /**
     * Get process instance statistics
     */
    fun getProcessStatistics(): Map<String, Any> {
        val allProcesses = processInstanceRepository.findAll()
        val activeCount = allProcesses.count { it.status == "ACTIVE" }
        val completedCount = allProcesses.count { it.status == "COMPLETED" }
        val cancelledCount = allProcesses.count { it.status == "CANCELLED" }

        val allTasks = userTaskRepository.findAll()
        val pendingTasks = allTasks.count { it.status == "PENDING" }
        val completedTasks = allTasks.count { it.status == "COMPLETED" }

        return mapOf(
            "totalProcesses" to allProcesses.size,
            "activeProcesses" to activeCount,
            "completedProcesses" to completedCount,
            "cancelledProcesses" to cancelledCount,
            "totalTasks" to allTasks.size,
            "pendingTasks" to pendingTasks,
            "completedTasks" to completedTasks
        )
    }
}