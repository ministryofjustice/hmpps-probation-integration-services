package uk.gov.justice.digital.hmpps.integrations.delius

import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.MessageAttribute as SnsMessageAttribute

class MessageAttribute(
    val dataType: String,
    val stringValue: String,
    val binaryValue: String? = null
)

fun Map<String, MessageAttribute>.toSnsAttributes() = MessageAttributes(
    attributes = this.entries
        .associate { it.key to SnsMessageAttribute(it.value.dataType, it.value.stringValue) }
        .toMutableMap()
)
