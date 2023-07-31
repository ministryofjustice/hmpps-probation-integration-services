package uk.gov.justice.digital.hmpps.integrations.prison

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "prison-api", url = "\${integrations.prison-api.url}/api/bookings")
interface PrisonApiClient {

    @GetMapping(value = ["/{id}"])
    fun getBooking(
        @PathVariable("id") id: Long,
        @RequestParam basicInfo: Boolean = false,
        @RequestParam extraInfo: Boolean = true
    ): Booking
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
        IN, OUT
    }

    enum class Type(val received: String?, val released: String?) {
        ADMISSION("ADMISSION", null),
        COURT("RETURN_FROM_COURT", "SENT_TO_COURT"),
        HOSPITAL(null, "RELEASE_TO_HOSPITAL"),
        RELEASE(null, "RELEASED"),
        TEMPORARY_ABSENCE("TEMPORARY_ABSENCE_RETURN", "TEMPORARY_ABSENCE_RELEASE"),
        TRANSFER("TRANSFERRED", "TRANSFERRED"),
        OTHER(null, null)
    }

    val reason: String?
        get() {
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
            return when (inOutStatus) {
                InOutStatus.IN -> type.received
                InOutStatus.OUT -> type.released
            }
        }
}
