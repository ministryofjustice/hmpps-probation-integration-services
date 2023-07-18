package uk.gov.justice.digital.hmpps.integrations.delius.recall.reason

enum class RecallReasonCode(val code: String) {
    NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT("NN"),
    END_OF_TEMPORARY_LICENCE("EOTL"),
    TRANSFER_TO_SECURE_HOSPITAL("TSH")
}
