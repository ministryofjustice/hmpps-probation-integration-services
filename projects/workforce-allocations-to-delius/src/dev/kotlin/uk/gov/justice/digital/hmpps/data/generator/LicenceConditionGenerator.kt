package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.LicenceConditionTransfer
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewDisposal
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewLicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewLicenceConditionMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceConditionMainCategory
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceConditionManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import java.time.LocalDate
import java.time.ZonedDateTime

object LicenceConditionGenerator {
    val CASE_VIEW = forCaseView()

    // A condition with no sub-category (subCategory = null)
    val NO_SUB_CATEGORY = forCaseView(
        subCategory = null,
        id = IdGenerator.getAndIncrement()
    )

    // Conditions with fixed IDs for allocation integration tests
    val DEFAULT = generate()
    val NEW = generate(id = 8001L)
    val HISTORIC = generate(id = 8002L)
    val REALLOCATION = generate(id = 8003L)

    fun generate(
        person: Person = PersonGenerator.DEFAULT,
        disposal: Disposal = DisposalGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement(),
        active: Boolean = true
    ) = LicenceCondition(
        id = id,
        person = person,
        disposal = disposal,
        mainCategory = LicenceConditionAllocationMainCategoryGenerator.DEFAULT,
        subCategory = ReferenceDataGenerator.LICENCE_CONDITION_SUB_CATEGORY,
        startDate = LocalDate.now().minusDays(30),
        commenceDate = LocalDate.now().minusDays(20),
        terminationDate = null,
        active = active,
    )

    private fun forCaseView(
        personId: Long = PersonGenerator.CASE_VIEW.id,
        disposal: CaseViewDisposal = DisposalGenerator.CASE_VIEW,
        id: Long = IdGenerator.getAndIncrement(),
        active: Boolean = true,
        subCategory: ReferenceData? = ReferenceDataGenerator.LICENCE_CONDITION_SUB_CATEGORY
    ) = CaseViewLicenceCondition(
        id,
        personId,
        disposal,
        LicenceConditionMainCategoryGenerator.CASE_VIEW,
        subCategory,
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

/** Generates LicenceConditionMainCategory instances - maps to r_lic_cond_type_main_cat (same table as CaseViewLicenceConditionMainCategory). */
object LicenceConditionAllocationMainCategoryGenerator {
    val DEFAULT = generate("LC_ALLOC", "Main Category for Licence Condition Allocation")

    fun generate(
        code: String,
        description: String = code,
        id: Long = IdGenerator.getAndIncrement()
    ) = LicenceConditionMainCategory(id, code, description)
}

object LicenceConditionManagerGenerator : ManagerGenerator {
    var DEFAULT = generate(startDateTime = ZonedDateTime.now().minusMonths(1))
    lateinit var NEW: LicenceConditionManager
    lateinit var HISTORIC: LicenceConditionManager
    lateinit var REALLOCATION: LicenceConditionManager

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

object LicenceConditionTransferGenerator {
    fun generate(
        licenceConditionId: Long,
        statusId: Long = ReferenceDataGenerator.PENDING_TRANSFER.id,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = LicenceConditionTransfer(id, licenceConditionId, statusId, softDeleted)
}
