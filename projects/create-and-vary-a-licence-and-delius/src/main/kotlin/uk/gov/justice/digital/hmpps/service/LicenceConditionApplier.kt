package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.cvl.ActivatedLicence
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
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionCategoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.getByCvlCode
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.getLicenceConditionSubCategory

@Service
class LicenceConditionApplier(
    private val disposalRepository: DisposalRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val licenceConditionRepository: LicenceConditionRepository,
    private val cvlMappingRepository: CvlMappingRepository,
    private val licenceConditionCategoryRepository: LicenceConditionCategoryRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val licenceConditionService: LicenceConditionService
) {
    @Transactional
    fun applyLicenceConditions(crn: String, activatedLicence: ActivatedLicence): List<ActionResult> {
        val com = personManagerRepository.findByPersonCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
        return disposalRepository.findCustodialSentences(crn)
            .flatMap { applyLicenceConditions(it, com, activatedLicence) }
    }

    private fun applyLicenceConditions(
        disposal: Disposal,
        com: PersonManager,
        activatedLicence: ActivatedLicence
    ): List<ActionResult> {
        val existingConditions = licenceConditionRepository.findByDisposalId(disposal.id)
        val standardResult = activatedLicence.standardConditions(disposal, com, existingConditions)
        val additionalResults = activatedLicence.additionalConditions(disposal, com, existingConditions)
        val bespokeResult = activatedLicence.bespokeConditions(disposal, com, existingConditions)
        return listOfNotNull(standardResult, additionalResults, bespokeResult).ifEmpty {
            listOf(
                ActionResult.Success(
                    ActionResult.Type.NoChangeToLicenceConditions,
                    activatedLicence.telemetryProperties(disposal.event.number)
                )
            )
        }
    }

    private fun ActivatedLicence.standardConditions(
        disposal: Disposal,
        com: PersonManager,
        licenceConditions: List<LicenceCondition>
    ): ActionResult? {
        return if (
            licenceConditions.none {
                it.mainCategory.code == STANDARD_CATEGORY_CODE && it.subCategory.code == STANDARD_SUB_CATEGORY_CODE
            }
        ) {
            licenceConditionService.createLicenceCondition(
                disposal,
                releaseDate,
                licenceConditionCategoryRepository.getByCode(STANDARD_CATEGORY_CODE),
                referenceDataRepository.getLicenceConditionSubCategory(STANDARD_SUB_CATEGORY_CODE),
                standardLicenceConditions.joinToString(System.lineSeparator()) { it.description },
                com
            )
            ActionResult.Success(
                ActionResult.Type.StandardLicenceConditionAdded,
                telemetryProperties(disposal.event.number)
            )
        } else {
            null
        }
    }

    private fun ActivatedLicence.additionalConditions(
        disposal: Disposal,
        com: PersonManager,
        licenceConditions: List<LicenceCondition>
    ): ActionResult? {
        val additions = additionalLicenceConditions.mapNotNull { condition ->
            val cvlMapping = cvlMappingRepository.getByCvlCode(condition.code)
            if (
                licenceConditions.none {
                    it.mainCategory.code == cvlMapping.mainCategory.code && it.subCategory.code == cvlMapping.subCategory.code
                }
            ) {
                licenceConditionService.createLicenceCondition(
                    disposal,
                    releaseDate,
                    cvlMapping.mainCategory,
                    cvlMapping.subCategory,
                    condition.description,
                    com
                )
            } else {
                null
            }
        }
        return if (additions.isNotEmpty()) {
            ActionResult.Success(
                ActionResult.Type.AdditionalLicenceConditionsAdded,
                telemetryProperties(disposal.event.number)
            )
        } else {
            null
        }
    }

    private fun ActivatedLicence.bespokeConditions(
        disposal: Disposal,
        com: PersonManager,
        licenceConditions: List<LicenceCondition>
    ): ActionResult? {
        return if (
            licenceConditions.none {
                it.mainCategory.code == BESPOKE_CATEGORY_CODE && it.subCategory.code == BESPOKE_SUB_CATEGORY_CODE
            }
        ) {
            licenceConditionService.createLicenceCondition(
                disposal,
                releaseDate,
                licenceConditionCategoryRepository.getByCode(BESPOKE_CATEGORY_CODE),
                referenceDataRepository.getLicenceConditionSubCategory(BESPOKE_SUB_CATEGORY_CODE),
                bespokeLicenceConditions.joinToString(System.lineSeparator()) { it.description },
                com
            )
            ActionResult.Success(
                ActionResult.Type.BespokeLicenceConditionAdded,
                telemetryProperties(disposal.event.number)
            )
        } else {
            null
        }
    }
}
