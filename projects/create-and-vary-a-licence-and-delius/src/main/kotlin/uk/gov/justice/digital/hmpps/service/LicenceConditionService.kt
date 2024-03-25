package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import java.time.LocalDate

@Service
class LicenceConditionService(
    private val licenceConditionRepository: LicenceConditionRepository,
    private val licenceConditionManagerRepository: LicenceConditionManagerRepository,
    private val transferReasonRepository: TransferReasonRepository,
    private val referenceDataRepository: ReferenceDataRepository
) {
    fun findByDisposalId(id: Long) = licenceConditionRepository.findByDisposalId(id)
    fun createLicenceCondition(
        disposal: Disposal,
        startDate: LocalDate,
        category: LicenceConditionCategory,
        subCategory: ReferenceData,
        prefix: String,
        cvlText: String?,
        com: PersonManager
    ): LicenceCondition {
        val notes = cvlText?.let { prefix + System.lineSeparator() + it } ?: prefix
        val lc = licenceConditionRepository.save(
            LicenceCondition(
                disposal.event.person.id,
                disposal.id,
                startDate,
                category,
                subCategory,
                notes,
                cvlText
            )
        )
        licenceConditionManagerRepository.save(
            LicenceConditionManager(
                lc.id,
                startDate.atStartOfDay(EuropeLondon),
                com.provider.id,
                com.team.id,
                com.staff.id,
                transferReasonRepository.getByCode(TransferReason.DEFAULT_CODE),
                referenceDataRepository.getLmAllocationReason(ReferenceData.INITIAL_ALLOCATION_CODE)
            )
        )
        return lc
    }
}
