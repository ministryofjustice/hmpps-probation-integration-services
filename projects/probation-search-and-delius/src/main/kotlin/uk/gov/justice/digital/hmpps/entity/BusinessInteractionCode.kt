package uk.gov.justice.digital.hmpps.entity

import uk.gov.justice.digital.hmpps.audit.InteractionCode

enum class BusinessInteractionCode(override val code: String) : InteractionCode {
    SEARCH_CONTACTS("CLBI050")
}
