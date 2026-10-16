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

        return OffenceDetails(
            mainOffence = Offence(
                date = mainOffence.date,
                code = mainOffence.offence.code,
                description = mainOffence.offence.description,
                mainCategory = CodeAndDescription(
                    mainOffence.offence.mainCategoryCode,
                    mainOffence.offence.mainCategoryDescription
                ),
                subCategory = CodeAndDescription(
                    mainOffence.offence.subCategoryCode,
                    mainOffence.offence.subCategoryDescription
                )
            ),
            additionalOffences = additionalOffenceRepository.findAllByEventId(mainOffence.event.id).map {
                Offence(
                    date = it.date,
                    code = it.offence.code,
                    description = it.offence.description,
                    mainCategory = CodeAndDescription(it.offence.mainCategoryCode, it.offence.mainCategoryDescription),
                    subCategory = CodeAndDescription(it.offence.subCategoryCode, it.offence.subCategoryDescription)
                )
            }
        )
    }
}