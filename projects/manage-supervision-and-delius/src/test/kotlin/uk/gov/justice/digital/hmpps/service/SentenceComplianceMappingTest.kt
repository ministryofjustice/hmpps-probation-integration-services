package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiType
import java.time.LocalDate
import java.time.ZonedDateTime

internal class SentenceComplianceMappingTest {

    @Test
    fun `counts inactive breaches as prior breaches on current order`() {
        val breachType = NsiType(code = "BRE", description = "Breach", id = 1)
        val recallType = NsiType(code = "REC", description = "Recall", id = 2)
        val status = NsiStatus(id = 1, code = "STATUS", description = "Status")

        val activeBreach = Nsi(
            personId = 1,
            type = breachType,
            eventId = 99,
            actualStartDate = LocalDate.now().minusDays(1),
            expectedStartDate = null,
            nsiStatus = status,
            id = 1,
            lastUpdated = ZonedDateTime.now(),
            active = true,
        )
        val priorBreach = Nsi(
            personId = 1,
            type = breachType,
            eventId = 99,
            actualStartDate = LocalDate.now().minusDays(10),
            expectedStartDate = null,
            nsiStatus = status,
            id = 2,
            lastUpdated = ZonedDateTime.now(),
            active = false,
        )
        val priorRecall = Nsi(
            personId = 1,
            type = recallType,
            eventId = 99,
            actualStartDate = LocalDate.now().minusDays(5),
            expectedStartDate = null,
            nsiStatus = status,
            id = 3,
            lastUpdated = ZonedDateTime.now(),
            active = false,
        )

        val res = toSentenceCompliance(
            activities = emptyList(),
            breaches = listOf(activeBreach, priorBreach),
            recalls = listOf(priorRecall)
        )

        assertThat(res.breachStarted, equalTo(true))
        assertThat(res.breachesOnCurrentOrderCount, equalTo(1))
        assertThat(res.priorBreachesOnCurrentOrderCount, equalTo(1))
        assertThat(res.priorRecallsOnCurrentOrderCount, equalTo(1))
        assertThat(res.currentBreaches, equalTo(2))
        assertThat(res.failureToComplyCount, equalTo(0))
    }
}

