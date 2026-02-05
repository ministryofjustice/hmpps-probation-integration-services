import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.integrations.delius.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.ReferenceData

class ReferenceDataTest {

    @Test
    fun `should create ReferenceData with correct properties`() {
        val dataset = Dataset("test_code", 1L)
        val refData = ReferenceData(10L, "CODE", "Description", dataset, true)

        assertEquals(10L, refData.id)
        assertEquals("CODE", refData.code)
        assertEquals("Description", refData.description)
        assertEquals(dataset, refData.dataset)
        assertTrue(refData.selectable)
    }

    @Test
    fun `should create Dataset and check equality by code`() {
        val dataset1 = Dataset("address", 1L)
        val dataset2 = Dataset("address", 2L)
        val dataset3 = Dataset("other", 3L)

        assertEquals(dataset1, dataset2)
        assertNotEquals(dataset1, dataset3)
        assertEquals(dataset1.hashCode(), dataset2.hashCode())
    }
}