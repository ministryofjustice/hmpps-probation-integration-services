package uk.gov.justice.digital.hmpps.exception

class IgnorableMessageException(
    override val message: String,
    val additionalProperties: Map<String, String> = mapOf()
) : RuntimeException(message)