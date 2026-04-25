package uz.coder.camunda.config

import io.camunda.zeebe.client.ZeebeClient
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ProcessDeployer(private val zeebeClient: ZeebeClient) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun deployProcesses() {
        log.info("Deploying processes...")
        zeebeClient.newDeployResourceCommand()
            .addResourceFromClasspath("order-process.bpmn")
//            .addResourceFromClasspath("order-process2.bpmn")
            .send()
            .join()
        log.info("Deployed processes...")
    }
}