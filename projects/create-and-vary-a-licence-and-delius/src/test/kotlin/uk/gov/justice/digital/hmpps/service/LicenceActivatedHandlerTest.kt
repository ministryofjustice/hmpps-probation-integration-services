package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.web.client.HttpClientErrorException.NotFound
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference

@ExtendWith(MockitoExtension::class)
internal class LicenceActivatedHandlerTest {
    @Mock
    internal lateinit var detailService: DomainEventDetailService

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
    fun `ignore when activated licence not found`() {
        val event = HmppsDomainEvent(
            DomainEventType.LicenceActivated.name,
            1,
            detailUrl = "https://cvl.service.co.uk/licence-activated/58eb2a20-6b0e-416b-b91d-5b98b0c1be7f",
            personReference = PersonReference(listOf(PersonIdentifier("CRN", "X123456")))
        )
        whenever(detailService.getDetail<Any>(anyOrNull(), anyOrNull())).thenThrow(mock(NotFound::class.java))

        val res = lah.licenceActivated(event).first()
        assertThat(res, instanceOf(ActionResult.Ignored::class.java))
        val ignored = res as ActionResult.Ignored
        assertThat(ignored.reason, equalTo("Licence not found"))
    }
}
