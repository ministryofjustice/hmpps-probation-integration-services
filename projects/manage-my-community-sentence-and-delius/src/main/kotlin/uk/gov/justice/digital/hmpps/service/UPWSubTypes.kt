package uk.gov.justice.digital.hmpps.service

import uk.gov.justice.digital.hmpps.entity.sentence.requirement.Requirement

enum class UPWSubTypes(val code: String, val description: String, val concurrency: Boolean) {
    REGULAR("W01", "Regular UPW Hours", false),
    ADDITIONAL("W03", "Additional UPW Hours", false),
    HOURS_CONSECUTIVE_TO_OTHER_ORDER("W07", "Hours consecutive to another order", false),
    HOURS_CONCURRENT_TO_ANOTHER_ORDER("W06", "Hours Concurrent to another Order", true),
    HOURS_OUTSTANDING_AT_SENTENCE_EXPIRY("W08", "Hours Outstanding at Sentence Expiry", false),
    HOURS_OUTSTANDING_FROM_PREVIOUS_ORDER("W05", "Z - Hours Outstanding from a Previous Order", false);

    companion object {
        fun Requirement.upwSubType() =
            requireNotNull(subCategory?.code?.let { code -> entries.find { it.code == code } }) {
                "Unexpected unpaid work sub-type: ${subCategory?.code}"
            }
    }
}