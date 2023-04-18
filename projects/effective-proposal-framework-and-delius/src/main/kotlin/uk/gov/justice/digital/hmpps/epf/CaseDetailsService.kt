package uk.gov.justice.digital.hmpps.epf

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.epf.entity.PersonRepository
import uk.gov.justice.digital.hmpps.epf.entity.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.epf.entity.ResponsibleOfficerRepository
import uk.gov.justice.digital.hmpps.epf.entity.getPerson

@Service
class CaseDetailsService(
    private val personRepository: PersonRepository,
    private val responsibleOfficerRepository: ResponsibleOfficerRepository
) {
    fun caseDetails(crn: String, eventNumber: Int): CaseDetails {
        val person = personRepository.getPerson(crn)
        val responsibleOfficer = responsibleOfficerRepository.findByPersonIdAndEndDateIsNull(person.id)

        return CaseDetails(
            person.name(),
            person.dateOfBirth,
            person.gender.description,
            null,
            responsibleOfficer?.provider()
        )
    }

    fun ResponsibleOfficer.provider(): Provider {
        val provider = communityManager?.provider ?: prisonManager?.provider
        return Provider(provider!!.code, provider.description)
    }
}

