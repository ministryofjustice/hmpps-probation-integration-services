package uk.gov.justice.digital.hmpps

import org.apache.activemq.artemis.core.server.Queue
import org.springframework.jms.core.JmsTemplate
import java.time.Duration
import java.time.LocalDateTime.now
import java.util.concurrent.TimeoutException

fun JmsTemplate.convertSendAndWait(queue: Queue, message: Any) {
    convertAndSendWithDelay(queue, message)
    queue.waitForQueueToBeEmpty()
}

fun JmsTemplate.convertAndSendWithDelay(queue: Queue, message: Any, deliveryDelay: Long = 0) {
    // Temporarily override the deliveryDelay set in the JmsTemplate, so that test messages are delivered immediately
    val initialDeliveryDelay = this.deliveryDelay
    this.deliveryDelay = deliveryDelay
    convertAndSend(queue.name.toString(), message)
    this.deliveryDelay = initialDeliveryDelay
}

fun Queue.waitForQueueToBeEmpty(timeout: Duration = Duration.ofSeconds(5)) {
    val endTime = now().plus(timeout)
    while (messageCount > 0L) {
        if (now().isAfter(endTime)) throw TimeoutException("Message not read before timeout of $timeout")
    }
}

fun Queue.waitForMessagesToBeAcknowledged(timeout: Duration = Duration.ofSeconds(5)) {
    val endTime = now().plus(timeout)
    while (messagesAcknowledged == 0L) {
        if (now().isAfter(endTime)) throw TimeoutException("Message not acknowledged before timeout of $timeout")
    }
}
