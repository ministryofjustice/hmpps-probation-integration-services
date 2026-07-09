package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.model.AdditionalSentence
import uk.gov.justice.digital.hmpps.model.CodeAndDescription
import java.util.*

@Service
class OffenceService(
    val documentRepository: DocumentRepository,
    private val mainOffenceRepository: MainOffenceRepository,
    private val additionalOffenceRepository: AdditionalOffenceRepository,
    private val courtAppearanceRepository: CourtAppearanceRepository,
    private val disposalRepository: DisposalRepository,
    private val requirementRepository: RequirementRepository,
    private val additionalSentenceRepository: AdditionalSentenceRepository
) {
    companion object {
        const val SENTENCE_APPEARANCE_TYPE_CODE = "S"
    }

    fun getOffenceDetails(uuid: String): OffenceDetails {

        val eventId = documentRepository.findEventIdFromDocument(
            DocumentEntity.cossoBreachNoticeUrn(UUID.fromString(uuid))
        )
            ?: throw NotFoundException("DocumentEntity", "UUID", uuid)
        val mainOffence = mainOffenceRepository.findByEventId(eventId)?.offence
            ?: throw NotFoundException("Offence", "eventId", eventId)
        val additionalOffences = additionalOffenceRepository.findAllByEventId(eventId)
            .map { CodeAndDescription(it.offence.subCategoryCode, it.offence.subCategoryDescription) }
        val courtAppearance = courtAppearanceRepository.findSentencingAppearance(eventId).firstOrNull()
            ?: throw NotFoundException("CourtAppearance", "eventId", eventId)
        val disposal = disposalRepository.findFirstByEventIdOrderByDisposalDate(eventId)
            ?: throw NotFoundException("Disposal", "eventId", eventId)
        val additionalSentences = additionalSentenceRepository.findAllByEventId(eventId)
        return OffenceDetails(
            mainOffence = CodeAndDescription(mainOffence.subCategoryCode, mainOffence.subCategoryDescription),
            additionalOffences = additionalOffences,
            sentencingCourt = courtAppearance.court.courtName,
            sentenceDate = disposal.disposalDate,
            sentenceImposed = CodeAndDescription(courtAppearance.outcome.code, courtAppearance.outcome.description),
            suspendedCustodyLength = SuspendedCustodyLength(
                length = disposal.length2,
                units = disposal.length2Units?.description
            ).takeIf { disposal.disposalType.isSuspendedSentenceOrder },
            requirementsImposed = getRequirements(disposal.id),
            sentence = getSentence(disposal),
            additionalSentences = getAdditionalSentences(additionalSentences)
        )
    }

    fun getRequirements(disposalId: Long): List<Requirement> {
        return requirementRepository.getByDisposalId(disposalId)
            .map {
                Requirement(
                    id = it.id,
                    startDate = it.startDate,
                    mainCategory = it.requirementType?.description,
                    length = it.length,
                    lengthUnit = it.requirementType?.units?.description,
                    subCategory = it.requirementSubType?.description,
                    secondaryLength = it.length2,
                    secondaryLengthUnit = it.requirementType?.length2Units?.description
                )
            }
    }

    fun getSentence(disposal: Disposal): Sentence {
        return Sentence(
            length = disposal.length,
            lengthUnits = disposal.lengthUnits?.description,
            type = disposal.disposalType.description,
            secondLength = disposal.length2,
            secondLengthUnits = disposal.length2Units?.description
        )
    }

    fun getAdditionalSentences(entities: List<uk.gov.justice.digital.hmpps.entity.AdditionalSentence>): List<AdditionalSentence> {
        return entities.map {
            AdditionalSentence(
                length = it.length,
                amount = it.amount,
                notes = it.notes,
                type = CodeAndDescription(it.type.code, it.type.description),
                units = it.units?.let { u -> CodeAndDescription(u.code, u.description) }
            )
        }
    }
}