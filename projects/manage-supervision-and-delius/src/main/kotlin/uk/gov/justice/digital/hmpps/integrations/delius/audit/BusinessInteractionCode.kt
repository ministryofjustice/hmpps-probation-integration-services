package uk.gov.justice.digital.hmpps.integrations.delius.audit

import uk.gov.justice.digital.hmpps.audit.InteractionCode

enum class BusinessInteractionCode(override val code: String) : InteractionCode {
    ADD_CONTACT("CLBI003"),
    INSERT_ADDRESS("OIBI029"),
    UPDATE_ADDRESS("OIBI032"),
    UPDATE_PERSON("OIBI027"),
}
