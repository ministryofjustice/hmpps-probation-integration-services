package uk.gov.justice.digital.hmpps.integrations.delius.audit

import uk.gov.justice.digital.hmpps.audit.InteractionCode

enum class BusinessInteractionCode(override val code: String) : InteractionCode {
    SUBMIT_ASSESSMENT_SUMMARY("OABI007"),
    UPDATE_RISK_DATA("RRBI211")
}
