package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Person
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.api.resource.IdentifierType
import uk.gov.justice.digital.hmpps.api.resource.IdentifierType.CRN
import uk.gov.justice.digital.hmpps.api.resource.IdentifierType.NOMS
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrnAndSoftDeletedFalse
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByNomsIdAndSoftDeletedFalse
import uk.gov.justice.digital.hmpps.integrations.delius.person.getCaseType

@Service
class PersonService(private val personRepository: PersonRepository) {
    fun findByIdentifier(
        value: String,
        type: IdentifierType,
    ): Person {
        val person =
            when (type) {
                CRN -> personRepository.getByCrnAndSoftDeletedFalse(value)
                NOMS -> personRepository.getByNomsIdAndSoftDeletedFalse(value)
            }
        val caseType = personRepository.getCaseType(person.crn)
        return Person(person.crn, person.name(), caseType)
    }
}
