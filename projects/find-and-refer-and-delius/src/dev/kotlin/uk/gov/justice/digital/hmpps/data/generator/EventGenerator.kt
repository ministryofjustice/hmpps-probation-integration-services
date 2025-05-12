package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PERSON_1
import uk.gov.justice.digital.hmpps.entity.*

object EventGenerator {

    val CUSTODIAL_STATUS =
        ReferenceData(IdGenerator.getAndIncrement(), CustodialStatusCode.IN_CUSTODY.code, "In custody ROTL")
    val EVENT = generateEvent(PERSON_1)
    val DISPOSAL = generateDisposal(EVENT)
    val CUSTODY = generateCustody(DISPOSAL)

    private fun generateCustody(disposal: Disposal) =
        Custody(IdGenerator.getAndIncrement(), CUSTODIAL_STATUS, disposal, false)

    private fun generateDisposal(event: Event) =
        Disposal(IdGenerator.getAndIncrement(), event, active = true, softDeleted = false)

    private fun generateEvent(person: Person) =
        Event(IdGenerator.getAndIncrement(), person = person, active = true, softDeleted = false)
}
