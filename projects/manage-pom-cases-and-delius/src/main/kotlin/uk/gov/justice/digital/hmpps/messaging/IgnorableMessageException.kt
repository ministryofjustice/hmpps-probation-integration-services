package uk.gov.justice.digital.hmpps.messaging

class IgnorableMessageException(
    override val message: String,
    val additionalInformation: Map<String, String> = mapOf(),
) : RuntimeException(message)
