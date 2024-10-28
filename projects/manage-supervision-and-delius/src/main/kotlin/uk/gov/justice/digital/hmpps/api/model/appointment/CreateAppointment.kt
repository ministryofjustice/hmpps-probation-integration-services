package uk.gov.justice.digital.hmpps.api.model.appointment

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
        HomeVisitToCaseNS("CHVS"),
        InitialAppointmentInOfficeNS("COAI"),
        PlannedOfficeVisitNS("COAP"),
        InitialAppointmentHomeVisitNS("COHV")
    }

    companion object {
        const val URN_PREFIX = "urn:uk:gov:hmpps:manage-supervision-service:appointment:"
    }
}
