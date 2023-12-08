package uk.gov.justice.digital.hmpps.integrations.delius

class DeliusValidationError(override val message: String) : RuntimeException(message) {
    fun ignored() = message in IGNORED_VALIDATION_MESSAGES

    companion object {
        private val KNOWN_VALIDATION_MESSAGES =
            listOf(
                "The existing CAS Assessment Date is greater than a specified P_ASSESSMENT_DATE value",
                "The Event is Soft Deleted",
                "The event number does not exist against the specified Offender",
                "CRN/Offender does not exist",
                "No Event number provided",
                "Event Number = Null and no active events for the case",
            )

        private val IGNORED_VALIDATION_MESSAGES =
            listOf(
                "Event is Terminated",
                "Event does not exist for crn",
            ) + KNOWN_VALIDATION_MESSAGES

        fun isKnownValidationMessage(message: String): Boolean = message in KNOWN_VALIDATION_MESSAGES
    }
}
