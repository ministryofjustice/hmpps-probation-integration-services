package uk.gov.justice.digital.hmpps.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.ProbationRecord
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.CaseAllocationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getByNomsId
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.registration.entity.findMappaRegistration
import uk.gov.justice.digital.hmpps.services.mapping.record

@Service
class ProbationRecordService(
    val personRepository: PersonRepository,
    val caseAllocationRepository: CaseAllocationRepository,
    val registrationRepository: RegistrationRepository
) {
    fun findByNomsId(nomsId: String): ProbationRecord {
        val person = personRepository.getByNomsId(nomsId)
        val decision = caseAllocationRepository.findLatestActiveDecision(person.id)
        val registration = registrationRepository.findMappaRegistration(person.id)
        return person.record(decision, registration)
    }
}
