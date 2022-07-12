package uk.gov.justice.digital.hmpps

import org.springframework.jms.core.JmsTemplate
import java.time.Duration
import java.time.LocalDateTime.now
import java.util.concurrent.TimeoutException

fun JmsTemplate.convertSendAndWait(
    queueName: String,
    message: Any,
    timeout: Duration = Duration.ofSeconds(5),
    deliveryDelay: Long = 0
) {
    convertAndSendWithDelay(queueName, message, deliveryDelay)
    val startTime = now()
    val endTime = startTime.plus(timeout)
    var messages: Int
    do {
        messages = browse(queueName) { _, browser ->
            browser.enumeration.toList().size
        } ?: 0
    } while (messages > 0 && now().isBefore(endTime))

    if (now().isAfter(endTime) && messages > 0) {
        throw TimeoutException("Message not processed before timeout of $timeout")
    }
}

fun JmsTemplate.convertAndSendWithDelay(
    queueName: String,
    message: Any,
    deliveryDelay: Long = 0
) {
    // Temporarily override the deliveryDelay set in the JmsTemplate (see application.yml)
    val initialDeliveryDelay = this.deliveryDelay
    this.deliveryDelay = deliveryDelay
    convertAndSend(queueName, message)
    this.deliveryDelay = initialDeliveryDelay
}
