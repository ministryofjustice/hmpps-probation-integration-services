package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.ldap.DeliusRole

enum class LicencesRole(
    override val description: String,
    override val mappedRole: String,
) : DeliusRole {
    LHDCBT002("Digital Licence Create", "LHDCBT002"),
    LHDCBT003("Digital Licence Vary", "LHDCBT003"),
}
