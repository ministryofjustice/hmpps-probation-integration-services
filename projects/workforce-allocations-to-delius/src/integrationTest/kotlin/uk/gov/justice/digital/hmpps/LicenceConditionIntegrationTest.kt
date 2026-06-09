package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.CaseView
import uk.gov.justice.digital.hmpps.api.model.CvLicenceCondition
import uk.gov.justice.digital.hmpps.api.model.ReallocationCaseView
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionMainCategoryGenerator
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionTransferGenerator
import uk.gov.justice.digital.hmpps.data.generator.ManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceConditionManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceConditionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceConditionTransferRepository
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.ZonedDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class LicenceConditionIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val licenceConditionRepository: LicenceConditionRepository,
    private val licenceConditionManagerRepository: LicenceConditionManagerRepository,
    private val licenceConditionTransferRepository: LicenceConditionTransferRepository,
) {
    @Test
    fun `case view includes licence condition with sub-category`() {
        val person = PersonGenerator.CASE_VIEW
        val eventNumber = "10"

        val response = mockMvc.get("/allocation-demand/${person.crn}/$eventNumber/case-view") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CaseView>()

        assertThat(
            response.licenceConditions,
            hasItems(
                CvLicenceCondition(
                    LicenceConditionMainCategoryGenerator.CASE_VIEW.description,
                    ReferenceDataGenerator.LICENCE_CONDITION_SUB_CATEGORY.description
                )
            )
        )
    }

    @Test
    fun `case view includes licence condition without sub-category`() {
        val person = PersonGenerator.CASE_VIEW
        val eventNumber = "10"

        val response = mockMvc.get("/allocation-demand/${person.crn}/$eventNumber/case-view") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CaseView>()

        assertThat(
            response.licenceConditions,
            hasItems(
                CvLicenceCondition(LicenceConditionMainCategoryGenerator.CASE_VIEW.description, null)
            )
        )
    }

    @Test
    fun `reallocation case view includes licence condition without sub-category`() {
        val person = PersonGenerator.CASE_VIEW

        val response = mockMvc.get("/reallocation/${person.crn}/case-view") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<ReallocationCaseView>()

        val conditions = response.activeEvents.first().licenceConditions
        assertThat(
            conditions,
            hasItems(
                CvLicenceCondition(LicenceConditionMainCategoryGenerator.CASE_VIEW.description, null)
            )
        )
    }

    @Test
    fun `countPendingTransfers returns zero when no transfers exist`() {
        val count = licenceConditionRepository.countPendingTransfers(LicenceConditionGenerator.NEW.id)
        assertThat(count, equalTo(0))
    }

    @Test
    fun `countPendingTransfers returns one when a pending transfer exists`() {
        val transfer = licenceConditionTransferRepository.save(
            LicenceConditionTransferGenerator.generate(
                licenceConditionId = LicenceConditionGenerator.NEW.id,
                statusId = ReferenceDataGenerator.PENDING_TRANSFER.id
            )
        )
        try {
            val count = licenceConditionRepository.countPendingTransfers(LicenceConditionGenerator.NEW.id)
            assertThat(count, equalTo(1))
        } finally {
            licenceConditionTransferRepository.delete(transfer)
        }
    }

    @Test
    fun `countPendingTransfers does not count soft-deleted transfers`() {
        val softDeletedTransfer = licenceConditionTransferRepository.save(
            LicenceConditionTransferGenerator.generate(
                licenceConditionId = LicenceConditionGenerator.NEW.id,
                statusId = ReferenceDataGenerator.PENDING_TRANSFER.id,
                softDeleted = true
            )
        )
        try {
            val count = licenceConditionRepository.countPendingTransfers(LicenceConditionGenerator.NEW.id)
            assertThat(count, equalTo(0))
        } finally {
            licenceConditionTransferRepository.delete(softDeletedTransfer)
        }
    }

    @Test
    fun `findActiveManagerAtDate returns active manager within its date range`() {
        val manager = licenceConditionManagerRepository.findActiveManagerAtDate(
            LicenceConditionGenerator.NEW.id,
            ManagerGenerator.START_DATE_TIME.plusDays(1)
        )
        assertThat(manager?.licenceConditionId, equalTo(LicenceConditionGenerator.NEW.id))
    }

    @Test
    fun `findActiveManagerAtDate returns null before the manager start date`() {
        val manager = licenceConditionManagerRepository.findActiveManagerAtDate(
            LicenceConditionGenerator.NEW.id,
            ManagerGenerator.START_DATE_TIME.minusDays(1)
        )
        assertNull(manager)
    }

    @Test
    fun `findActiveManagerAtDate returns null for an unknown licence condition id`() {
        val manager = licenceConditionManagerRepository.findActiveManagerAtDate(
            Long.MAX_VALUE,
            ZonedDateTime.now()
        )
        assertNull(manager)
    }

    @Test
    fun `findActiveManagerAtDate returns null after manager end date`() {
        val endDate = ManagerGenerator.START_DATE_TIME.plusDays(5)
        val existing = licenceConditionManagerRepository.findByIdOrNull(LicenceConditionManagerGenerator.HISTORIC.id)!!
        licenceConditionManagerRepository.save(existing.apply { this.endDate = endDate })

        // Active one day before the end date
        val activeManager = licenceConditionManagerRepository.findActiveManagerAtDate(
            LicenceConditionGenerator.HISTORIC.id,
            endDate.minusDays(1)
        )
        assertThat(activeManager?.licenceConditionId, equalTo(LicenceConditionGenerator.HISTORIC.id))

        // Null at the end date (query uses strict less-than: endDate > dateTime)
        val expiredManager = licenceConditionManagerRepository.findActiveManagerAtDate(
            LicenceConditionGenerator.HISTORIC.id,
            endDate
        )
        assertNull(expiredManager)
    }
}

