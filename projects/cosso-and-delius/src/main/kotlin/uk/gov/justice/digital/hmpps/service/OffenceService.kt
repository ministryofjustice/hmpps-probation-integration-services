package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.AdditionalOffenceRepository
import uk.gov.justice.digital.hmpps.entity.CourtAppearanceRepository
import uk.gov.justice.digital.hmpps.entity.Disposal
import uk.gov.justice.digital.hmpps.entity.DisposalRepository
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.entity.MainOffenceRepository
import uk.gov.justice.digital.hmpps.entity.RequirementRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.CodedDescription
import uk.gov.justice.digital.hmpps.model.OffenceDetails
import uk.gov.justice.digital.hmpps.model.Requirement
import uk.gov.justice.digital.hmpps.model.Sentence

@Service
class OffenceService(
    val documentRepository: DocumentRepository,
    private val mainOffenceRepository: MainOffenceRepository,
    private val additionalOffenceRepository: AdditionalOffenceRepository,
    private val courtAppearanceRepository: CourtAppearanceRepository,
    private val disposalRepository: DisposalRepository,
    private val requirementRepository: RequirementRepository
) {
    companion object {
        const val SENTENCE_APPEARANCE_TYPE_CODE = "S"
    }

    fun getOffenceDetails(uuid: String): OffenceDetails {

        val eventId = documentRepository.getByUuid(uuid).primaryKeyId
        val mainOffence = mainOffenceRepository.findByEventId(eventId)?.offence
            ?: throw NotFoundException("Offence", "eventId", eventId)
        val additionalOffences = additionalOffenceRepository.findAllByEventId(eventId)
            .map { CodedDescription(it.offence.mainCategoryCode, it.offence.mainCategoryDescription) }
        val courtAppearance = courtAppearanceRepository.findSentencingAppearance(eventId).firstOrNull()
            ?: throw NotFoundException("CourtAppearance", "eventId", eventId)
        val disposal = disposalRepository.findByEventId(eventId).firstOrNull { it.activeFlag }
            ?: throw NotFoundException("Disposal", "eventId", eventId)
        return OffenceDetails(
            mainOffence = CodedDescription(mainOffence.mainCategoryCode, mainOffence.mainCategoryDescription),
            additionalOffences = additionalOffences,
            sentencingCourt = courtAppearance.court.courtName,
            sentenceDate = disposal.disposalDate,
            sentenceImposed = CodedDescription(courtAppearance.outcome.code, courtAppearance.outcome.description),
            requirementsImposed = getRequirements(disposal.id),
            sentence = getSentence(disposal)
        )
    }

    fun getRequirements(disposalId: Long): List<Requirement> {
        return requirementRepository.getByDisposalId(disposalId)
            .map {
                Requirement(
                    id = it.id,
                    startDate = it.startDate,
                    mainCategory = it.requirementType.description,
                    length = it.length,
                    lengthUnit = it.requirementType.units.description,
                    subCategory = it.requirementSubType.description,
                    secondaryLength = it.length2,
                    secondaryLengthUnit = it.requirementType.length2Units.description
                )
            }
    }

    fun getSentence(disposal: Disposal): Sentence {
        return Sentence(
            length = disposal.length,
            lengthUnits = disposal.lengthUnits.description,
            type = disposal.disposalType.disposalTypeDescription,
            secondLength = disposal.length2,
            secondLengthUnits = disposal.length2Units.description
        )
    }
}