package uk.gov.justice.digital.hmpps.integrations.prison

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PostExchange
import java.time.LocalDate
import java.time.LocalTime

interface PrisonApiClient {

    @GetExchange(value = "/bookings/offenderNo/{nomsId}")
    fun getBookingByNomsId(
        @PathVariable("nomsId") id: String,
        @RequestParam basicInfo: Boolean = false,
        @RequestParam extraInfo: Boolean = true
    ): Booking

    @PostExchange(value = "/movements/offenders")
    fun getLatestMovement(
        @RequestBody offenderIds: List<String>,
        @RequestParam latestOnly: Boolean = true,
    ): List<Movement>
}

data class Booking(
    @JsonAlias("bookingId")
    val id: Long,
    @JsonAlias("bookingNo")
    val reference: String,
    @JsonAlias("activeFlag")
    val active: Boolean,
    @JsonAlias("offenderNo")
    val personReference: String,
    val agencyId: String,
    @JsonAlias("lastMovementTypeCode")
    val movementType: String,
    @JsonAlias("lastMovementReasonCode")
    val movementReason: String,
    val inOutStatus: InOutStatus
) {
    enum class InOutStatus {
        IN, OUT, TRN
    }

    enum class Type(val received: String?, val released: String?) {
        ADMISSION("ADMISSION", null),
        COURT("RETURN_FROM_COURT", "SENT_TO_COURT"),
        HOSPITAL(null, "RELEASED_TO_HOSPITAL"),
        RELEASE(null, "RELEASED"),
        TEMPORARY_ABSENCE("TEMPORARY_ABSENCE_RETURN", "TEMPORARY_ABSENCE_RELEASE"),
        TRANSFER("TRANSFERRED", "TRANSFERRED"),
        OTHER(null, null)
    }

    val reason = let {
        val type = when (movementType) {
            "CRT" -> Type.COURT
            "TAP" -> Type.TEMPORARY_ABSENCE
            "ADM" -> {
                when (movementReason) {
                    "INT", "TRNCRT", "TRNTAP" -> Type.TRANSFER
                    else -> Type.ADMISSION
                }
            }

            "REL" -> {
                when (movementReason) {
                    "HO", "HP", "HQ" -> Type.HOSPITAL
                    else -> Type.RELEASE
                }
            }

            else -> Type.OTHER
        }
        when (inOutStatus) {
            InOutStatus.IN -> type.received
            InOutStatus.OUT -> type.released
            InOutStatus.TRN -> null
        }
    }
}

data class Movement(
    val fromAgency: String?,
    val toAgency: String,
    val movementType: String,
    @JsonAlias("movementReasonCode")
    val movementReason: String,
    val movementDate: LocalDate,
    val movementTime: LocalTime
)
