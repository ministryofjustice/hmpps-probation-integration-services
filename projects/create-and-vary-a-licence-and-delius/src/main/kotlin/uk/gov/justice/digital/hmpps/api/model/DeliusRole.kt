package uk.gov.justice.digital.hmpps.api.model

enum class DeliusRole(
    override val description: String,
    override val mappedRole: String,
) : uk.gov.justice.digital.hmpps.ldap.DeliusRole {
    LHDCBT002("Digital Licence Create", "CVL_DLC"),
    ;

    companion object {
        fun from(role: String): DeliusRole? = entries.firstOrNull { it.mappedRole == role.uppercase() }
    }
}
