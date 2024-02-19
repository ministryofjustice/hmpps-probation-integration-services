package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.cvl.ActivatedLicence
import uk.gov.justice.digital.hmpps.integrations.cvl.AdditionalLicenceCondition
import uk.gov.justice.digital.hmpps.integrations.cvl.telemetryProperties
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.getByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionCategory.Companion.BESPOKE_CATEGORY_CODE
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionCategory.Companion.STANDARD_CATEGORY_CODE
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ReferenceData.Companion.BESPOKE_SUB_CATEGORY_CODE
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ReferenceData.Companion.STANDARD_SUB_CATEGORY_CODE
import java.time.ZonedDateTime

val STANDARD_PREFIX = """
    |Licence Condition created automatically from the Create and Vary a licence system of to reflect the Standard Licence Condition set as identified below:
""".trimMargin()

val LIMITED_PREFIX = """
    |Licence Condition created automatically from the Create and Vary a licence system
""".trimMargin()

val CONDITION_PREFIX = "$LIMITED_PREFIX of"

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
        val com = personManagerRepository.getByCrn(crn)
        return disposalRepository.findCustodialSentences(crn)
            .flatMap {
                applyLicenceConditions(
                    SentencedCase(com, it, licenceConditionService.findByDisposalId(it.id)),
                    activatedLicence,
                    occurredAt
                )
            }.ifEmpty {
                listOf(ActionResult.Ignored("No Custodial Sentences to apply Licence Conditions"))
            }
    }

    private fun applyLicenceConditions(
        sentencedCase: SentencedCase,
        activatedLicence: ActivatedLicence,
        occurredAt: ZonedDateTime
    ): List<ActionResult> {
        val standardResult = activatedLicence.standardConditions(sentencedCase)
        val additionalResult = activatedLicence.additionalConditions(sentencedCase)
        val bespokeResult = activatedLicence.bespokeConditions(sentencedCase)
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

    private fun ActivatedLicence.standardConditions(
        sentencedCase: SentencedCase
    ): ActionResult? {
        val category = licenceConditionCategoryRepository.getByCode(STANDARD_CATEGORY_CODE)
        val subCategory = referenceDataRepository.getLicenceConditionSubCategory(STANDARD_SUB_CATEGORY_CODE)
        val described = conditions.ap.standard
        return if (
            sentencedCase.licenceConditions.none {
                it.mainCategory.code == category.code && it.subCategory.code == subCategory.code && described.isNotEmpty()
            }
        ) {
            licenceConditionService.createLicenceCondition(
                sentencedCase.sentence,
                startDate,
                category,
                subCategory,
                STANDARD_PREFIX,
                described.joinToString(System.lineSeparator()) { it.description },
                sentencedCase.com
            )
            ActionResult.Success(
                ActionResult.Type.StandardLicenceConditionAdded,
                telemetryProperties(sentencedCase.sentence.event.number)
            )
        } else {
            null
        }
    }

    private fun ActivatedLicence.additionalConditions(
        sentencedCase: SentencedCase
    ): ActionResult? {
        val additions = conditions.ap.additional.mapNotNull { condition ->
            val cvlMapping = when (condition.type) {
                AdditionalLicenceCondition.Type.ELECTRONIC_MONITORING -> cvlMappingRepository.getByCvlCodeAndModifier(
                    condition.code,
                    condition.restrictions?.firstOrNull()?.modifier
                )

                else -> cvlMappingRepository.getByCvlCode(condition.code)
            }
            if (
                sentencedCase.licenceConditions.none {
                    it.mainCategory.code == cvlMapping.mainCategory.code && it.subCategory.code == cvlMapping.subCategory.code
                }
            ) {
                licenceConditionService.createLicenceCondition(
                    sentencedCase.sentence,
                    startDate,
                    cvlMapping.mainCategory,
                    cvlMapping.subCategory,
                    if (cvlMapping.subCategory.isVictimNotes()) LIMITED_PREFIX else CONDITION_PREFIX,
                    if (cvlMapping.subCategory.isVictimNotes()) null else condition.description,
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

    private fun ReferenceData.isVictimNotes(): Boolean = code in ReferenceData.VICTIM_NOTES

    private fun ActivatedLicence.bespokeConditions(
        sentencedCase: SentencedCase
    ): ActionResult? {
        val additions = conditions.ap.bespoke.mapNotNull { condition ->
            val category = licenceConditionCategoryRepository.getByCode(BESPOKE_CATEGORY_CODE)
            val subCategory = referenceDataRepository.getLicenceConditionSubCategory(BESPOKE_SUB_CATEGORY_CODE)
            if (sentencedCase.licenceConditions.none { it.mainCategory.code == category.code && it.subCategory.code == subCategory.code }) {
                licenceConditionService.createLicenceCondition(
                    sentencedCase.sentence,
                    startDate,
                    category,
                    subCategory,
                    CONDITION_PREFIX,
                    condition.description,
                    sentencedCase.com
                )
            } else {
                null
            }
        }
        return if (additions.isNotEmpty()) {
            ActionResult.Success(
                ActionResult.Type.BespokeLicenceConditionAdded,
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
