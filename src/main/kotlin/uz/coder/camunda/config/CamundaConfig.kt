package uz.coder.camunda.config

import io.camunda.zeebe.client.ZeebeClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class CamundaConfig {

    @Value("\${camunda.client.zeebe.gatewayAddress}")
    private lateinit var gatewayAddress: String

//    @Value("\${camunda.client.zeebe.security.plaintext}")
//    private lateinit var clientSecurity: String

    @Primary
    @Bean
    fun secondZeebeClient(): ZeebeClient {
        val client = ZeebeClient.newClientBuilder()
            .gatewayAddress(gatewayAddress)
            .usePlaintext()
            .build()
//        client.newDeployResourceCommand()
//            .addResourceFromClasspath("order-process.bpmn")
//            .send()
//            .join()
        return client
    }

}