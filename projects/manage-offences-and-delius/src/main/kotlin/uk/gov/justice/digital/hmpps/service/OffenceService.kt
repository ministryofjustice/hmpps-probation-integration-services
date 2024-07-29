package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.client.Offence
import uk.gov.justice.digital.hmpps.entity.DetailedOffence
import uk.gov.justice.digital.hmpps.entity.OffenceRepository
import uk.gov.justice.digital.hmpps.entity.ReferenceOffence
import uk.gov.justice.digital.hmpps.entity.getByCode
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.repository.DetailedOffenceRepository
import uk.gov.justice.digital.hmpps.repository.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.repository.findCourtCategory

@Service
@Transactional
class OffenceService(
    private val detailedOffenceRepository: DetailedOffenceRepository,
    private val offenceRepository: OffenceRepository,
    private val referenceDataRepository: ReferenceDataRepository
) {
    fun createOffence(offence: Offence): Boolean {
        mergeDetailedOffence(offence)
        val isNew = createReferenceOffence(offence)
        return isNew
    }

    private fun mergeDetailedOffence(offence: Offence) {
        val existingEntity = detailedOffenceRepository.findByCode(offence.code)
        detailedOffenceRepository.save(existingEntity.mergeWith(offence.toDetailedOffence()))
    }

    private fun createReferenceOffence(offence: Offence): Boolean {
        val homeOfficeCode = offence.homeOfficeCode
        return if (homeOfficeCode != null && offenceRepository.findByCode(homeOfficeCode) == null) {
            val highLevelOffence = offenceRepository.getByCode(offence.highLevelCode!!)
            offenceRepository.save(offence.toReferenceOffence(highLevelOffence))
            true
        } else false // entity already exists, don't update it
    }

    private fun Offence.toDetailedOffence() = DetailedOffence(
        code = code,
        description = description,
        startDate = startDate,
        endDate = endDate,
        homeOfficeCode = homeOfficeStatsCode,
        homeOfficeDescription = homeOfficeDescription,
        legislation = legislation,
        category = referenceDataRepository.findCourtCategory(offenceType)
            ?: throw NotFoundException("Court category", "code", offenceType),
        schedule15ViolentOffence = schedule15ViolentOffence,
        schedule15SexualOffence = schedule15SexualOffence
    )

    private fun Offence.toReferenceOffence(highLevelOffence: ReferenceOffence) = ReferenceOffence(
        code = homeOfficeCode!!,
        description = "$homeOfficeDescription - $homeOfficeCode",
        mainCategoryCode = mainCategoryCode!!,
        selectable = false,
        mainCategoryDescription = highLevelOffence.description.take(200),
        mainCategoryAbbreviation = highLevelOffence.description.take(50),
        ogrsOffenceCategoryId = highLevelOffence.ogrsOffenceCategoryId,
        subCategoryCode = subCategoryCode!!,
        subCategoryDescription = homeOfficeDescription!!.take(200),
        form20Code = highLevelOffence.form20Code,
        childAbduction = null,
        schedule15ViolentOffence = schedule15ViolentOffence,
        schedule15SexualOffence = schedule15SexualOffence
    )

    private fun DetailedOffence?.mergeWith(newEntity: DetailedOffence) = this?.apply {
        code = newEntity.code
        description = newEntity.description
        startDate = newEntity.startDate
        endDate = newEntity.endDate
        homeOfficeCode = newEntity.homeOfficeCode
        homeOfficeDescription = newEntity.homeOfficeDescription
        legislation = newEntity.legislation
        category = newEntity.category
    } ?: newEntity
}