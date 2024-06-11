package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.data.generator.BookingGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.NotificationGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator

@TestPropertySource(properties = ["logging.level.org.springframework.transaction=DEBUG", "logging.level.org.hibernate.engine.transaction.internal.TransactionImpl=DEBUG"])
@ExtendWith(OutputCaptureExtension::class)
class PcstdIntegrationTransactionRollbackTest : PcstdIntegrationTestBase() {
    private val releaseOnLicence = "Released on Licence"

    @Test
    fun `when a prisoner is matched with more than one pom`(output: CapturedOutput) {

        val notification = NotificationGenerator.PRISONER_MATCHED_WITH_POM
        val person = PersonGenerator.MATCHABLE_WITH_POM
        withBooking(
            BookingGenerator.MATCHED_WITH_POM,
            BookingGenerator.MATCHED_WITH_POM.lastMovement(notification.message.occurredAt)
        )
        val before = getCustody(person.nomsNumber)
        assertThat(before.institution?.code, equalTo(InstitutionGenerator.DEFAULT.code))

        channelManager.getChannel(queueName).publishAndWait(notification)

        val custody = getCustody(person.nomsNumber)
        assertTrue(custody.isInCustody())
        assertThat(custody.institution?.code, equalTo(InstitutionGenerator.DEFAULT.code))

        verifyCustodyHistory(
            custody
        )

        assertThat(output.toString(), containsString("rolling back"))
        assertThat(output.toString(), containsString("Query did not return a unique result: 2 results were returned"))
    }
}
