package uk.gov.justice.digital.hmpps.exception

class IgnorableMessageException(
    override val message: String,
    val additionalProperties: Map<String, String> = mapOf()
) : RuntimeException(message) {
    companion object {
        inline fun <reified T> T?.orIgnore(reason: () -> String): T = this ?: throw IgnorableMessageException(reason())
    }
}