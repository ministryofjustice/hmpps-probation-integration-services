package uk.gov.justice.digital.hmpps.api.model

enum class AllocationReason {
    ALLOCATED_TO_RESPONSIBLE_OFFICER,
    CASELOAD_ADJUSTMENT,
    CHANGE_IN_TIER_OR_RISK,
    CHANGE_OF_ADDRESS,
    OFFICER_LEFT,
    OTHER,
    RISK_TO_STAFF,
    TRANSFER_IN,
    PROBATION_RESET,
    INITIAL_ALLOCATION;

    companion object {
        fun fromTextOrDefaultInitial(value: String?): AllocationReason {
            val parsed = value
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let { runCatching { AllocationReason.valueOf(it) }.getOrNull() }

            return parsed ?: INITIAL_ALLOCATION
        }
    }
}

enum class AllocationType {
    PERSON,
    ORDER,
    REQUIREMENT,
}

data class DeliusCodes(
    val person: String,
    val order: String,
    val requirement: String,
) {
    fun forType(type: AllocationType): String = when (type) {
        AllocationType.PERSON -> person
        AllocationType.ORDER -> order
        AllocationType.REQUIREMENT -> requirement
    }
}

val ALLOCATION_REASON_TO_DELIUS: Map<AllocationReason, DeliusCodes> = mapOf(
    AllocationReason.ALLOCATED_TO_RESPONSIBLE_OFFICER to DeliusCodes("RO", "RO", "RO"),
    AllocationReason.CASELOAD_ADJUSTMENT to DeliusCodes("AR02", "AR02", "AR02"),
    AllocationReason.CHANGE_IN_TIER_OR_RISK to DeliusCodes("CTR", "CTR", "CTR"),
    AllocationReason.CHANGE_OF_ADDRESS to DeliusCodes("AR03", "OA01", "OM"),
    AllocationReason.OFFICER_LEFT to DeliusCodes("OFL", "OFL", "OFL"),
    AllocationReason.OTHER to DeliusCodes("OTH", "OTH", "OTH"),
    AllocationReason.RISK_TO_STAFF to DeliusCodes("RTS", "RTS", "RTS"),
    AllocationReason.TRANSFER_IN to DeliusCodes("TIN", "TIN", "TIN"),
    AllocationReason.PROBATION_RESET to DeliusCodes("RESET", "RESET", "RESET"),
    AllocationReason.INITIAL_ALLOCATION to DeliusCodes("IN1", "INT", "IN1"),
)

fun deriveDeliusCodeDefaultInitial(
    reason: AllocationReason?,
    type: AllocationType,
): String {
    val effective = reason ?: AllocationReason.INITIAL_ALLOCATION
    val codes = ALLOCATION_REASON_TO_DELIUS.getValue(effective)
    return codes.forType(type)
}

fun deriveDeliusCodeFromTextDefaultInitial(
    reasonText: String?,
    type: AllocationType,
): String {
    val reason = AllocationReason.fromTextOrDefaultInitial(reasonText)
    return deriveDeliusCodeDefaultInitial(reason, type)
}




