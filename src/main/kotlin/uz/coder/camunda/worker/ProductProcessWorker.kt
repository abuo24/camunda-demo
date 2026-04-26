package uz.coder.camunda.worker

import com.auth0.json.mgmt.client.Client
import com.fasterxml.jackson.databind.ObjectMapper
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
    private val client: ZeebeClient,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Deserializes directly to data class instead of casting
     */
    private inline fun <reified T> getVariablesAs(job: ActivatedJob): T {
        return objectMapper.convertValue(job.variablesAsMap, T::class.java)
    }

    /**
     * Validate product task
     */
    @JobWorker(type = "validate-product", autoComplete = false)
    fun validateProduct(job: ActivatedJob) {
        try {
            logger.info("Starting validate-product task for process: ${job.processInstanceKey}")

            val input = getVariablesAs<ValidateProductInput>(job)

            val isValid = input.productName?.isNotBlank() == true && input.productId > 0

            logger.info("Product validation result: $isValid for product: ${input.productId}")

            val output = ValidateProductOutput(
                isValid = isValid,
                validationTime = System.currentTimeMillis(),
                productId = input.productId
            )

            client.newCompleteCommand(job.key)
                .variables(output)
                .send()
                .join()

            logger.info("Completed validate-product task")

        } catch (e: Exception) {
            logger.error("Error in validate-product task", e)
            handleJobFailure(job, e)
        }
    }

    /**
     * Create order task
     */
    @JobWorker(type = "create-order", autoComplete = false)
    fun createOrder(job: ActivatedJob) {
        try {
            logger.info("Starting create-order task for process: ${job.processInstanceKey}")

            val input = getVariablesAs<CreateOrderInput>(job)

            val orderId = "ORD-${System.currentTimeMillis()}"
            val totalPrice = input.price * input.quantity

            logger.info("Order created: $orderId for product: ${input.productId}, qty: ${input.quantity}")

            // Update process instance
            updateProcessInstance(job.processInstanceKey, "IN_PROGRESS")

            val output = CreateOrderOutput(
                orderId = orderId,
                totalPrice = totalPrice,
                createdAt = LocalDateTime.now(),
                productId = input.productId
            )

            client.newCompleteCommand(job.key)
                .variables(output)
                .send()
                .join()

            logger.info("Completed create-order task")

        } catch (e: Exception) {
            logger.error("Error in create-order task", e)
            handleJobFailure(job, e)
        }
    }

    /**
     * Send notification task
     */
    @JobWorker(type = "send-notification", autoComplete = false)
    fun sendNotification(job: ActivatedJob) {
        try {
            logger.info("Starting send-notification task for process: ${job.processInstanceKey}")

            val input = getVariablesAs<SendNotificationInput>(job)

            logger.info("Sending ${input.notificationType} notification to ${input.email} for order: ${input.orderId}")

            // Simulate sending email/SMS
            Thread.sleep(500)

            val output = SendNotificationOutput(
                notificationSent = true,
                sentAt = LocalDateTime.now(),
                orderId = input.orderId
            )

            client.newCompleteCommand(job.key)
                .variables(output)
                .send()
                .join()

            logger.info("Completed send-notification task")

        } catch (e: Exception) {
            logger.error("Error in send-notification task", e)
            handleJobFailure(job, e)
        }
    }

    /**
     * Fulfill order task
     */
    @JobWorker(type = "fulfill-order", autoComplete = false)
    fun fulfillOrder(job: ActivatedJob) {
        try {
            logger.info("Starting fulfill-order task for process: ${job.processInstanceKey}")

            val input = getVariablesAs<FulfillOrderInput>(job)

            logger.info("Fulfilling order: ${input.orderId}")

            // Update process instance to completed
            updateProcessInstance(job.processInstanceKey, "COMPLETED")

            val output = FulfillOrderOutput(
                fulfilled = true,
                fulfilledAt = LocalDateTime.now(),
                orderId = input.orderId
            )

            client.newCompleteCommand(job.key)
                .variables(output)
                .send()
                .join()

            logger.info("Completed fulfill-order task")

        } catch (e: Exception) {
            logger.error("Error in fulfill-order task", e)
            handleJobFailure(job, e)
        }
    }

    /**
     * Extracted to avoid repetition
     */
    private fun handleJobFailure(job: ActivatedJob, exception: Exception) {
        try {
            client.newFailCommand(job.key)
                .retries(maxOf(0, job.retries - 1))
                .errorMessage(exception.message ?: "Unknown error")
                .send()
                .join()
        } catch (e: Exception) {
            logger.error("Failed to fail job", e)
        }
    }

    /**
     * Extracted to avoid repetition
     */
    private fun updateProcessInstance(processInstanceKey: Long, status: String) {
        try {
            val processInstance = processInstanceRepository
                .findByProcessInstanceId(processInstanceKey.toString())

            processInstance?.apply {
                this.status = status
                this.updatedAt = LocalDateTime.now()
                processInstanceRepository.save(this)
            }
        } catch (e: Exception) {
            logger.warn("Failed to update process instance status", e)
        }
    }
}

// ============================================================================
// INPUT/OUTPUT DATA CLASSES - Type-safe variable containers
// ============================================================================

/**
 * Validate Product Task Input Variables
 */
data class ValidateProductInput(
    val productId: Int = 0,
    val productName: String? = null
)

/**
 * Validate Product Task Output Variables
 */
data class ValidateProductOutput(
    val isValid: Boolean,
    val validationTime: Long,
    val productId: Int
)

/**
 * Create Order Task Input Variables
 */
data class CreateOrderInput(
    val productId: Int = 0,
    val quantity: Int = 1,
    val price: Double = 0.0
)

/**
 * Create Order Task Output Variables
 */
data class CreateOrderOutput(
    val orderId: String,
    val totalPrice: Double,
    val createdAt: LocalDateTime,
    val productId: Int
)

/**
 * Send Notification Task Input Variables
 */
data class SendNotificationInput(
    val orderId: String,
    val email: String? = null,
    val notificationType: String = "EMAIL"
)

/**
 * Send Notification Task Output Variables
 */
data class SendNotificationOutput(
    val notificationSent: Boolean,
    val sentAt: LocalDateTime,
    val orderId: String
)

/**
 * Fulfill Order Task Input Variables
 */
data class FulfillOrderInput(
    val orderId: String
)

/**
 * Fulfill Order Task Output Variables
 */
data class FulfillOrderOutput(
    val fulfilled: Boolean,
    val fulfilledAt: LocalDateTime,
    val orderId: String
)
