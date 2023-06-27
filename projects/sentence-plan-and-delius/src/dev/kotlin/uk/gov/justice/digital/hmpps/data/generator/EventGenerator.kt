package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.service.entity.Custody
import uk.gov.justice.digital.hmpps.service.entity.Disposal
import uk.gov.justice.digital.hmpps.service.entity.Event
import uk.gov.justice.digital.hmpps.service.entity.Person
import uk.gov.justice.digital.hmpps.service.entity.ReferenceData

object EventGenerator {
    val DEFAULT_EVENT = generateEvent(PersonGenerator.DEFAULT)
    val DEFAULT_DISPOSAL = generateDisposal(DEFAULT_EVENT)
    val DEFAULT_CUSTODY = generateCustody(DEFAULT_DISPOSAL)
    val NON_CUSTODIAL_EVENT = generateEvent(PersonGenerator.NON_CUSTODIAL)
    val NON_CUSTODIAL_DISPOSAL = generateDisposal(NON_CUSTODIAL_EVENT)
    val NON_CUSTODIAL_CUSTODY = generateCustody(NON_CUSTODIAL_DISPOSAL, ReferenceDataGenerator.TC_STATUS_NO_CUSTODY)

    fun generateEvent(person: Person, id: Long = IdGenerator.getAndIncrement()) =
        Event(id, person, null)

    fun generateDisposal(event: Event, id: Long = IdGenerator.getAndIncrement()) =
        Disposal(id, event, null)

    fun generateCustody(disposal: Disposal, status: ReferenceData = ReferenceDataGenerator.TC_STATUS_CUSTODY, id: Long = IdGenerator.getAndIncrement()) =
        Custody(id, status, disposal)
}
