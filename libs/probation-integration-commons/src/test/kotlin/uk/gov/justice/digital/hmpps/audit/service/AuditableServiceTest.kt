package uk.gov.justice.digital.hmpps.audit.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.audit.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode

@ExtendWith(MockitoExtension::class)
class AuditableServiceTest {

    @Mock
    private lateinit var auditedInteractionService: AuditedInteractionService

    @InjectMocks
    private lateinit var auditedService: AuditedService

    @Test
    fun `audit success`() {
        val result = auditedService.audit(BusinessInteractionCode.TEST_BI_CODE, AuditedInteraction.Parameters()) {
            4 + 4
        }

        assertThat(result, equalTo(8))
        verify(auditedInteractionService).createAuditedInteraction(
            eq(BusinessInteractionCode.TEST_BI_CODE),
            any(),
            eq(AuditedInteraction.Outcome.SUCCESS)
        )
    }

    @Test
    fun `audit failure`() {
        assertThrows<RuntimeException> {
            auditedService.audit(BusinessInteractionCode.TEST_BI_CODE, AuditedInteraction.Parameters()) {
                throw RuntimeException("something went wrong")
            }
        }

        verify(auditedInteractionService).createAuditedInteraction(
            eq(BusinessInteractionCode.TEST_BI_CODE),
            any(),
            eq(AuditedInteraction.Outcome.FAIL)
        )
    }

    class AuditedService(auditedInteractionService: AuditedInteractionService) :
        AuditableService(auditedInteractionService)
}