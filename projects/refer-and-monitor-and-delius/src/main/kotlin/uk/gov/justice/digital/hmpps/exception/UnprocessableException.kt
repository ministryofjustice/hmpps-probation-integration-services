package uk.gov.justice.digital.hmpps.exception

open class UnprocessableException(
    override val message: String,
    val properties: Map<String, String> = mapOf(),
) : RuntimeException(message)
