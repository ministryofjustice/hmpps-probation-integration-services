package uk.gov.justice.digital.hmpps.integrations.delius.audit

import uk.gov.justice.digital.hmpps.audit.InteractionCode

enum class BusinessInteractionCode(override val code: String) : InteractionCode {
    INSERT_PERSON("OIBI025"),
    INSERT_ADDRESS("OIBI029"),
    INSERT_EVENT("CWBI006"),
    INSERT_COURT_APPEARANCE("CWBI007")
}
