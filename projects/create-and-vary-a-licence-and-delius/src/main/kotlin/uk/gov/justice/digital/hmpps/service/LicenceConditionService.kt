package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionCategory
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionManager
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime

@Service
class LicenceConditionService(
    private val licenceConditionRepository: LicenceConditionRepository,
    private val licenceConditionManagerRepository: LicenceConditionManagerRepository
) {

    fun findByDisposalId(id: Long) = licenceConditionRepository.findByDisposalId(id)
    fun createLicenceCondition(
        disposal: Disposal,
        startDate: LocalDate,
        category: LicenceConditionCategory,
        subCategory: ReferenceData,
        notes: String,
        com: PersonManager
    ): LicenceCondition {
        val lc = licenceConditionRepository.save(
            LicenceCondition(
                disposal.event.person.id,
                disposal.id,
                startDate,
                category,
                subCategory,
                notes
            )
        )
        licenceConditionManagerRepository.save(
            LicenceConditionManager(
                lc.id,
                startDate.atStartOfDay(EuropeLondon),
                com.provider.id,
                com.team.id,
                com.staff.id
            )
        )
        return lc
    }
}
