package uk.gov.justice.digital.hmpps

import org.springframework.jms.core.JmsTemplate
import java.time.Duration
import java.time.LocalDateTime.now
import java.util.concurrent.TimeoutException

fun JmsTemplate.convertSendAndWait(queueName: String, message: Any, timeout: Duration = Duration.ofSeconds(5)) {
    convertAndSend(queueName, message)
    waitForQueueToBeEmpty(queueName, timeout)
}

fun JmsTemplate.waitForQueueToBeEmpty(queueName: String, timeout: Duration = Duration.ofSeconds(5)) {
    val endTime = now().plus(timeout)
    while (getMessageCount(queueName) > 0) {
        if (now().isAfter(endTime)) throw TimeoutException("Message not read before timeout of $timeout")
    }
}

fun JmsTemplate.getMessageCount(queueName: String) =
    browse(queueName) { _, browser -> browser.enumeration.toList().size } ?: 0
