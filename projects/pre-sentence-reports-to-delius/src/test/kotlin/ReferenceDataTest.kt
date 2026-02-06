import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.entity.ReferenceData

class ReferenceDataTest {

    @Test
    fun `should create ReferenceData with correct properties`() {
        val refData = ReferenceData(10L, "CODE", "Description", true)
        assertEquals(10L, refData.id)
        assertEquals("CODE", refData.code)
        assertEquals("Description", refData.description)
        assertTrue(refData.selectable)
    }
}