package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

object EventGenerator {
    val DEFAULT = generate(person = PersonGenerator.DEFAULT)

    fun generate(
        person: Person,
        id: Long? = IdGenerator.getAndIncrement()
    ) = Event(
        id = id,
        person = person,
        number = "1",
        referralDate = LocalDate.now(),
        active = true,
        ftcCount = 0
    )
}

object OrderManagerGenerator {
    val DEFAULT = generate(event = EventGenerator.DEFAULT)
    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        allocationDate: LocalDate = LocalDate.now(),
        teamId: Long = TeamGenerator.UNALLOCATED.id,
        staffId: Long = StaffGenerator.UNALLOCATED.id,
        event: Event = EventGenerator.DEFAULT,
        allocationReason: ReferenceData = ReferenceDataGenerator.ORDER_MANAGER_INITIAL_ALLOCATION,
        transferReason: TransferReason = TransferReasonGenerator.CASE_ORDER
    ) = OrderManager(
        id = id,
        allocationDate = allocationDate,
        teamId = teamId,
        staffId = staffId,
        event = event,
        allocationReason = allocationReason,
        transferReason = transferReason
    )
}

object ContactGenerator {
    val EAPP =
        generate(person = PersonGenerator.DEFAULT, event = EventGenerator.DEFAULT, type = ContactTypeGenerator.EAPP)

    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        person: Person,
        event: Event? = null,
        type: ContactType
    ) = Contact(
        id = id,
        date = LocalDate.now(),
        eventId = event?.id,
        person = person,
        startTime = ZonedDateTime.now(),
        endTime = ZonedDateTime.now().plusDays(1),
        type = type,
        staff = StaffGenerator.UNALLOCATED,
        staffEmployeeId = StaffGenerator.UNALLOCATED.id,
        team = TeamGenerator.UNALLOCATED,
        providerEmployeeId = TeamGenerator.UNALLOCATED.id
    )
}

object CourtAppearanceGenerator {
    val TRIAL_ADJOURNMENT = generate(
        event = EventGenerator.DEFAULT,
        appearanceType = ReferenceDataGenerator.TRIAL_ADJOURNMENT_APPEARANCE_TYPE,
        court = CourtGenerator.UNKNOWN_COURT_N07_PROVIDER,
        person = PersonGenerator.DEFAULT,
        hearingId = UUID.randomUUID().toString()
    )

    val TRIAL_ADJOURNMENT_NO_HEARING = generate(
        event = EventGenerator.DEFAULT,
        appearanceType = ReferenceDataGenerator.TRIAL_ADJOURNMENT_APPEARANCE_TYPE,
        court = CourtGenerator.UNKNOWN_COURT_N07_PROVIDER,
        person = PersonGenerator.DEFAULT
    )

    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        appearanceDate: LocalDate = LocalDate.now(),
        event: Event,
        appearanceType: ReferenceData,
        court: Court,
        person: Person,
        hearingId: String? = null
    ) = CourtAppearance(
        id = id,
        appearanceDate = appearanceDate,
        event = event,
        softDeleted = false,
        appearanceType = appearanceType,
        person = person,
        court = court,
        courtNotes = hearingId
    )
}