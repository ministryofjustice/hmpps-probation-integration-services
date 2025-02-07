package uk.gov.justice.digital.hmpps.integrations.prison

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.model.BcstPathway
import uk.gov.justice.digital.hmpps.model.StaffName
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit.SECONDS

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

    @Test
    fun `occurred at date has time component generated when alert type`() {
        val now = ZonedDateTime.now().withZoneSameLocal(EuropeLondon)
        val caseNote = PrisonCaseNote(
            UUID.randomUUID().toString(),
            1234,
            "A1234BC",
            "ALERT",
            "ACTIVE",
            creationDateTime = now,
            occurrenceDateTime = ZonedDateTime.of(LocalDate.now().minusDays(1).atStartOfDay(), EuropeLondon),
            authorName = "Jane Smith",
            text = "Alert Case Note",
            amendments = listOf()
        )

        assertThat(caseNote.occurredAt(), equalTo(now.minusDays(1)))
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
