package uz.coder.camunda.worker

import com.auth0.json.mgmt.client.Client
import uz.coder.camunda.repository.ProcessInstanceRepository
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.spring.client.annotation.JobWorker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * Camunda Workers handle service tasks in BPMN processes
 */
@Component
class ProductProcessWorker(
    private val processInstanceRepository: ProcessInstanceRepository,
    private val client: ZeebeClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Process validate product task
     */
    @JobWorker(type = "validate-product", autoComplete = false)
    fun validateProduct(job: ActivatedJob) {
        try {
            logger.info("Starting validate-product task for process: ${job.processInstanceKey}")
            logger.info("Job: ${job.variablesAsMap}")

            // Get variables from process
            val variables = job.variablesAsMap
            val productId = variables["productId"] as? Int
            val productName = variables["productName"] as? String

            // Business logic: validate product
            val isValid = productName?.isNotBlank() == true && productId != null && productId > 0

            logger.info("Product validation result: $isValid")

            // Complete job with variables
            client.newCompleteCommand(job.key)
                .variables(
                    mapOf(
                        "isValid" to isValid,
                        "validationTime" to System.currentTimeMillis()
                    )
                )
                .send()
                .join()

            logger.info("Completed validate-product task")

        } catch (e: Exception) {
            logger.error("Error in validate-product task", e)
            client.newFailCommand(job.key)
                .retries(job.retries - 1)
                .errorMessage(e.message)
                .send()
                .join()
        }
    }

    /**
     * Process create order task
     */
    @JobWorker(type = "create-order", autoComplete = false)
    fun createOrder(job: ActivatedJob) {
        try {
            logger.info("Starting create-order task for process: ${job.processInstanceKey}")

            val variables = job.variablesAsMap
            val productId = variables["productId"] as? Int
            val quantity = variables["quantity"] as? Int ?: 1

            // Business logic: create order
            val orderId = "ORD-${System.currentTimeMillis()}"
            val totalPrice = (variables["price"] as? Double ?: 0.0) * quantity

            logger.info("Order created: $orderId with quantity: $quantity")

            // Update process instance status
            val processInstance = processInstanceRepository
                .findByProcessInstanceId(job.processInstanceKey.toString())
            if (processInstance != null) {
                processInstance.status = "IN_PROGRESS"
                processInstance.updatedAt = LocalDateTime.now()
                processInstanceRepository.save(processInstance)
            }

            // Complete job
            client.newCompleteCommand(job.key)
                .variables(
                    mapOf(
                        "orderId" to orderId,
                        "totalPrice" to totalPrice,
                        "createdAt" to LocalDateTime.now().toString()
                    )
                )
                .send()
                .join()

            logger.info("Completed create-order task")

        } catch (e: Exception) {
            logger.error("Error in create-order task", e)
            client.newFailCommand(job.key)
                .retries(job.retries - 1)
                .errorMessage(e.message)
                .send()
                .join()
        }
    }

    /**
     * Process send notification task
     */
    @JobWorker(type = "send-notification", autoComplete = false)
    fun sendNotification(job: ActivatedJob) {
        try {
            logger.info("Starting send-notification task for process: ${job.processInstanceKey}")

            val variables = job.variablesAsMap
            val orderId = variables["orderId"] as? String
            val email = variables["email"] as? String
            val notificationType = variables["type"] as? String ?: "EMAIL"

            // Business logic: send notification
            logger.info("Sending $notificationType notification to $email for order: $orderId")

            // Simulate sending email/SMS
            Thread.sleep(500)

            // Complete job
            client.newCompleteCommand(job.key)
                .variables(
                    mapOf(
                        "notificationSent" to true,
                        "sentAt" to LocalDateTime.now().toString()
                    )
                )
                .send()
                .join()

            logger.info("Completed send-notification task")

        } catch (e: Exception) {
            logger.error("Error in send-notification task", e)
            client.newFailCommand(job.key)
                .retries(job.retries - 1)
                .errorMessage(e.message)
                .send()
                .join()
        }
    }

    /**
     * Process fulfill order task
     */
    @JobWorker(type = "fulfill-order", autoComplete = false)
    fun fulfillOrder(job: ActivatedJob) {
        try {
            logger.info("Starting fulfill-order task for process: ${job.processInstanceKey}")

            val variables = job.variablesAsMap
            val orderId = variables["orderId"] as? String

            // Business logic: fulfill order
            logger.info("Fulfilling order: $orderId")

            // Update process instance to completed
            val processInstance = processInstanceRepository
                .findByProcessInstanceId(job.processInstanceKey.toString())
            if (processInstance != null) {
                processInstance.status = "COMPLETED"
                processInstance.updatedAt = LocalDateTime.now()
                processInstanceRepository.save(processInstance)
            }

            // Complete job
            client.newCompleteCommand(job.key)
                .variables(
                    mapOf(
                        "fulfilled" to true,
                        "fulfilledAt" to LocalDateTime.now().toString()
                    )
                )
                .send()
                .join()

            logger.info("Completed fulfill-order task")

        } catch (e: Exception) {
            logger.error("Error in fulfill-order task", e)
            client.newFailCommand(job.key)
                .retries(job.retries - 1)
                .errorMessage(e.message)
                .send()
                .join()
        }
    }
}