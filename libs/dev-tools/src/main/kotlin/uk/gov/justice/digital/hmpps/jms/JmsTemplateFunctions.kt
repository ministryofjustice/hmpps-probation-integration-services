package uk.gov.justice.digital.hmpps.jms

import org.apache.activemq.artemis.core.server.Queue
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ
import org.springframework.jms.core.JmsTemplate
import java.time.Duration
import java.time.LocalDateTime.now
import java.util.concurrent.TimeoutException

fun JmsTemplate.convertSendAndWait(activeMQ: EmbeddedActiveMQ, queueName: String, message: Any) {
    convertSendAndWait(activeMQ.activeMQServer.locateQueue(queueName), message)
}

fun JmsTemplate.convertSendAndWait(queue: Queue, message: Any) {
    convertAndSend(queue.name.toString(), message)
    queue.waitForQueueToBeEmpty()
}

fun Queue.waitForQueueToBeEmpty(timeout: Duration = Duration.ofSeconds(5)) {
    val endTime = now().plus(timeout)
    while (messageCount > 0L) {
        if (now().isAfter(endTime)) throw TimeoutException("Message not read before timeout of $timeout")
    }
}
