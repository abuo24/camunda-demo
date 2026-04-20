package uz.coder.camunda

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class CamundaApplication

fun main(args: Array<String>) {
    runApplication<CamundaApplication>(*args)
}
