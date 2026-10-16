package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

internal class SentenceServiceTest {

    private val service = SentenceService(
        eventRepository = mock(),
        personRepository = mock(),
        contactRepository = mock(),
        unpaidWorkAppointmentRepository = mock(),
    )

    @Test
    fun `apportionHours assigns available hours to matching subtype code`() {
        val apportionedHours = service.apportionHours(
            upwSubTypes = listOf("UPW" to 10),
            totalUPWHours = 6,
        )

        assertThat(apportionedHours, equalTo(mapOf("UPW" to 6)))
    }

    @Test
    fun `apportionHours caps completed hours at requirement length`() {
        val apportionedHours = service.apportionHours(
            upwSubTypes = listOf("UPW" to 10),
            totalUPWHours = 12,
        )

        assertThat(apportionedHours, equalTo(mapOf("UPW" to 10)))
    }

    @Test
    fun `apportionHours allocates hours across multiple requirements in order`() {
        val apportionedHours = service.apportionHours(
            upwSubTypes = listOf("W03" to 8, "UPW" to 10, "W07" to 6),
            totalUPWHours = 14,
        )

        assertThat(apportionedHours, equalTo(mapOf("UPW" to 10, "W03" to 4)))
    }

    @Test
    fun `apportionHours sums allocations for repeated subtype codes`() {
        val apportionedHours = service.apportionHours(
            upwSubTypes = listOf("UPW" to 3, "UPW" to 4, "W03" to 5),
            totalUPWHours = 8,
        )

        assertThat(apportionedHours, equalTo(mapOf("UPW" to 7, "W03" to 1)))
    }

    @Test
    fun `apportionHours deducts W01 then W03 then W07 regardless of input order`() {
        val apportionedHours = service.apportionHours(
            upwSubTypes = listOf("W07" to 5, "W03" to 4, "W01" to 6),
            totalUPWHours = 8,
        )

        assertThat(apportionedHours, equalTo(mapOf("W01" to 6, "W03" to 2)))
    }

    @Test
    fun `apportionHours treats W06 as concurrent to sequential deductions`() {
        val apportionedHours = service.apportionHours(
            upwSubTypes = listOf("W06" to 4, "W03" to 8, "UPW" to 10),
            totalUPWHours = 12,
        )

        assertThat(apportionedHours, equalTo(mapOf("UPW" to 10, "W03" to 2, "W06" to 4)))
    }
}


