package uk.gov.justice.digital.hmpps.api.model

import java.time.ZonedDateTime
import java.util.*

data class CreateAppointment(
    val type: Type,
    val start: ZonedDateTime,
    val end: ZonedDateTime,
    val notes: String? = null,
    val uuid: UUID = UUID.randomUUID()
) {
    val urn = URN_PREFIX + uuid

    enum class Type(val code: String) {
        Accommodation("RP1"),
        ThinkingAndBehaviour("RP2"),
        FamilyAndCommunity("RP3"),
        DrugsAndAlcohol("RP4"),
        SkillsAndWork("RP5"),
        Finance("RP6"),
        Health("RP7"),
        Benefits("RP8")
    }

    companion object {
        const val URN_PREFIX = "urn:uk:gov:hmpps:resettlement-passport-service:appointment:"
    }
}