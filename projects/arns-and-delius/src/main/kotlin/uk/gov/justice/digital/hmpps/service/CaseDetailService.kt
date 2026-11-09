package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.Event
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.SupervisionPackageRepository
import uk.gov.justice.digital.hmpps.model.ArnsResponse
import uk.gov.justice.digital.hmpps.model.OffenceDetail
import uk.gov.justice.digital.hmpps.model.SupervisionPackageResponse
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import uk.gov.justice.digital.hmpps.repository.getByCrn

@Service
class CaseDetailService(
    private val personRepository: PersonRepository,
    private val supervisionPackageRepository: SupervisionPackageRepository
) {
    fun getCaseDetail(crn: String): ArnsResponse = personRepository.getByCrn(crn).toModel()

    fun getSupervisionPackage(crn: String): List<SupervisionPackageResponse> {
        personRepository.getByCrn(crn)
        val packageEntries = supervisionPackageRepository.findAllByPersonCrn(crn).groupBy { it.disposal }
        return packageEntries.map { (disposal, entries) ->
            val createdDate = entries.minOf { it.supervisionPackageStart }
            val supervisionPackageChangedDate = entries
                .sortedBy { it.supervisionPackageStart }
                .zipWithNext()
                .lastOrNull { (previous, current) ->
                    previous.packageType != current.packageType
                }
                ?.second
                ?.supervisionPackageStart
            SupervisionPackageResponse(
                crn = crn,
                eventId = disposal.event.id,
                createdDate = createdDate,
                supervisionPackageChangedDate = supervisionPackageChangedDate
            )
        }
    }
}

private fun Person.toModel() = ArnsResponse(
    crn = crn,
    dateOfBirth = dateOfBirth,
    gender = gender.description,
    offences = events.map(Event::toModel),
)

private fun Event.toModel() = OffenceDetail(
    mainOffence = mainOffence?.offence?.description,
    sentenceStartDate = disposal?.startDate,
    convictionDate = convictionDate,
)