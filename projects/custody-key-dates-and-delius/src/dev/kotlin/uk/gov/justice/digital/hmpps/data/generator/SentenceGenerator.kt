package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.Event
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.OrderManager
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object SentenceGenerator {
    var DEFAULT_CUSTODY: Custody =
        Custody(IdGenerator.getAndIncrement(), ReferenceDataGenerator.DEFAULT_CUSTODY_STATUS, "38339A")

    fun generateEvent(person: Person = PersonGenerator.DEFAULT, number: String = "1") =
        Event(IdGenerator.getAndIncrement(), number, person)

    fun generateOrderManager(event: Event, providerId: Long = 1, teamId: Long = 2, staffId: Long = 3) =
        OrderManager(IdGenerator.getAndIncrement(), event, providerId, teamId, staffId)

    fun generateDisposal(event: Event) = Disposal(IdGenerator.getAndIncrement(), event)

    fun generateCustodialSentence(
        custodyStatus: ReferenceData = ReferenceDataGenerator.DEFAULT_CUSTODY_STATUS,
        disposal: Disposal,
        bookingRef: String,
    ) = Custody(IdGenerator.getAndIncrement(), custodyStatus, bookingRef, disposal)
}