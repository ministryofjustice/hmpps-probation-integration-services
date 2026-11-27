package uk.gov.justice.digital.hmpps.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAdjustmentRepository
import uk.gov.justice.digital.hmpps.model.AdjustmentType
import uk.gov.justice.digital.hmpps.service.AdjustmentService.Companion.REFERENCE_PREFIX
import java.util.UUID

class AdjustmentServiceTest {
    private val adjustmentRepository = mock<UnpaidWorkAdjustmentRepository>()
    private val service = AdjustmentService(
        adjustmentRepository = adjustmentRepository,
        unpaidWorkDetailsRepository = mock(),
        referenceDataRepository = mock(),
        userRepository = mock(),
        personRepository = mock(),
        eventRepository = mock(),
        communityPaybackAppointmentsService = mock(),
    )

    @Test
    fun `adjustment responses have no last updated username when user is null`() {
        val reference = UUID.randomUUID()
        val adjustment = UPWGenerator.generateUPWAdjustment(
            upwDetails = UPWGenerator.UPW_DETAILS_ADJUSTMENT,
            adjustmentAmount = 30,
            adjustmentType = AdjustmentType.NEGATIVE.code,
            reference = reference,
        )
        val crn = "X123456"
        val eventNumber = 1
        val pageable = PageRequest.of(0, 10)

        assertThat(adjustment.lastUpdatedByUser).isNull()
        whenever(adjustmentRepository.findByReference(reference))
            .thenReturn(adjustment)
        whenever(adjustmentRepository.findByCrnAndEventNumber(crn, eventNumber.toString()))
            .thenReturn(listOf(adjustment))
        whenever(adjustmentRepository.searchAdjustments(crn, "1", null, null, null, pageable))
            .thenReturn(PageImpl(listOf(adjustment), pageable, 1))

        assertThat(service.getAdjustment(reference).lastUpdatedBy).isNull()
        assertThat(service.getAdjustments(crn, eventNumber).adjustments.single().lastUpdatedBy).isNull()
        assertThat(
            service.searchAdjustments(crn, eventNumber, null, null, null, pageable)
                .content.single().lastUpdatedBy
        ).isNull()
    }
}