package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.cvl.CvlClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference

@ExtendWith(MockitoExtension::class)
internal class LicenceActivatedHandlerTest {
    @Mock
    internal lateinit var cvlClient: CvlClient

    @Mock
    internal lateinit var lca: LicenceConditionApplier

    @InjectMocks
    internal lateinit var lah: LicenceActivatedHandler

    @Test
    fun `exception returned when crn not found in message`() {
        val event = HmppsDomainEvent(DomainEventType.LicenceActivated.name, 1)
        val res = lah.licenceActivated(event).first()
        assertThat(res, instanceOf(ActionResult.Failure::class.java))
        val fail = res as ActionResult.Failure
        assertThat(fail.exception, instanceOf(IllegalArgumentException::class.java))
        assertThat(fail.exception.message, equalTo("No CRN Provided"))
    }

    @Test
    fun `exception returned when detail url not found in message`() {
        val event = HmppsDomainEvent(
            DomainEventType.LicenceActivated.name,
            1,
            personReference = PersonReference(listOf(PersonIdentifier("CRN", "X123456")))
        )
        val res = lah.licenceActivated(event).first()
        assertThat(res, instanceOf(ActionResult.Failure::class.java))
        val fail = res as ActionResult.Failure
        assertThat(fail.exception, instanceOf(IllegalArgumentException::class.java))
        assertThat(fail.exception.message, equalTo("No Detail Url Provided"))
    }

    @Test
    fun `exception returned when activated licence not found`() {
        val event = HmppsDomainEvent(
            DomainEventType.LicenceActivated.name,
            1,
            detailUrl = "https://cvl.service.co.uk/licence-activated/58eb2a20-6b0e-416b-b91d-5b98b0c1be7f",
            personReference = PersonReference(listOf(PersonIdentifier("CRN", "X123456")))
        )
        whenever(cvlClient.getActivatedLicence(any())).thenReturn(null)

        val res = lah.licenceActivated(event).first()
        assertThat(res, instanceOf(ActionResult.Failure::class.java))
        val fail = res as ActionResult.Failure
        assertThat(fail.exception, instanceOf(NotFoundException::class.java))
        assertThat(
            fail.exception.message,
            equalTo("Activated Licence with detailUrl of https://cvl.service.co.uk/licence-activated/58eb2a20-6b0e-416b-b91d-5b98b0c1be7f not found")
        )
    }
}
