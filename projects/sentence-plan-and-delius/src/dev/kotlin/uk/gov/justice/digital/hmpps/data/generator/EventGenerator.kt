package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.service.entity.Contact
import uk.gov.justice.digital.hmpps.service.entity.ContactType
import uk.gov.justice.digital.hmpps.service.entity.Custody
import uk.gov.justice.digital.hmpps.service.entity.Disposal
import uk.gov.justice.digital.hmpps.service.entity.Event
import uk.gov.justice.digital.hmpps.service.entity.Person
import uk.gov.justice.digital.hmpps.service.entity.ReferenceData
import java.time.ZoneId
import java.time.ZonedDateTime

object EventGenerator {
    val DEFAULT_EVENT = generateEvent(PersonGenerator.DEFAULT)
    val DEFAULT_DISPOSAL = generateDisposal(DEFAULT_EVENT)
    val DEFAULT_CUSTODY = generateCustody(DEFAULT_DISPOSAL)
    val NON_CUSTODIAL_EVENT = generateEvent(PersonGenerator.NON_CUSTODIAL)
    val NON_CUSTODIAL_DISPOSAL = generateDisposal(NON_CUSTODIAL_EVENT)
    val NON_CUSTODIAL_CUSTODY = generateCustody(NON_CUSTODIAL_DISPOSAL, ReferenceDataGenerator.TC_STATUS_NO_CUSTODY)
    val FIRST_APPT_CT = generateContactType()
    val FIRST_APPT_CONTACT = generateFirstAppointment(PersonGenerator.NON_CUSTODIAL, NON_CUSTODIAL_EVENT)

    fun generateEvent(person: Person, id: Long = IdGenerator.getAndIncrement()) =
        Event(id, person, null)

    fun generateDisposal(event: Event, id: Long = IdGenerator.getAndIncrement()) =
        Disposal(id, event, null)

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

    fun generateContactType(id: Long = IdGenerator.getAndIncrement()) = ContactType(id, "COAI")
}
