package uk.gov.justice.digital.hmpps.exceptions

class IgnorableMessageException(
    override val message: String,
    val additionalProperties: Map<String, String> = mapOf()
) : RuntimeException(message)