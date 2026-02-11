package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.AdditionalOffenceRepository
import uk.gov.justice.digital.hmpps.entity.MainOffenceRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.CodeAndDescription
import uk.gov.justice.digital.hmpps.model.Offence
import uk.gov.justice.digital.hmpps.model.OffenceDetails

@Service
class OffenceService(
    private val mainOffenceRepository: MainOffenceRepository,
    private val additionalOffenceRepository: AdditionalOffenceRepository
) {
    fun getOffences(crn: String, event: Int): OffenceDetails {
        val mainOffence = mainOffenceRepository.findByPersonCrnAndEventEventNumber(crn, event.toString())
            ?: throw NotFoundException("No offences found for CRN $crn and event $event")
        val additionalOffences = additionalOffenceRepository.findAllByEventId(mainOffence.event.id).map {
            Offence(
                it.offenceDate,
                CodeAndDescription(it.offence.mainCategoryCode.trim(), it.offence.mainCategoryDescription),
                CodeAndDescription(it.offence.subCategoryCode.trim(), it.offence.subCategoryDescription)
            )
        }

        return OffenceDetails(
            Offence(
                mainOffence.date,
                CodeAndDescription(
                    mainOffence.offence.mainCategoryCode.trim(),
                    mainOffence.offence.mainCategoryDescription
                ),
                CodeAndDescription(
                    mainOffence.offence.subCategoryCode.trim(),
                    mainOffence.offence.subCategoryDescription
                )
            ), additionalOffences
        )
    }
}