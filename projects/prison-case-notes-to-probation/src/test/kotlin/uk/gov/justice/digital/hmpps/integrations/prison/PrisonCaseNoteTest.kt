package uk.gov.justice.digital.hmpps.integrations.prison

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.integrations.delius.model.StaffName
import java.time.ZonedDateTime

class PrisonCaseNoteTest {
    @Test
    fun `get staff name test`() {
        val caseNote =
            PrisonCaseNote(
                "1",
                1L,
                "1",
                "type",
                "subType",
                creationDateTime = ZonedDateTime.now(),
                occurrenceDateTime = ZonedDateTime.now(),
                authorName = "Smith,Bob",
                text = "",
                amendments = listOf(),
            )

        val staffName = StaffName("Bob", "Smith")
        assertThat(caseNote.getStaffName(), equalTo(staffName))
    }
}
