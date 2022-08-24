package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.institution.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode

object CustodyGenerator {
    fun generate(
        person: Person,
        institution: Institution,
        custodialStatusCode: CustodialStatusCode = CustodialStatusCode.IN_CUSTODY,
    ) = EventGenerator.custodialEvent(person, institution, custodialStatusCode).disposal!!.custody!!
}
