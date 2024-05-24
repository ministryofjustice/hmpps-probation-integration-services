package uk.gov.justice.digital.hmpps.integrations.prison

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.integrations.delius.model.BcstPathway
import uk.gov.justice.digital.hmpps.model.StaffName
import java.time.ZonedDateTime

class PrisonCaseNoteTest {
    @Test
    fun `get staff name test`() {
        val caseNote = PrisonCaseNote(
            "1",
            1L,
            "1",
            "type",
            "subType",
            creationDateTime = ZonedDateTime.now(),
            occurrenceDateTime = ZonedDateTime.now(),
            authorName = "Smith,Bob",
            text = "",
            amendments = listOf()
        )

        val staffName = StaffName("Bob", "Smith")
        assertThat(caseNote.getStaffName(), equalTo(staffName))
    }

    @ParameterizedTest
    @MethodSource("summaryFirstLines")
    fun `bcst pathway parsed correctly`(firstLine: String, pathway: BcstPathway) {
        assertThat(BcstPathway.from(firstLine), equalTo(pathway))
    }

    companion object {
        @JvmStatic
        fun summaryFirstLines() = listOf(
            of("Case note summary from Accommodation BCST2 report", BcstPathway.ACCOMMODATION),
            of(
                "Case note summary from Attitudes, thinking and behaviour BCST2 report",
                BcstPathway.ATTITUDES_THINKING_AND_BEHAVIOUR
            ),
            of(
                "Case note summary from Children, families and communities BCST2 report",
                BcstPathway.CHILDREN_FAMILIES_AND_COMMUNITY
            ),
            of("Case note summary from Drugs and alcohol Pre-release report", BcstPathway.DRUGS_AND_ALCOHOL),
            of(
                "Case note summary from Education, skills and work Pre-release report",
                BcstPathway.EDUCATION_SKILLS_AND_WORK
            ),
            of("Case note summary from Finance and ID Pre-release report", BcstPathway.FINANCE_AND_ID),
            of("Case note summary from Health Pre-release report", BcstPathway.HEALTH),
        )
    }
}
