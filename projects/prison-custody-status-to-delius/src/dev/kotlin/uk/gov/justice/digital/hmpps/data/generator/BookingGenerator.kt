package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.prison.Booking

object BookingGenerator {
    private val options = ('A'..'Z') + ('0'..'9')

    val RELEASED = generateRelease(PersonGenerator.RELEASABLE.nomsNumber, "NCS", active = false)
    val RECEIVED = generateReceive(PersonGenerator.RECALLABLE.nomsNumber, "R1")
    val DIED = generateRelease(PersonGenerator.DIED.nomsNumber, "DEC", active = false)
    val MATCHED = generateReceive(
        PersonGenerator.MATCHABLE.nomsNumber,
        "INT",
        prisonId = InstitutionGenerator.MOVED_TO.nomisCdeCode!!
    )
    val NEW_CUSTODY = generateReceive(PersonGenerator.NEW_CUSTODY.nomsNumber, "N")
    val RECALLED = generateReceive(PersonGenerator.RECALLED.nomsNumber, "24")
    val HOSPITAL_RELEASE = generateRelease(PersonGenerator.HOSPITAL_RELEASED.nomsNumber, "HO")
    val HOSPITAL_CUSTODY = generateRelease(PersonGenerator.HOSPITAL_IN_CUSTODY.nomsNumber, "HQ", active = false)
    val ROTL_RETURN = generateReceive(PersonGenerator.ROTL.nomsNumber, "24", "TAP")
    val IRC_RELEASED = generateRelease(PersonGenerator.IRC_RELEASED.nomsNumber, "DE")
    val IRC_CUSTODY = generateRelease(PersonGenerator.IRC_IN_CUSTODY.nomsNumber, "DD")
    val ECSL_ACTIVE = generateRelease(PersonGenerator.RELEASABLE_ECSL_ACTIVE.nomsNumber, "ECSL")
    val ABSCONDED = generateRelease(PersonGenerator.ABSCONDED.nomsNumber, "UAL", active = false)
    val ETR_CUSTODY = generateRelease(PersonGenerator.ETR_IN_CUSTODY.nomsNumber, "ETR")

    fun generateRelease(
        nomsId: String,
        movementReason: String,
        movementType: String = "REL",
        prisonId: String = "OUT",
        inOutStatus: Booking.InOutStatus = Booking.InOutStatus.OUT,
        active: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = Booking(
        id,
        bookingReference(),
        active,
        nomsId,
        prisonId,
        movementType,
        movementReason,
        inOutStatus
    )

    fun generateReceive(
        nomsId: String,
        movementReason: String,
        movementType: String = "ADM",
        prisonId: String = InstitutionGenerator.DEFAULT.nomisCdeCode!!,
        inOutStatus: Booking.InOutStatus = Booking.InOutStatus.IN,
        active: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = Booking(
        id,
        bookingReference(),
        active,
        nomsId,
        prisonId,
        movementType,
        movementReason,
        inOutStatus
    )

    fun bookingReference() = List(6) { options.random() }.joinToString("")
}