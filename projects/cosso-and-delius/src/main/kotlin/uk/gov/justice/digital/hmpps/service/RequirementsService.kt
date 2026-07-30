package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.entity.DocumentEntity.Companion.cossoBreachNoticeUrn
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.CodeAndDescription
import uk.gov.justice.digital.hmpps.model.CossoRequirement
import uk.gov.justice.digital.hmpps.model.RequirementsResponse
import java.util.*

@Service
class RequirementsService(
    private val documentRepository: DocumentRepository,
    private val requirementRepository: RequirementRepository,
    private val disposalRepository: DisposalRepository,
    private val referenceDataRepository: ReferenceDataRepository,
) {
    fun getRequirements(breachNoticeId: String): RequirementsResponse {
        val eventId = documentRepository.findEventIdFromDocument(cossoBreachNoticeUrn(UUID.fromString(breachNoticeId)))
            ?: throw NotFoundException("DocumentEntity", "breachNoticeId", breachNoticeId)
        val disposals = disposalRepository.findByEventId(eventId).map { it.id }
        val requirements = requirementRepository.findAllByDisposalIdIn(disposals)
        val breachReasons = getBreachReasons()
        return RequirementsResponse(
            requirements = requirements.map {
                CossoRequirement(
                    id = it.id,
                    startDate = it.startDate,
                    mainCategory = it.requirementType?.description,
                    length = it.length,
                    lengthUnit = it.requirementType?.units?.description,
                    subCategory = it.requirementSubType?.description,
                    secondaryLength = it.length2,
                    secondaryLengthUnit = it.requirementType?.length2Units?.description
                )
            },
            breachReasons = breachReasons.map { CodeAndDescription(it.code, it.description) }
        )
    }

    fun getBreachReasons(): List<ReferenceData> =
        referenceDataRepository.findAllByDataSetName(ReferenceDataSet.Code.BREACH_REASON.value).filter { it.selectable }
}