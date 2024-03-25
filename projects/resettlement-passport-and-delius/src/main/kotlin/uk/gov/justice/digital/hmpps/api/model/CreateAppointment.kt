package uk.gov.justice.digital.hmpps.api.model

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Duration
import java.time.ZonedDateTime
import java.util.*

data class CreateAppointment(
    val type: Type,
    val start: ZonedDateTime,
    @Schema(
        type = "string",
        format = "duration",
        example = "PT30M",
        description = "ISO-8601 representation of the duration"
    )
    val duration: Duration,
    val notes: String? = null,
    val uuid: UUID = UUID.randomUUID()
) {
    @JsonIgnore
    val urn = URN_PREFIX + uuid

    @JsonIgnore
    val end: ZonedDateTime = start.plus(duration)

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