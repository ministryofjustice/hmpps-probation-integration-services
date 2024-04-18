package uk.gov.justice.digital.hmpps.epf

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.epf.entity.*

@Service
class CaseDetailsService(
    private val personRepository: PersonRepository,
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val courtAppearanceRepository: CourtAppearanceRepository,
    private val eventRepository: EventRepository,
    private val keyDateRepository: KeyDateRepository,
    private val ogrsAssessmentRepository: OgrsAssessmentRepository
) {
    @Transactional
    fun caseDetails(crn: String, eventNumber: Int): CaseDetails {
        val person = personRepository.getPerson(crn)
        val provider = responsibleOfficerRepository.findByPersonIdAndEndDateIsNull(person.id)?.provider()
            ?: personManagerRepository.findByPersonId(person.id)?.provider()
        val event = eventRepository.getEvent(person.crn, eventNumber.toString())
        val appearance = courtAppearanceRepository.findByEventIdOrderByAppearanceDateDesc(event.id)
        val erd = event.disposal?.custody?.let { keyDateRepository.getExpectedEndDate(it.id) }
        val ogrsScore = ogrsAssessmentRepository.findFirstByEventIdOrderByAssessmentDateDesc(event.id)?.score
        return CaseDetails(
            person.nomsId,
            person.name(),
            person.dateOfBirth,
            person.gender.description,
            appearance?.let { Appearance(it.appearanceDate, Court(it.court.name)) },
            erd?.let { Sentence(it.date) },
            provider,
            ogrsScore
        )
    }

    fun ResponsibleOfficer.provider(): Provider? {
        val provider = communityManager?.provider ?: prisonManager?.provider
        return provider?.let { Provider(it.code, it.description) }
    }

    fun PersonManager.provider(): Provider = Provider(provider.code, provider.description)
}
