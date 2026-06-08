package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewDisposal
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewLicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewLicenceConditionMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceConditionManager
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import java.time.ZonedDateTime

object LicenceConditionGenerator {
    val CASE_VIEW = forCaseView()

    private fun forCaseView(
        personId: Long = PersonGenerator.CASE_VIEW.id,
        disposal: CaseViewDisposal = DisposalGenerator.CASE_VIEW,
        id: Long = IdGenerator.getAndIncrement(),
        active: Boolean = true
    ) = CaseViewLicenceCondition(
        id,
        personId,
        disposal,
        LicenceConditionMainCategoryGenerator.CASE_VIEW,
        ReferenceDataGenerator.LICENCE_CONDITION_SUB_CATEGORY,
        null,
        listOf(),
        active
    )
}

object LicenceConditionMainCategoryGenerator {
    val CASE_VIEW = generate("LC_CASE_VIEW", "Main Category for Licence Condition CV")

    fun generate(
        code: String,
        description: String = code,
        id: Long = IdGenerator.getAndIncrement()
    ) = CaseViewLicenceConditionMainCategory(id, code, description)
}

object LicenceConditionManagerGenerator : ManagerGenerator {
    fun generate(
        licenceConditionId: Long = LicenceConditionGenerator.CASE_VIEW.id,
        transferReasonId: Long = TransferReasonGenerator.COMPONENT.id,
        provider: Provider = ProviderGenerator.DEFAULT,
        team: Team = TeamGenerator.DEFAULT,
        staff: Staff = StaffGenerator.DEFAULT,
        startDateTime: ZonedDateTime = ZonedDateTime.now(),
        endDateTime: ZonedDateTime? = null,
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
        lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now(),
        createdUserId: Long = UserGenerator.AUDIT_USER.id,
        lastModifiedUserId: Long = UserGenerator.AUDIT_USER.id,
        allocationReason: ReferenceData = ReferenceDataGenerator.REALLOCATION_RM_ALLOCATION,
        version: Long = 0
    ) = LicenceConditionManager(
        licenceConditionId,
        transferReasonId
    ).build(
        provider,
        team,
        staff,
        startDateTime,
        endDateTime,
        createdDateTime,
        lastModifiedDateTime,
        createdUserId,
        lastModifiedUserId,
        allocationReason,
        version
    ) as LicenceConditionManager
}

