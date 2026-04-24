package uz.coder.camunda.config

import io.camunda.zeebe.client.ZeebeClient
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ProcessDeployer(private val zeebeClient: ZeebeClient) {
    
    @EventListener(ApplicationReadyEvent::class)
    fun deployProcesses() {
        zeebeClient.newDeployResourceCommand()
            .addResourceFromClasspath("order-process.bpmn")
            .send()
            .join()
    }
}