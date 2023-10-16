package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.cvl.ActivatedLicence
import uk.gov.justice.digital.hmpps.integrations.cvl.Describable
import uk.gov.justice.digital.hmpps.integrations.cvl.telemetryProperties
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CvlMapping.Companion.BESPOKE_CATEGORY_CODE
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CvlMapping.Companion.BESPOKE_SUB_CATEGORY_CODE
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CvlMapping.Companion.STANDARD_CATEGORY_CODE
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CvlMapping.Companion.STANDARD_SUB_CATEGORY_CODE
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CvlMappingRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.DisposalRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionCategory
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionCategoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.getByCvlCode
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.getLicenceConditionSubCategory
import java.time.ZonedDateTime

@Service
class LicenceConditionApplier(
    private val disposalRepository: DisposalRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val cvlMappingRepository: CvlMappingRepository,
    private val licenceConditionCategoryRepository: LicenceConditionCategoryRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val licenceConditionService: LicenceConditionService,
    private val contactService: ContactService
) {
    @Transactional
    fun applyLicenceConditions(
        crn: String,
        activatedLicence: ActivatedLicence,
        occurredAt: ZonedDateTime
    ): List<ActionResult> {
        val com = personManagerRepository.findByPersonCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
        return disposalRepository.findCustodialSentences(crn)
            .flatMap {
                applyLicenceConditions(
                    SentencedCase(com, it, licenceConditionService.findByDisposalId(it.id)),
                    activatedLicence,
                    occurredAt
                )
            }
    }

    private fun applyLicenceConditions(
        sentencedCase: SentencedCase,
        activatedLicence: ActivatedLicence,
        occurredAt: ZonedDateTime
    ): List<ActionResult> {
        val standardResult = activatedLicence.groupedConditions(
            sentencedCase,
            licenceConditionCategoryRepository.getByCode(STANDARD_CATEGORY_CODE),
            referenceDataRepository.getLicenceConditionSubCategory(STANDARD_SUB_CATEGORY_CODE),
            activatedLicence.standardLicenceConditions,
            ActionResult.Type.StandardLicenceConditionAdded
        )
        val additionalResult = activatedLicence.additionalConditions(sentencedCase)
        val bespokeResult = activatedLicence.groupedConditions(
            sentencedCase,
            licenceConditionCategoryRepository.getByCode(BESPOKE_CATEGORY_CODE),
            referenceDataRepository.getLicenceConditionSubCategory(BESPOKE_SUB_CATEGORY_CODE),
            activatedLicence.bespokeLicenceConditions,
            ActionResult.Type.BespokeLicenceConditionAdded
        )
        val results = listOfNotNull(standardResult, additionalResult, bespokeResult)
        if (results.isNotEmpty()) {
            contactService.createContact(sentencedCase.sentence, sentencedCase.com, occurredAt)
        }
        return results.ifEmpty {
            listOf(
                ActionResult.Success(
                    ActionResult.Type.NoChangeToLicenceConditions,
                    activatedLicence.telemetryProperties(sentencedCase.sentence.event.number)
                )
            )
        }
    }

    private fun ActivatedLicence.groupedConditions(
        sentencedCase: SentencedCase,
        category: LicenceConditionCategory,
        subCategory: ReferenceData,
        described: List<Describable>,
        successType: ActionResult.Type
    ): ActionResult? {
        return if (
            sentencedCase.licenceConditions.none {
                it.mainCategory.code == category.code && it.subCategory.code == subCategory.code
            }
        ) {
            licenceConditionService.createLicenceCondition(
                sentencedCase.sentence,
                releaseDate,
                category,
                subCategory,
                described.joinToString(System.lineSeparator()) { it.description },
                sentencedCase.com
            )
            ActionResult.Success(
                successType,
                telemetryProperties(sentencedCase.sentence.event.number)
            )
        } else {
            null
        }
    }

    private fun ActivatedLicence.additionalConditions(
        sentencedCase: SentencedCase
    ): ActionResult? {
        val additions = additionalLicenceConditions.mapNotNull { condition ->
            val cvlMapping = cvlMappingRepository.getByCvlCode(condition.code)
            if (
                sentencedCase.licenceConditions.none {
                    it.mainCategory.code == cvlMapping.mainCategory.code && it.subCategory.code == cvlMapping.subCategory.code
                }
            ) {
                licenceConditionService.createLicenceCondition(
                    sentencedCase.sentence,
                    releaseDate,
                    cvlMapping.mainCategory,
                    cvlMapping.subCategory,
                    condition.description,
                    sentencedCase.com
                )
            } else {
                null
            }
        }
        return if (additions.isNotEmpty()) {
            ActionResult.Success(
                ActionResult.Type.AdditionalLicenceConditionsAdded,
                telemetryProperties(sentencedCase.sentence.event.number)
            )
        } else {
            null
        }
    }
}

class SentencedCase(
    val com: PersonManager,
    val sentence: Disposal,
    val licenceConditions: List<LicenceCondition>
)
