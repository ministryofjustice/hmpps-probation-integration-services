package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.event.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.institution.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode

object EventGenerator {
    fun custodialEvent(
        person: Person,
        institution: Institution,
        custodialStatusCode: CustodialStatusCode = CustodialStatusCode.IN_CUSTODY,
    ): Event {
        val event = Event(
            IdGenerator.getAndIncrement(),
            person,
            Disposal(
                IdGenerator.getAndIncrement(),
                Custody(
                    IdGenerator.getAndIncrement(),
                    ReferenceDataGenerator.CUSTODIAL_STATUS[custodialStatusCode]!!,
                    institution
                ),
                DisposalType(IdGenerator.getAndIncrement(), "NC")
            )
        )
        return event.copy(
            disposal = event.disposal.copy(
                event = event,
                custody = event.disposal.custody.copy(
                    disposal = event.disposal
                )
            )
        )
    }
}
