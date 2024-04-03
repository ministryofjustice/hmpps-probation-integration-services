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
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction

@ExtendWith(MockitoExtension::class)
class AuditableServiceTest {

    @Mock
    private lateinit var auditedInteractionService: AuditedInteractionService

    @InjectMocks
    private lateinit var auditedService: AuditedService

    @Test
    fun `audit success`() {
        val result = auditedService.success(4, 4)

        assertThat(result, equalTo(8))
        verify(auditedInteractionService).createAuditedInteraction(
            eq(BusinessInteractionCode.TEST_BI_CODE),
            eq(AuditedInteraction.Parameters(mutableMapOf("field" to "value"))),
            eq(AuditedInteraction.Outcome.SUCCESS),
            any(),
            anyOrNull()
        )
    }

    @Test
    fun `audit failure`() {
        assertThrows<IllegalArgumentException> {
            auditedService.fail()
        }

        verify(auditedInteractionService).createAuditedInteraction(
            eq(BusinessInteractionCode.TEST_BI_CODE),
            eq(AuditedInteraction.Parameters(mutableMapOf("field" to "value"))),
            eq(AuditedInteraction.Outcome.FAIL),
            any(),
            anyOrNull()
        )
    }

    class AuditedService(auditedInteractionService: AuditedInteractionService) :
        AuditableService(auditedInteractionService) {
        fun success(left: Int, right: Int) = audit(BusinessInteractionCode.TEST_BI_CODE) {
            it["field"] = "value"
            left + right
        }

        fun fail(): Nothing = audit(
            BusinessInteractionCode.TEST_BI_CODE,
            params = AuditedInteraction.Parameters(mutableMapOf("field" to "value"))
        ) {
            throw IllegalArgumentException("Something went wrong")
        }
    }
}
