package uk.gov.justice.digital.hmpps.integrations.delius

class DeliusValidationError(override val message: String) : RuntimeException(message) {
    fun ignored() = message in IGNORED_VALIDATION_MESSAGES

    companion object {
        private val IGNORED_VALIDATION_MESSAGES = listOf(
            "No Event number provided"
        )

        private val KNOWN_VALIDATION_MESSAGES = listOf(
            "The existing CAS Assessment Date is greater than a specified P_ASSESSMENT_DATE value",
            "The Event is Soft Deleted",
            "The event number does not exist against the specified Offender",
            "CRN/Offender does not exist",
            "No Event number provided"
        )

        fun isKnownValidationMessage(message: String): Boolean = message in KNOWN_VALIDATION_MESSAGES
    }
}
