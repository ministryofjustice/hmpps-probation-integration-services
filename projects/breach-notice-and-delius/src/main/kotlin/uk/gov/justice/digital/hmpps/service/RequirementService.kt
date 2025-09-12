package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.integrations.delius.Document.Companion.breachNoticeUrn
import uk.gov.justice.digital.hmpps.model.RequirementResponse
import java.util.*

@Service
class RequirementService(
    private val documentRepository: DocumentRepository,
    private val disposalRepository: DisposalRepository,
    private val requirementRepository: RequirementRepository,
    private val pssRequirementRepository: PssRequirementRepository,
    private val referenceDataRepository: ReferenceDataRepository,
) {
    fun getRequirements(breachNoticeId: UUID): RequirementResponse {
        val eventId = documentRepository.eventId(breachNoticeUrn(breachNoticeId))
        val disposal = requireNotNull(disposalRepository.getByEventId(eventId)) { "Event is not sentenced" }
        return RequirementResponse(
            requirementRepository.findAllByDisposalId(disposal.id).map { it.toModel() } +
                pssRequirementRepository.findAllByCustodyDisposalId(disposal.id).map { it.toModel() },
            referenceDataRepository.findByDatasetCodeAndSelectableTrue(Dataset.BREACH_REASON).codedDescriptions()
        )
    }
}