package uk.gov.justice.digital.hmpps

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component

@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}

@Component
class Receiver {
    @JmsListener(destination = "MyQueue")
    fun receiveMessage(string: String) {
        println("Received <$string>")
    }
}