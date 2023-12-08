package uk.gov.justice.digital.hmpps.model

enum class DeliusRole(
    override val description: String,
    override val mappedRole: String,
) : uk.gov.justice.digital.hmpps.ldap.DeliusRole {
    CTRBT001("Pathfinder CT Probation", "PF_STD_PROBATION"),
    CTRBT002("Pathfinder CT Approval", "PF_APPROVAL"),
    CTRBT003("Pathfinder National Reader", "PF_NATIONAL_READER"),
    CTRBT004("Pathfinder HQ User", "PF_HQ"),
    CTRBT005("Pathfinder User", "PF_USER"),
    CTRBT006("Pathfinder Admin", "PF_ADMIN"),
    ;

    companion object {
        fun from(role: String): DeliusRole? = entries.firstOrNull { it.mappedRole == role.uppercase() }
    }
}
