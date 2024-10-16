package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.*
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import java.time.LocalDate

object SentenceGenerator {
    var DEFAULT_CUSTODY: Custody =
        Custody(IdGenerator.getAndIncrement(), ReferenceDataGenerator.DEFAULT_CUSTODY_STATUS, "38339A")

    var DEFAULT_DISPOSAL_TYPE = generateDisposalType()

    fun generateEvent(
        person: Person = PersonGenerator.DEFAULT,
        number: String = "1",
        firstReleaseDate: LocalDate? = null
    ) =
        Event(IdGenerator.getAndIncrement(), number, person, firstReleaseDate)

    fun generateOrderManager(event: Event, providerId: Long = 1, teamId: Long = 2, staffId: Long = 3) =
        OrderManager(IdGenerator.getAndIncrement(), event, providerId, teamId, staffId)

    fun generateDisposal(event: Event, type: DisposalType = DEFAULT_DISPOSAL_TYPE) =
        Disposal(IdGenerator.getAndIncrement(), event, type)

    fun generateDisposalType(requiredInformation: String = "L1") =
        DisposalType(IdGenerator.getAndIncrement(), requiredInformation)

    fun generateCustodialSentence(
        custodyStatus: ReferenceData = ReferenceDataGenerator.DEFAULT_CUSTODY_STATUS,
        disposal: Disposal,
        bookingRef: String
    ) = Custody(IdGenerator.getAndIncrement(), custodyStatus, bookingRef, disposal)
}
