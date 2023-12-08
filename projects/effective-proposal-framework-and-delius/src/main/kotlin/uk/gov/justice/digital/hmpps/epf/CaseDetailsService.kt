package uk.gov.justice.digital.hmpps.epf

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.epf.entity.CourtAppearanceRepository
import uk.gov.justice.digital.hmpps.epf.entity.EventRepository
import uk.gov.justice.digital.hmpps.epf.entity.OgrsAssessmentRepository
import uk.gov.justice.digital.hmpps.epf.entity.PersonRepository
import uk.gov.justice.digital.hmpps.epf.entity.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.epf.entity.ResponsibleOfficerRepository
import uk.gov.justice.digital.hmpps.epf.entity.getEvent
import uk.gov.justice.digital.hmpps.epf.entity.getPerson

@Service
class CaseDetailsService(
    private val personRepository: PersonRepository,
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
    private val courtAppearanceRepository: CourtAppearanceRepository,
    private val eventRepository: EventRepository,
    private val ogrsAssessmentRepository: OgrsAssessmentRepository,
) {
    fun caseDetails(
        crn: String,
        eventNumber: Int,
    ): CaseDetails {
        val person = personRepository.getPerson(crn)
        val responsibleOfficer = responsibleOfficerRepository.findByPersonIdAndEndDateIsNull(person.id)
        val event = eventRepository.getEvent(person.crn, eventNumber.toString())
        val courtName = courtAppearanceRepository.findMostRecentCourtNameByEventId(event.id)
        val ogrsScore = ogrsAssessmentRepository.findFirstByEventIdOrderByAssessmentDateDesc(event.id)?.score
        return CaseDetails(
            person.nomsId,
            person.name(),
            person.dateOfBirth,
            person.gender.description,
            event.disposal?.date?.let {
                Sentence(it, Court(courtName), event.firstReleaseDate)
            },
            responsibleOfficer?.provider(),
            ogrsScore,
        )
    }

    fun ResponsibleOfficer.provider(): Provider {
        val provider = communityManager?.provider ?: prisonManager?.provider
        return Provider(provider!!.code, provider.description)
    }
}
