package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import uk.gov.justice.digital.hmpps.api.model.sentence.SentenceType
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

class MinimalOrderMappingTest {
    @Test
    fun `minimal order displays entered weeks rather than stored length`() {
        val disposal = disposal(length = 5, entryLength = 20, unit = ReferenceData(1, "W", "Weeks"))

        assertEquals("Custody (20 Weeks)", disposal.toMinimalOrder(SentenceType.CUSTODY).description)
    }

    @Test
    fun `minimal order displays entered years without conversion`() {
        val disposal = disposal(length = 24, entryLength = 2, unit = ReferenceData(1, "Y", "Years"))

        assertEquals("Custody (2 Years)", disposal.toMinimalOrder(SentenceType.CUSTODY).description)
    }

    @Test
    fun `minimal order omits the duration when either entered value is missing`() {
        val missingLength = disposal(length = 12, entryLength = null, unit = ReferenceData(1, "M", "Months"))
        val missingUnit = disposal(length = 12, entryLength = 12, unit = null)

        assertEquals("Custody", missingLength.toMinimalOrder(SentenceType.CUSTODY).description)
        assertEquals("Custody", missingUnit.toMinimalOrder(SentenceType.CUSTODY).description)
    }

    private fun disposal(length: Long, entryLength: Long?, unit: ReferenceData?) = Disposal(
        event = mock(),
        personId = 1,
        date = LocalDate.of(2026, 6, 26),
        length = length,
        entryLength = entryLength,
        type = DisposalType(code = "C", description = "Custody", id = 1),
        lengthUnit = unit,
        active = true,
        softDeleted = false,
        id = 1
    )
}
