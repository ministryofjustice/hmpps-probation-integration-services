package uk.gov.justice.digital.hmpps.integrations.approvedpremises

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.data.generator.AssessedByGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventDetailsGenerator
import java.time.LocalDate

class ApplicationAssessedTest {

    @Test
    fun `when arrival date is provided notes state that it will now be matched`() {
        val details = givenApplicationAssessedDetails(LocalDate.now())
        assertThat(
            details.eventDetails.notes, equalTo(
                """
                |Application for a placement in an Approved Premises has been assessed as suitable. The application will now be matched to a suitable Approved Premises.
                |Details of the application can be found here: https://example.com
                """.trimMargin()
            )
        )
    }

    @Test
    fun `when no arrival date is provided notes indicate placement to be received and approved`() {
        val details = givenApplicationAssessedDetails(null)
        assertThat(
            details.eventDetails.notes, equalTo(
                """
                |Application for a placement in an Approved Premises has been assessed as suitable. The application will now be matched to a suitable Approved Premises once a placement request has been received and approved.
                |Details of the application can be found here: https://example.com
                """.trimMargin()
            )
        )
    }

    private fun givenApplicationAssessedDetails(arrivalDate: LocalDate?): EventDetails<ApplicationAssessed> =
        EventDetailsGenerator.applicationAssessed(assessedBy = AssessedByGenerator.generate(), arrivalDate)
}