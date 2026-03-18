package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.DisposalRepository
import uk.gov.justice.digital.hmpps.entity.DocumentEntity.Companion.cossoBreachNoticeUrn
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.entity.ReferenceDataSet
import uk.gov.justice.digital.hmpps.entity.RequirementRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.CodeAndDescription
import uk.gov.justice.digital.hmpps.model.CossoRequirement
import uk.gov.justice.digital.hmpps.model.RequirementsResponse
import java.util.UUID

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
        val disposals = disposalRepository.findByEventId(eventId)
        val requirements = disposals.flatMap { requirementRepository.getByDisposalId(it.id) }
        val breachReasons = getBreachReasons()
        return RequirementsResponse(
            requirements = requirements.map {
                CossoRequirement(
                    id = it.id,
                    type = CodeAndDescription(it.requirementType.code, it.requirementType.description),
                    subType = CodeAndDescription(it.requirementSubType.code, it.requirementSubType.description),
                )
            },
            breachReasons = breachReasons.map { CodeAndDescription(it.code, it.description) }
        )
    }

    fun getBreachReasons(): List<ReferenceData> =
        referenceDataRepository.findAllByDataSetName(ReferenceDataSet.Code.BREACH_REASON.value)
}