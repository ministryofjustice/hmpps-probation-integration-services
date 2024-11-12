package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.service.entity.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

object EventGenerator {

    val DISPOSAL_TYPE = generateDisposalType()
    val REQUIREMENT_CAT_F = generateRequirementCategory(code = "F")
    val REQUIREMENT_CAT_W = generateRequirementCategory(code = "W")
    val DEFAULT_EVENT = generateEvent(PersonGenerator.DEFAULT)
    val DEFAULT_DISPOSAL =
        generateDisposal(DEFAULT_EVENT, start = LocalDate.now().minusDays(2), end = LocalDate.now().plusDays(5))
    val DEFAULT_CUSTODY = generateCustody(DEFAULT_DISPOSAL)
    val NON_CUSTODIAL_EVENT = generateEvent(PersonGenerator.NON_CUSTODIAL)
    val NON_CUSTODIAL_DISPOSAL =
        generateDisposal(NON_CUSTODIAL_EVENT, start = LocalDate.now().minusDays(3), end = LocalDate.now().plusDays(7))
    val NON_CUSTODIAL_CUSTODY = generateCustody(NON_CUSTODIAL_DISPOSAL, ReferenceDataGenerator.TC_STATUS_NO_CUSTODY)
    val FIRST_APPT_CT = generateContactType()
    val FIRST_APPT_CONTACT = generateFirstAppointment(PersonGenerator.NON_CUSTODIAL, NON_CUSTODIAL_EVENT)
    val REQUIREMENT_1 = generateRequirement(disposal = DEFAULT_DISPOSAL, length = 12, cat = REQUIREMENT_CAT_F)
    val REQUIREMENT_2 = generateRequirement(disposal = NON_CUSTODIAL_DISPOSAL, length = 6, cat = REQUIREMENT_CAT_W)
    val UPW_DETAILS_1 = generateUpwDetails(disposal = DEFAULT_DISPOSAL)
    val UPW_APPOINTMENT_1 = generateUpwAppointment(upwDetails = UPW_DETAILS_1, mins = 20)
    val UPW_DETAILS_2 = generateUpwDetails(disposal = NON_CUSTODIAL_DISPOSAL)
    val UPW_APPOINTMENT_2 = generateUpwAppointment(upwDetails = UPW_DETAILS_2, mins = 3)
    val RAR_CONTACT_1 = generateRarContact(PersonGenerator.DEFAULT, DEFAULT_EVENT, requirement = REQUIREMENT_1)
    val RAR_CONTACT_2 = generateRarContact(
        PersonGenerator.DEFAULT,
        DEFAULT_EVENT,
        attended = true,
        compiled = true,
        requirement = REQUIREMENT_1
    )

    fun generateDisposalType(id: Long = IdGenerator.getAndIncrement()) =
        DisposalType(id, description = "Sentence Type 1", "CC")

    fun generateRequirementCategory(id: Long = IdGenerator.getAndIncrement(), code: String) =
        RequirementMainCategory(id, description = "Sentence Type 1", code = code)

    fun generateEvent(person: Person, id: Long = IdGenerator.getAndIncrement()) =
        Event(id, person, null)

    fun generateRequirement(
        id: Long = IdGenerator.getAndIncrement(),
        disposal: Disposal,
        length: Long,
        cat: RequirementMainCategory
    ) =
        Requirement(id = id, mainCategory = cat, disposal = disposal, length = length)

    fun generateDisposal(event: Event, id: Long = IdGenerator.getAndIncrement(), start: LocalDate, end: LocalDate) =
        Disposal(id, DISPOSAL_TYPE, event, null, start, end)

    fun generateCustody(
        disposal: Disposal,
        status: ReferenceData = ReferenceDataGenerator.TC_STATUS_CUSTODY,
        id: Long = IdGenerator.getAndIncrement()
    ) =
        Custody(id, status, disposal)

    fun generateFirstAppointment(person: Person, event: Event, id: Long = IdGenerator.getAndIncrement()): Contact {
        val date = ZonedDateTime.of(2020, 12, 1, 12, 12, 12, 0, ZoneId.of("Europe/London"))
        val startTime = ZonedDateTime.of(2020, 12, 1, 12, 12, 12, 0, ZoneId.of("Europe/London"))

        return Contact(id, person, event, FIRST_APPT_CT, date, startTime)
    }

    fun generateRarContact(
        person: Person,
        event: Event,
        id: Long = IdGenerator.getAndIncrement(),
        attended: Boolean? = null,
        compiled: Boolean? = null,
        requirement: Requirement
    ) =
        Contact(
            id,
            rarActivity = true,
            person = person,
            event = event,
            type = FIRST_APPT_CT,
            requirement = requirement,
            attended = attended,
            complied = compiled
        )

    fun generateUpwDetails(id: Long = IdGenerator.getAndIncrement(), disposal: Disposal) =
        UpwDetails(id = id, disposal = disposal)

    fun generateUpwAppointment(id: Long = IdGenerator.getAndIncrement(), mins: Long, upwDetails: UpwDetails) =
        UpwAppointment(
            id = id,
            minutesCredited = mins, attended = "Y", upwDetails = upwDetails
        )

    fun generateContactType(id: Long = IdGenerator.getAndIncrement()) = ContactType(id, "COAI")
}
