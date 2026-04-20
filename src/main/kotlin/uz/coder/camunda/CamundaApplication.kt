package uz.coder.camunda

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CamundaApplication

fun main(args: Array<String>) {
    runApplication<CamundaApplication>(*args)
}
