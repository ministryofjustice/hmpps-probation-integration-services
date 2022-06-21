package uk.gov.justice.digital.hmpps

import org.springframework.jms.core.JmsTemplate

fun JmsTemplate.convertSendAndWait(queueName: String, message: Any) {
    convertAndSend(queueName, message)
    var messages = browse(queueName) { _, browser ->
        browser.enumeration.toList().size
    } ?: 0
    while (messages > 0) {
        messages = browse(queueName) { _, browser ->
            browser.enumeration.toList().size
        } ?: 0
    }
}
