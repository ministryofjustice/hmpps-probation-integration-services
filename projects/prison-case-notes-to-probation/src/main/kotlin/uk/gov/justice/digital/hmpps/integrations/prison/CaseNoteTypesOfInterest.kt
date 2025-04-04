package uk.gov.justice.digital.hmpps.integrations.prison

object CaseNoteTypesOfInterest {
    private val typeSubTypeMap = mapOf(
        "PRISON" to setOf("RELEASE"),
        "TRANSFER" to setOf("FROMTOL"),
        "GEN" to setOf("OSE"),
        "RESET" to setOf("BCST"),
        "ALERT" to setOf(),
        "OMIC" to setOf(),
        "OMIC_OPD" to setOf(),
        "KA" to setOf()
    )

    fun forSearchRequest(useAlertsApi: Boolean): Set<TypeSubTypeRequest> =
        typeSubTypeMap.filter { !useAlertsApi || it.key != "ALERT" }.map { TypeSubTypeRequest(it.key, it.value) }
            .toSet()

    fun verifyOfInterest(type: String, subType: String, ignoreAlerts: Boolean): Boolean {
        if (ignoreAlerts && type == "ALERT") return false
        val subTypes = typeSubTypeMap[type] ?: return false
        return subTypes.isEmpty() || subTypes.contains(subType)
    }
}